package com.financeiro.repository;

import com.financeiro.model.GastoCartao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GastoCartaoRepository extends JpaRepository<GastoCartao, Long> {

    /** Gastos fixos do cartão (aparecem todo mês) */
    List<GastoCartao> findByCartaoIdAndFixoTrueOrderByDescricaoAsc(Long cartaoId);

    /**
     * Todos os gastos do cartão para um mês:
     * - Fixos (aparecem sempre)
     * - Extras não-parcelados do mês específico
     * - Parcelados que ainda estão dentro do período de parcelas
     */
    @Query("SELECT g FROM GastoCartao g WHERE g.cartao.id = :cartaoId AND (" +
           "  g.fixo = true " +
           "  OR (g.fixo = false AND g.parcelado = false AND g.mesReferencia = :mes AND g.anoReferencia = :ano) " +
           "  OR (g.fixo = false AND g.parcelado = true) " +
           ") ORDER BY g.fixo DESC, g.descricao ASC")
    List<GastoCartao> findGastosMensalCandidatos(@Param("cartaoId") Long cartaoId,
                                                   @Param("mes") int mes,
                                                   @Param("ano") int ano);
}
