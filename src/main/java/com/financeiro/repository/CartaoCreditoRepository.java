package com.financeiro.repository;

import com.financeiro.model.CartaoCredito;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CartaoCreditoRepository extends JpaRepository<CartaoCredito, Long> {

    List<CartaoCredito> findByPessoaIdOrderByNomeAsc(Long pessoaId);
}
