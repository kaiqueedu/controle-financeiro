package com.financeiro.service;

import com.financeiro.dto.GastoCartaoMensalDTO;
import com.financeiro.model.CartaoCredito;
import com.financeiro.model.GastoCartao;
import com.financeiro.model.PagamentoCartaoMensal;
import com.financeiro.model.Pessoa;
import com.financeiro.repository.CartaoCreditoRepository;
import com.financeiro.repository.GastoCartaoRepository;
import com.financeiro.repository.PagamentoCartaoMensalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CartaoCreditoService {

    private final CartaoCreditoRepository cartaoRepository;
    private final GastoCartaoRepository gastoCartaoRepository;
    private final PagamentoCartaoMensalRepository pagamentoCartaoRepository;
    private final PessoaService pessoaService;

    // ==================== CARTÃO ====================

    public List<CartaoCredito> listarPorPessoa(Long pessoaId) {
        return cartaoRepository.findByPessoaIdOrderByNomeAsc(pessoaId);
    }

    public CartaoCredito buscarPorId(Long id) {
        return cartaoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Cartão não encontrado: " + id));
    }

    public CartaoCredito salvarCartao(CartaoCredito cartao, Long pessoaId) {
        Pessoa pessoa = pessoaService.buscarPorId(pessoaId);
        cartao.setPessoa(pessoa);
        return cartaoRepository.save(cartao);
    }

    public void excluirCartao(Long id) {
        cartaoRepository.deleteById(id);
    }

    // ==================== GASTOS DO CARTÃO ====================

    public List<GastoCartaoMensalDTO> listarGastosMensal(Long cartaoId, int mes, int ano) {
        return gastoCartaoRepository.findGastosMensalCandidatos(cartaoId, mes, ano).stream()
                .filter(g -> isVisivelNoMes(g, mes, ano))
                .map(g -> GastoCartaoMensalDTO.of(g, mes, ano))
                .toList();
    }

    private boolean isVisivelNoMes(GastoCartao gasto, int mes, int ano) {
        if (gasto.isFixo()) return true;
        if (!gasto.isParcelado()) return true;
        int parcela = gasto.getNumeroParcela(mes, ano);
        return parcela >= 1 && parcela <= gasto.getTotalParcelas();
    }

    public BigDecimal totalFaturaMensal(Long cartaoId, int mes, int ano) {
        return listarGastosMensal(cartaoId, mes, ano).stream()
                .map(GastoCartaoMensalDTO::getValorExibido)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal totalCartoesMensal(Long pessoaId, int mes, int ano) {
        return listarPorPessoa(pessoaId).stream()
                .map(c -> totalFaturaMensal(c.getId(), mes, ano))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /** Total pago dos cartões no mês */
    public BigDecimal totalCartoesPagosMensal(Long pessoaId, int mes, int ano) {
        return listarPorPessoa(pessoaId).stream()
                .filter(c -> isFaturaPaga(c.getId(), mes, ano))
                .map(c -> totalFaturaMensal(c.getId(), mes, ano))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public GastoCartao buscarGastoPorId(Long id) {
        return gastoCartaoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Gasto do cartão não encontrado: " + id));
    }

    public GastoCartao salvarGasto(GastoCartao gasto, Long cartaoId, int mesAtual, int anoAtual) {
        CartaoCredito cartao = buscarPorId(cartaoId);
        gasto.setCartao(cartao);

        if (!gasto.isFixo()) {
            if (gasto.getMesReferencia() == null) {
                gasto.setMesReferencia(mesAtual);
                gasto.setAnoReferencia(anoAtual);
            }
            if (!gasto.isParcelado()) {
                gasto.setTotalParcelas(null);
            }
        } else {
            gasto.setMesReferencia(null);
            gasto.setAnoReferencia(null);
            gasto.setParcelado(false);
            gasto.setTotalParcelas(null);
        }

        return gastoCartaoRepository.save(gasto);
    }

    public void excluirGasto(Long id) {
        gastoCartaoRepository.deleteById(id);
    }

    // ==================== PAGAMENTO DA FATURA ====================

    public boolean isFaturaPaga(Long cartaoId, int mes, int ano) {
        return pagamentoCartaoRepository.findByCartaoIdAndMesAndAno(cartaoId, mes, ano)
                .map(PagamentoCartaoMensal::isPago).orElse(false);
    }

    public void marcarFaturaPaga(Long cartaoId, int mes, int ano) {
        PagamentoCartaoMensal pagamento = pagamentoCartaoRepository
                .findByCartaoIdAndMesAndAno(cartaoId, mes, ano)
                .orElseGet(() -> {
                    PagamentoCartaoMensal p = new PagamentoCartaoMensal();
                    p.setCartao(buscarPorId(cartaoId));
                    p.setMes(mes);
                    p.setAno(ano);
                    return p;
                });
        pagamento.setPago(true);
        pagamento.setDataPagamento(LocalDate.now());
        pagamentoCartaoRepository.save(pagamento);
    }

    public void desmarcarFaturaPaga(Long cartaoId, int mes, int ano) {
        pagamentoCartaoRepository.findByCartaoIdAndMesAndAno(cartaoId, mes, ano)
                .ifPresent(p -> {
                    p.setPago(false);
                    p.setDataPagamento(null);
                    pagamentoCartaoRepository.save(p);
                });
    }
}
