package com.financeiro.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "gastos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Gasto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Descrição é obrigatória")
    @Column(nullable = false)
    private String descricao;

    @NotNull(message = "Valor é obrigatório")
    @Positive(message = "Valor deve ser positivo")
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal valor;

    @NotNull(message = "Dia de vencimento é obrigatório")
    @Column(nullable = false)
    private Integer diaVencimento;

    @NotNull(message = "Tipo de gasto é obrigatório")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoGasto tipo;

    @Column(nullable = false)
    @Builder.Default
    private boolean recorrente = false;

    /** Parcelado: aparece nos próximos N meses a partir do mês original */
    @Column(nullable = false)
    @Builder.Default
    private boolean parcelado = false;

    /** Número total de parcelas */
    private Integer totalParcelas;

    /** Mês/ano original de criação */
    private Integer mesOriginal;
    private Integer anoOriginal;

    /**
     * Mês/ano de encerramento — a partir deste mês o gasto NÃO aparece mais.
     * Usado para gastos fixos que deixaram de existir (ex: financiamento quitado).
     * Se null, o gasto continua ativo indefinidamente.
     */
    private Integer mesEncerramento;
    private Integer anoEncerramento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pessoa_id", nullable = false)
    private Pessoa pessoa;

    @OneToMany(mappedBy = "gasto", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PagamentoMensal> pagamentos = new ArrayList<>();

    @OneToMany(mappedBy = "gasto", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ValorMensalGasto> valoresMensais = new ArrayList<>();

    /**
     * Retorna o número da parcela para um dado mês/ano.
     */
    public int getNumeroParcela(int mes, int ano) {
        if (!parcelado || mesOriginal == null || anoOriginal == null) return 0;
        int mesesDiff = (ano - anoOriginal) * 12 + (mes - mesOriginal);
        return mesesDiff + 1;
    }

    /**
     * Verifica se o gasto está ativo (não encerrado) no mês/ano informado.
     * Se mesEncerramento é null, está sempre ativo.
     * O gasto NÃO aparece no mês de encerramento nem depois.
     */
    public boolean isAtivoNoMes(int mes, int ano) {
        if (mesEncerramento == null || anoEncerramento == null) return true;
        int refEncerramento = anoEncerramento * 12 + mesEncerramento;
        int refAtual = ano * 12 + mes;
        return refAtual < refEncerramento;
    }
}
