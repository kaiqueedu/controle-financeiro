package com.financeiro.repository;

import com.financeiro.model.Investimento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface InvestimentoRepository extends JpaRepository<Investimento, Long> {

    List<Investimento> findByPessoaIdOrderByDataInvestimentoDesc(Long pessoaId);

    @Query("SELECT COALESCE(SUM(i.valor), 0) FROM Investimento i WHERE i.pessoa.id = :pessoaId")
    BigDecimal somarTotalInvestido(@Param("pessoaId") Long pessoaId);

    @Query("SELECT DISTINCT i.banco FROM Investimento i WHERE i.pessoa.id = :pessoaId ORDER BY i.banco")
    List<String> listarBancos(@Param("pessoaId") Long pessoaId);
}
