package com.financeiro.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

/**
 * Rastreia o status de pagamento de um gasto em um mês/ano específico.
 * Permite que gastos fixos e recorrentes tenham controle de pagamento independente por mês.
 */
@Entity
@Table(name = "pagamentos_mensais",
       uniqueConstraints = @UniqueConstraint(columnNames = {"gasto_id", "mes", "ano"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PagamentoMensal {

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

    @Column(nullable = false)
    @Builder.Default
    private boolean pago = false;

    private LocalDate dataPagamento;
}
