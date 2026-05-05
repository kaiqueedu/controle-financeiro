package com.financeiro.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Entity
@Table(name = "gastos_cartao")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GastoCartao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Descrição é obrigatória")
    @Column(nullable = false)
    private String descricao;

    /** Valor total (ou valor da parcela se não parcelado) */
    @NotNull(message = "Valor é obrigatório")
    @Positive(message = "Valor deve ser positivo")
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal valor;

    /**
     * Se fixo = true, aparece em todos os meses (assinatura, academia, etc.)
     * Se fixo = false, é um gasto extra.
     */
    @Column(nullable = false)
    @Builder.Default
    private boolean fixo = true;

    /** Mês/ano de referência — mês de início para extras e parcelados */
    private Integer mesReferencia;
    private Integer anoReferencia;

    // ==================== PARCELAMENTO ====================

    /** Se o gasto é parcelado */
    @Column(nullable = false)
    @Builder.Default
    private boolean parcelado = false;

    /** Número total de parcelas */
    private Integer totalParcelas;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cartao_id", nullable = false)
    private CartaoCredito cartao;

    // ==================== MÉTODOS AUXILIARES ====================

    /** Valor da parcela mensal */
    public BigDecimal getValorParcela() {
        if (parcelado && totalParcelas != null && totalParcelas > 0) {
            return valor.divide(BigDecimal.valueOf(totalParcelas), 2, RoundingMode.HALF_UP);
        }
        return valor;
    }

    /**
     * Retorna o número da parcela para um dado mês/ano.
     * Ex: se começou em maio/2026 e estamos em julho/2026, retorna 3.
     */
    public int getNumeroParcela(int mes, int ano) {
        if (!parcelado || mesReferencia == null || anoReferencia == null) return 0;
        int mesesDiff = (ano - anoReferencia) * 12 + (mes - mesReferencia);
        return mesesDiff + 1;
    }
}
