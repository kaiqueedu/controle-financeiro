package com.financeiro.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "cartoes_credito")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartaoCredito {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Nome do cartão é obrigatório")
    @Column(nullable = false)
    private String nome;

    /** Bandeira (Visa, Mastercard, etc.) */
    private String bandeira;

    @NotNull(message = "Dia de vencimento da fatura é obrigatório")
    @Column(nullable = false)
    private Integer diaVencimento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pessoa_id", nullable = false)
    private Pessoa pessoa;

    /** Gastos fixos do cartão (assinaturas, academia, etc.) — aparecem todo mês */
    @OneToMany(mappedBy = "cartao", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<GastoCartao> gastosCartao = new ArrayList<>();
}
