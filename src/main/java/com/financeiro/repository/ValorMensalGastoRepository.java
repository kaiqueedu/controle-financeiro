package com.financeiro.repository;

import com.financeiro.model.ValorMensalGasto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ValorMensalGastoRepository extends JpaRepository<ValorMensalGasto, Long> {

    Optional<ValorMensalGasto> findByGastoIdAndMesAndAno(Long gastoId, int mes, int ano);
}
