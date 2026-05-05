package com.financeiro.repository;

import com.financeiro.model.PagamentoCartaoMensal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PagamentoCartaoMensalRepository extends JpaRepository<PagamentoCartaoMensal, Long> {

    Optional<PagamentoCartaoMensal> findByCartaoIdAndMesAndAno(Long cartaoId, int mes, int ano);
}
