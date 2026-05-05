package com.financeiro.dto;

import com.financeiro.model.Gasto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
public class GastoMensalDTO {

    private Long gastoId;
    private String descricao;
    private BigDecimal valor;
    private BigDecimal valorBase;
    private Integer diaVencimento;
    private String tipo;
    private boolean recorrente;
    private boolean parcelado;
    private Integer parcelaAtual;
    private Integer totalParcelas;
    private boolean pago;
    private LocalDate dataPagamento;
    private LocalDate dataVencimentoCompleta;
    /** true se o valor exibido é customizado para este mês */
    private boolean valorCustomizado;

    public static GastoMensalDTO of(Gasto gasto, BigDecimal valorMes, boolean valorCustomizado,
                                     boolean pago, LocalDate dataPagamento, int mes, int ano) {
        int dia = Math.min(gasto.getDiaVencimento(), LocalDate.of(ano, mes, 1).lengthOfMonth());
        LocalDate dataVenc = LocalDate.of(ano, mes, dia);

        int parcelaAtual = gasto.isParcelado() ? gasto.getNumeroParcela(mes, ano) : 0;

        String desc = gasto.getDescricao();
        if (gasto.isParcelado() && gasto.getTotalParcelas() != null) {
            desc = gasto.getDescricao() + " (" + parcelaAtual + "/" + gasto.getTotalParcelas() + ")";
        }

        return new GastoMensalDTO(
                gasto.getId(),
                desc,
                valorMes,
                gasto.getValor(),
                gasto.getDiaVencimento(),
                gasto.getTipo().getDescricao(),
                gasto.isRecorrente(),
                gasto.isParcelado(),
                parcelaAtual,
                gasto.getTotalParcelas(),
                pago,
                dataPagamento,
                dataVenc,
                valorCustomizado
        );
    }
}
