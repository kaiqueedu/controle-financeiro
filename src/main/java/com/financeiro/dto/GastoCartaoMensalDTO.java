package com.financeiro.dto;

import com.financeiro.model.GastoCartao;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class GastoCartaoMensalDTO {

    private Long id;
    private String descricao;
    private BigDecimal valorExibido;
    private BigDecimal valorTotal;
    private boolean fixo;
    private boolean parcelado;
    private Integer parcelaAtual;
    private Integer totalParcelas;

    public static GastoCartaoMensalDTO of(GastoCartao gasto, int mes, int ano) {
        BigDecimal valorExibido = gasto.getValorParcela();
        int parcelaAtual = gasto.isParcelado() ? gasto.getNumeroParcela(mes, ano) : 0;

        String desc = gasto.getDescricao();
        if (gasto.isParcelado() && gasto.getTotalParcelas() != null) {
            desc = gasto.getDescricao() + " (" + parcelaAtual + "/" + gasto.getTotalParcelas() + ")";
        }

        return new GastoCartaoMensalDTO(
                gasto.getId(),
                desc,
                valorExibido,
                gasto.getValor(),
                gasto.isFixo(),
                gasto.isParcelado(),
                parcelaAtual,
                gasto.getTotalParcelas()
        );
    }
}
