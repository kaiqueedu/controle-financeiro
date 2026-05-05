package com.financeiro.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "investimentos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Investimento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Descrição é obrigatória")
    @Column(nullable = false)
    private String descricao;

    @NotBlank(message = "Banco/Corretora é obrigatório")
    @Column(nullable = false)
    private String banco;

    @NotNull(message = "Valor é obrigatório")
    @Positive(message = "Valor deve ser positivo")
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal valor;

    @NotNull(message = "Data do investimento é obrigatória")
    @Column(nullable = false)
    private LocalDate dataInvestimento;

    /** Tipo do investimento (ex: Renda Fixa, Ações, FII, Tesouro Direto, Crypto, etc.) */
    private String tipoInvestimento;

    /** Observações adicionais */
    private String observacoes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pessoa_id", nullable = false)
    private Pessoa pessoa;
}
