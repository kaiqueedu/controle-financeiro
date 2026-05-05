package com.financeiro.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * Armazena um valor customizado para um gasto em um mês/ano específico.
 * Permite que gastos recorrentes/parcelados tenham valores diferentes por mês.
 */
@Entity
@Table(name = "valores_mensais_gasto",
       uniqueConstraints = @UniqueConstraint(columnNames = {"gasto_id", "mes", "ano"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ValorMensalGasto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gasto_id", nullable = false)
    private Gasto gasto;

    @Column(nullable = false)
    private int mes;

    @Column(nullable = false)
    private int ano;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal valor;
}
