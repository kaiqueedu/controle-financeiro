package com.financeiro.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * DTO para exportação/importação completa dos dados de uma pessoa.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BackupDTO {

    private String versao;
    private LocalDate dataExportacao;
    private PessoaBackup pessoa;

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class PessoaBackup {
        private String nome;
        private String email;
        private List<ReceitaBackup> receitas;
        private List<GastoBackup> gastos;
        private List<CartaoBackup> cartoes;
        private List<InvestimentoBackup> investimentos;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class ReceitaBackup {
        private String descricao;
        private BigDecimal valor;
        private Integer diaRecebimento;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class GastoBackup {
        private String descricao;
        private BigDecimal valor;
        private Integer diaVencimento;
        private String tipo;
        private boolean recorrente;
        private boolean parcelado;
        private Integer totalParcelas;
        private Integer mesOriginal;
        private Integer anoOriginal;
        private Integer mesEncerramento;
        private Integer anoEncerramento;
        private List<PagamentoBackup> pagamentos;
        private List<ValorMensalBackup> valoresMensais;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class PagamentoBackup {
        private int mes;
        private int ano;
        private boolean pago;
        private LocalDate dataPagamento;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class ValorMensalBackup {
        private int mes;
        private int ano;
        private BigDecimal valor;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class CartaoBackup {
        private String nome;
        private String bandeira;
        private Integer diaVencimento;
        private List<GastoCartaoBackup> gastos;
        private List<PagamentoCartaoBackup> pagamentos;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class GastoCartaoBackup {
        private String descricao;
        private BigDecimal valor;
        private boolean fixo;
        private boolean parcelado;
        private Integer totalParcelas;
        private Integer mesReferencia;
        private Integer anoReferencia;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class PagamentoCartaoBackup {
        private int mes;
        private int ano;
        private boolean pago;
        private LocalDate dataPagamento;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class InvestimentoBackup {
        private String descricao;
        private String banco;
        private BigDecimal valor;
        private LocalDate dataInvestimento;
        private String tipoInvestimento;
        private String observacoes;
    }
}
