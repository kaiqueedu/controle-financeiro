package com.financeiro.repository;

import com.financeiro.model.PagamentoMensal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PagamentoMensalRepository extends JpaRepository<PagamentoMensal, Long> {

    Optional<PagamentoMensal> findByGastoIdAndMesAndAno(Long gastoId, int mes, int ano);
}
