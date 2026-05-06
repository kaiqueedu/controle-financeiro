package com.financeiro.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

/**
 * Representa um alerta de gasto que subiu significativamente entre dois meses.
 */
@Getter
@AllArgsConstructor
public class AlertaGastoDTO {

    private String descricao;
    private String categoria;
    private BigDecimal valorMes1;
    private BigDecimal valorMes2;
    private BigDecimal diferenca;
    private double percentualAumento;
}
