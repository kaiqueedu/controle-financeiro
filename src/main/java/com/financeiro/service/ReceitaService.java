package com.financeiro.service;

import com.financeiro.model.Pessoa;
import com.financeiro.model.Receita;
import com.financeiro.repository.ReceitaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ReceitaService {

    private final ReceitaRepository receitaRepository;
    private final PessoaService pessoaService;

    /** Receitas são fixas — aparecem em todos os meses */
    public List<Receita> listarPorPessoa(Long pessoaId) {
        return receitaRepository.findByPessoaIdOrderByDiaRecebimentoAsc(pessoaId);
    }

    public BigDecimal totalReceitas(Long pessoaId) {
        return listarPorPessoa(pessoaId).stream()
                .map(Receita::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public Receita buscarPorId(Long id) {
        return receitaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Receita não encontrada: " + id));
    }

    public Receita salvar(Receita receita, Long pessoaId) {
        Pessoa pessoa = pessoaService.buscarPorId(pessoaId);
        receita.setPessoa(pessoa);
        return receitaRepository.save(receita);
    }

    public void excluir(Long id) {
        receitaRepository.deleteById(id);
    }
}
