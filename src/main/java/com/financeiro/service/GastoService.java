package com.financeiro.service;

import com.financeiro.dto.GastoMensalDTO;
import com.financeiro.model.*;
import com.financeiro.repository.GastoRepository;
import com.financeiro.repository.PagamentoMensalRepository;
import com.financeiro.repository.ValorMensalGastoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class GastoService {

    private final GastoRepository gastoRepository;
    private final PagamentoMensalRepository pagamentoRepository;
    private final ValorMensalGastoRepository valorMensalRepository;
    private final PessoaService pessoaService;

    // ==================== LISTAGEM ====================

    public List<GastoMensalDTO> listarFixosPorMes(Long pessoaId, int mes, int ano) {
        List<Gasto> gastos = gastoRepository.findByPessoaIdAndTipoOrderByDiaVencimentoAsc(pessoaId, TipoGasto.FIXO);
        return gastos.stream()
                .filter(g -> g.isAtivoNoMes(mes, ano))
                .map(g -> toDTO(g, mes, ano))
                .toList();
    }

    public List<GastoMensalDTO> listarVariaveisPorMes(Long pessoaId, int mes, int ano) {
        return gastoRepository.findVariaveisCandidatos(pessoaId, mes, ano).stream()
                .filter(g -> isVisivelNoMes(g, mes, ano))
                .map(g -> toDTO(g, mes, ano))
                .toList();
    }

    private boolean isVisivelNoMes(Gasto gasto, int mes, int ano) {
        if (gasto.isRecorrente() && !gasto.isParcelado()) return true;
        if (!gasto.isParcelado()) return true; // não-recorrente simples, já filtrado pela query
        // Parcelado: verificar se a parcela atual está dentro do range
        int parcela = gasto.getNumeroParcela(mes, ano);
        return parcela >= 1 && parcela <= gasto.getTotalParcelas();
    }

    private GastoMensalDTO toDTO(Gasto gasto, int mes, int ano) {
        var pagamento = pagamentoRepository.findByGastoIdAndMesAndAno(gasto.getId(), mes, ano);
        boolean pago = pagamento.map(PagamentoMensal::isPago).orElse(false);
        LocalDate dataPgto = pagamento.map(PagamentoMensal::getDataPagamento).orElse(null);

        // Buscar valor customizado do mês, se existir
        var valorCustom = valorMensalRepository.findByGastoIdAndMesAndAno(gasto.getId(), mes, ano);
        BigDecimal valorMes = valorCustom.map(ValorMensalGasto::getValor).orElse(gasto.getValor());
        boolean customizado = valorCustom.isPresent();

        return GastoMensalDTO.of(gasto, valorMes, customizado, pago, dataPgto, mes, ano);
    }

    // ==================== TOTAIS ====================

    public BigDecimal totalGastosFixos(Long pessoaId, int mes, int ano) {
        return gastoRepository.findByPessoaIdAndTipoOrderByDiaVencimentoAsc(pessoaId, TipoGasto.FIXO)
                .stream()
                .filter(g -> g.isAtivoNoMes(mes, ano))
                .map(g -> getValorDoMes(g, mes, ano))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal totalGastosVariaveis(Long pessoaId, int mes, int ano) {
        return gastoRepository.findVariaveisCandidatos(pessoaId, mes, ano).stream()
                .filter(g -> isVisivelNoMes(g, mes, ano))
                .map(g -> getValorDoMes(g, mes, ano))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal totalPagosMensal(Long pessoaId, int mes, int ano) {
        BigDecimal totalFixosPagos = gastoRepository.findByPessoaIdAndTipoOrderByDiaVencimentoAsc(pessoaId, TipoGasto.FIXO)
                .stream()
                .filter(g -> g.isAtivoNoMes(mes, ano))
                .filter(g -> isPago(g.getId(), mes, ano))
                .map(g -> getValorDoMes(g, mes, ano))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalVarPagos = gastoRepository.findVariaveisCandidatos(pessoaId, mes, ano).stream()
                .filter(g -> isVisivelNoMes(g, mes, ano))
                .filter(g -> isPago(g.getId(), mes, ano))
                .map(g -> getValorDoMes(g, mes, ano))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return totalFixosPagos.add(totalVarPagos);
    }

    private BigDecimal getValorDoMes(Gasto gasto, int mes, int ano) {
        return valorMensalRepository.findByGastoIdAndMesAndAno(gasto.getId(), mes, ano)
                .map(ValorMensalGasto::getValor)
                .orElse(gasto.getValor());
    }

    private boolean isPago(Long gastoId, int mes, int ano) {
        return pagamentoRepository.findByGastoIdAndMesAndAno(gastoId, mes, ano)
                .map(PagamentoMensal::isPago).orElse(false);
    }

    // ==================== CRUD ====================

    public Gasto buscarPorId(Long id) {
        return gastoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Gasto não encontrado: " + id));
    }

    public Gasto salvar(Gasto gasto, Long pessoaId, int mesAtual, int anoAtual) {
        Pessoa pessoa = pessoaService.buscarPorId(pessoaId);
        gasto.setPessoa(pessoa);

        if (gasto.getTipo() == TipoGasto.FIXO) {
            gasto.setRecorrente(true);
            gasto.setParcelado(false);
            gasto.setTotalParcelas(null);
        }

        if (gasto.getTipo() == TipoGasto.VARIAVEL) {
            // Parcelado precisa de mês original
            if (gasto.isParcelado()) {
                gasto.setRecorrente(false);
                if (gasto.getMesOriginal() == null) {
                    gasto.setMesOriginal(mesAtual);
                    gasto.setAnoOriginal(anoAtual);
                }
            } else if (!gasto.isRecorrente()) {
                // Não-recorrente e não-parcelado: só aparece no mês atual
                if (gasto.getMesOriginal() == null) {
                    gasto.setMesOriginal(mesAtual);
                    gasto.setAnoOriginal(anoAtual);
                }
            } else {
                // Recorrente: não precisa de mês original nem parcelas
                gasto.setMesOriginal(null);
                gasto.setAnoOriginal(null);
                gasto.setParcelado(false);
                gasto.setTotalParcelas(null);
            }
        }

        return gastoRepository.save(gasto);
    }

    // ==================== VALOR MENSAL CUSTOMIZADO ====================

    public void salvarValorMensal(Long gastoId, int mes, int ano, BigDecimal valor) {
        ValorMensalGasto vmg = valorMensalRepository.findByGastoIdAndMesAndAno(gastoId, mes, ano)
                .orElseGet(() -> {
                    ValorMensalGasto v = new ValorMensalGasto();
                    v.setGasto(buscarPorId(gastoId));
                    v.setMes(mes);
                    v.setAno(ano);
                    return v;
                });
        vmg.setValor(valor);
        valorMensalRepository.save(vmg);
    }

    public BigDecimal getValorMensal(Long gastoId, int mes, int ano) {
        Gasto gasto = buscarPorId(gastoId);
        return valorMensalRepository.findByGastoIdAndMesAndAno(gastoId, mes, ano)
                .map(ValorMensalGasto::getValor)
                .orElse(gasto.getValor());
    }

    // ==================== PAGAMENTO MENSAL ====================

    public void encerrarGasto(Long gastoId, int mes, int ano) {
        Gasto gasto = buscarPorId(gastoId);
        gasto.setMesEncerramento(mes);
        gasto.setAnoEncerramento(ano);
        gastoRepository.save(gasto);
    }

    public void reativarGasto(Long gastoId) {
        Gasto gasto = buscarPorId(gastoId);
        gasto.setMesEncerramento(null);
        gasto.setAnoEncerramento(null);
        gastoRepository.save(gasto);
    }

    public void marcarComoPago(Long gastoId, int mes, int ano) {
        PagamentoMensal pagamento = pagamentoRepository
                .findByGastoIdAndMesAndAno(gastoId, mes, ano)
                .orElseGet(() -> {
                    PagamentoMensal p = new PagamentoMensal();
                    p.setGasto(buscarPorId(gastoId));
                    p.setMes(mes);
                    p.setAno(ano);
                    return p;
                });
        pagamento.setPago(true);
        pagamento.setDataPagamento(LocalDate.now());
        pagamentoRepository.save(pagamento);
    }

    public void desmarcarPagamento(Long gastoId, int mes, int ano) {
        pagamentoRepository.findByGastoIdAndMesAndAno(gastoId, mes, ano)
                .ifPresent(p -> {
                    p.setPago(false);
                    p.setDataPagamento(null);
                    pagamentoRepository.save(p);
                });
    }

    public void excluir(Long id) {
        gastoRepository.deleteById(id);
    }
}
