package com.financeiro.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
@Builder
public class ResumoMensalDTO {

    private int mes;
    private int ano;
    private String nomeMes;

    private BigDecimal totalReceitas;
    private BigDecimal totalGastosFixos;
    private BigDecimal totalGastosVariaveis;
    private BigDecimal totalCartoes;
    private BigDecimal totalGastos;
    private BigDecimal totalPagos;
    private BigDecimal saldo;

    /** Saldo positivo = sobrou, negativo = faltou */
    public boolean isSaldoPositivo() {
        return saldo.signum() >= 0;
    }
}
