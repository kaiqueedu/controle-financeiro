package com.financeiro.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

/**
 * Rastreia o status de pagamento da fatura de um cartão em um mês/ano específico.
 */
@Entity
@Table(name = "pagamentos_cartao_mensais",
       uniqueConstraints = @UniqueConstraint(columnNames = {"cartao_id", "mes", "ano"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PagamentoCartaoMensal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cartao_id", nullable = false)
    private CartaoCredito cartao;

    @Column(nullable = false)
    private int mes;

    @Column(nullable = false)
    private int ano;

    @Column(nullable = false)
    @Builder.Default
    private boolean pago = false;

    private LocalDate dataPagamento;
}
