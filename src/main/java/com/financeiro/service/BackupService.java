package com.financeiro.service;

import com.financeiro.dto.BackupDTO;
import com.financeiro.dto.BackupDTO.*;
import com.financeiro.model.*;
import com.financeiro.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BackupService {

    private final PessoaService pessoaService;
    private final ReceitaRepository receitaRepository;
    private final GastoRepository gastoRepository;
    private final PagamentoMensalRepository pagamentoMensalRepository;
    private final ValorMensalGastoRepository valorMensalGastoRepository;
    private final CartaoCreditoRepository cartaoRepository;
    private final GastoCartaoRepository gastoCartaoRepository;
    private final PagamentoCartaoMensalRepository pagamentoCartaoRepository;
    private final InvestimentoRepository investimentoRepository;
    private final PessoaRepository pessoaRepository;

    private static final String VERSAO = "1.0.0";

    @Transactional(readOnly = true)
    public BackupDTO exportar(Long pessoaId) {
        Pessoa pessoa = pessoaService.buscarPorId(pessoaId);

        var receitas = receitaRepository.findByPessoaIdOrderByDiaRecebimentoAsc(pessoaId).stream()
                .map(r -> ReceitaBackup.builder()
                        .descricao(r.getDescricao())
                        .valor(r.getValor())
                        .diaRecebimento(r.getDiaRecebimento())
                        .build())
                .toList();

        var gastos = gastoRepository.findAll().stream()
                .filter(g -> g.getPessoa().getId().equals(pessoaId))
                .map(g -> {
                    var pagamentos = g.getPagamentos().stream()
                            .map(p -> PagamentoBackup.builder()
                                    .mes(p.getMes()).ano(p.getAno())
                                    .pago(p.isPago()).dataPagamento(p.getDataPagamento())
                                    .build())
                            .toList();
                    var valores = g.getValoresMensais().stream()
                            .map(v -> ValorMensalBackup.builder()
                                    .mes(v.getMes()).ano(v.getAno()).valor(v.getValor())
                                    .build())
                            .toList();
                    return GastoBackup.builder()
                            .descricao(g.getDescricao())
                            .valor(g.getValor())
                            .diaVencimento(g.getDiaVencimento())
                            .tipo(g.getTipo().name())
                            .recorrente(g.isRecorrente())
                            .parcelado(g.isParcelado())
                            .totalParcelas(g.getTotalParcelas())
                            .mesOriginal(g.getMesOriginal())
                            .anoOriginal(g.getAnoOriginal())
                            .mesEncerramento(g.getMesEncerramento())
                            .anoEncerramento(g.getAnoEncerramento())
                            .pagamentos(pagamentos)
                            .valoresMensais(valores)
                            .build();
                })
                .toList();

        var cartoes = cartaoRepository.findByPessoaIdOrderByNomeAsc(pessoaId).stream()
                .map(c -> {
                    var gastosCartao = c.getGastosCartao().stream()
                            .map(gc -> GastoCartaoBackup.builder()
                                    .descricao(gc.getDescricao())
                                    .valor(gc.getValor())
                                    .fixo(gc.isFixo())
                                    .parcelado(gc.isParcelado())
                                    .totalParcelas(gc.getTotalParcelas())
                                    .mesReferencia(gc.getMesReferencia())
                                    .anoReferencia(gc.getAnoReferencia())
                                    .build())
                            .toList();
                    var pagCartao = pagamentoCartaoRepository.findAll().stream()
                            .filter(p -> p.getCartao().getId().equals(c.getId()))
                            .map(p -> PagamentoCartaoBackup.builder()
                                    .mes(p.getMes()).ano(p.getAno())
                                    .pago(p.isPago()).dataPagamento(p.getDataPagamento())
                                    .build())
                            .toList();
                    return CartaoBackup.builder()
                            .nome(c.getNome())
                            .bandeira(c.getBandeira())
                            .diaVencimento(c.getDiaVencimento())
                            .gastos(gastosCartao)
                            .pagamentos(pagCartao)
                            .build();
                })
                .toList();

        var investimentos = investimentoRepository.findByPessoaIdOrderByDataInvestimentoDesc(pessoaId).stream()
                .map(i -> InvestimentoBackup.builder()
                        .descricao(i.getDescricao())
                        .banco(i.getBanco())
                        .valor(i.getValor())
                        .dataInvestimento(i.getDataInvestimento())
                        .tipoInvestimento(i.getTipoInvestimento())
                        .observacoes(i.getObservacoes())
                        .build())
                .toList();

        var pessoaBackup = PessoaBackup.builder()
                .nome(pessoa.getNome())
                .email(pessoa.getEmail())
                .receitas(receitas)
                .gastos(gastos)
                .cartoes(cartoes)
                .investimentos(investimentos)
                .build();

        return BackupDTO.builder()
                .versao(VERSAO)
                .dataExportacao(LocalDate.now())
                .pessoa(pessoaBackup)
                .build();
    }

    @Transactional
    public Pessoa importar(BackupDTO backup) {
        var pb = backup.getPessoa();

        // Cria a pessoa
        Pessoa pessoa = new Pessoa();
        pessoa.setNome(pb.getNome());
        pessoa.setEmail(pb.getEmail());
        pessoa = pessoaRepository.save(pessoa);

        // Receitas
        if (pb.getReceitas() != null) {
            for (var rb : pb.getReceitas()) {
                Receita r = new Receita();
                r.setDescricao(rb.getDescricao());
                r.setValor(rb.getValor());
                r.setDiaRecebimento(rb.getDiaRecebimento());
                r.setPessoa(pessoa);
                receitaRepository.save(r);
            }
        }

        // Gastos
        if (pb.getGastos() != null) {
            for (var gb : pb.getGastos()) {
                Gasto g = new Gasto();
                g.setDescricao(gb.getDescricao());
                g.setValor(gb.getValor());
                g.setDiaVencimento(gb.getDiaVencimento());
                g.setTipo(TipoGasto.valueOf(gb.getTipo()));
                g.setRecorrente(gb.isRecorrente());
                g.setParcelado(gb.isParcelado());
                g.setTotalParcelas(gb.getTotalParcelas());
                g.setMesOriginal(gb.getMesOriginal());
                g.setAnoOriginal(gb.getAnoOriginal());
                g.setMesEncerramento(gb.getMesEncerramento());
                g.setAnoEncerramento(gb.getAnoEncerramento());
                g.setPessoa(pessoa);
                g = gastoRepository.save(g);

                if (gb.getPagamentos() != null) {
                    for (var pgb : gb.getPagamentos()) {
                        PagamentoMensal pm = new PagamentoMensal();
                        pm.setGasto(g);
                        pm.setMes(pgb.getMes());
                        pm.setAno(pgb.getAno());
                        pm.setPago(pgb.isPago());
                        pm.setDataPagamento(pgb.getDataPagamento());
                        pagamentoMensalRepository.save(pm);
                    }
                }

                if (gb.getValoresMensais() != null) {
                    for (var vmb : gb.getValoresMensais()) {
                        ValorMensalGasto vm = new ValorMensalGasto();
                        vm.setGasto(g);
                        vm.setMes(vmb.getMes());
                        vm.setAno(vmb.getAno());
                        vm.setValor(vmb.getValor());
                        valorMensalGastoRepository.save(vm);
                    }
                }
            }
        }

        // Cartões
        if (pb.getCartoes() != null) {
            for (var cb : pb.getCartoes()) {
                CartaoCredito c = new CartaoCredito();
                c.setNome(cb.getNome());
                c.setBandeira(cb.getBandeira());
                c.setDiaVencimento(cb.getDiaVencimento());
                c.setPessoa(pessoa);
                c = cartaoRepository.save(c);

                if (cb.getGastos() != null) {
                    for (var gcb : cb.getGastos()) {
                        GastoCartao gc = new GastoCartao();
                        gc.setDescricao(gcb.getDescricao());
                        gc.setValor(gcb.getValor());
                        gc.setFixo(gcb.isFixo());
                        gc.setParcelado(gcb.isParcelado());
                        gc.setTotalParcelas(gcb.getTotalParcelas());
                        gc.setMesReferencia(gcb.getMesReferencia());
                        gc.setAnoReferencia(gcb.getAnoReferencia());
                        gc.setCartao(c);
                        gastoCartaoRepository.save(gc);
                    }
                }

                if (cb.getPagamentos() != null) {
                    for (var pcb : cb.getPagamentos()) {
                        PagamentoCartaoMensal pcm = new PagamentoCartaoMensal();
                        pcm.setCartao(c);
                        pcm.setMes(pcb.getMes());
                        pcm.setAno(pcb.getAno());
                        pcm.setPago(pcb.isPago());
                        pcm.setDataPagamento(pcb.getDataPagamento());
                        pagamentoCartaoRepository.save(pcm);
                    }
                }
            }
        }

        // Investimentos
        if (pb.getInvestimentos() != null) {
            for (var ib : pb.getInvestimentos()) {
                Investimento inv = new Investimento();
                inv.setDescricao(ib.getDescricao());
                inv.setBanco(ib.getBanco());
                inv.setValor(ib.getValor());
                inv.setDataInvestimento(ib.getDataInvestimento());
                inv.setTipoInvestimento(ib.getTipoInvestimento());
                inv.setObservacoes(ib.getObservacoes());
                inv.setPessoa(pessoa);
                investimentoRepository.save(inv);
            }
        }

        return pessoa;
    }
}
