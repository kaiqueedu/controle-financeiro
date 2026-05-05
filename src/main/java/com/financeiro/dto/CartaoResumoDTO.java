package com.financeiro.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class CartaoResumoDTO {
    private Long cartaoId;
    private String nome;
    private String bandeira;
    private Integer diaVencimento;
    private BigDecimal totalFatura;
    private boolean pago;
}
