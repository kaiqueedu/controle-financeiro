package com.financeiro.service;

import com.financeiro.model.Pessoa;
import com.financeiro.repository.PessoaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class PessoaService {

    private final PessoaRepository pessoaRepository;

    public List<Pessoa> listarTodas() {
        return pessoaRepository.findAllByOrderByNomeAsc();
    }

    public Pessoa buscarPorId(Long id) {
        return pessoaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Pessoa não encontrada: " + id));
    }

    public Pessoa salvar(Pessoa pessoa) {
        return pessoaRepository.save(pessoa);
    }

    public void excluir(Long id) {
        pessoaRepository.deleteById(id);
    }
}
