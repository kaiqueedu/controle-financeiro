package com.financeiro.repository;

import com.financeiro.model.Gasto;
import com.financeiro.model.TipoGasto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GastoRepository extends JpaRepository<Gasto, Long> {

    List<Gasto> findByPessoaIdAndTipoOrderByDiaVencimentoAsc(Long pessoaId, TipoGasto tipo);

    /**
     * Gastos variáveis candidatos para um mês:
     * - Recorrentes (aparecem sempre)
     * - Não-recorrentes do mês específico
     * - Parcelados (filtro fino feito no serviço)
     */
    @Query("SELECT g FROM Gasto g WHERE g.pessoa.id = :pessoaId AND g.tipo = 'VARIAVEL' " +
           "AND (g.recorrente = true " +
           "  OR (g.recorrente = false AND g.parcelado = false AND g.mesOriginal = :mes AND g.anoOriginal = :ano) " +
           "  OR (g.parcelado = true) " +
           ") ORDER BY g.diaVencimento ASC")
    List<Gasto> findVariaveisCandidatos(@Param("pessoaId") Long pessoaId,
                                         @Param("mes") int mes,
                                         @Param("ano") int ano);
}
