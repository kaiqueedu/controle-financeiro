package com.financeiro.service;

import com.financeiro.model.Investimento;
import com.financeiro.model.Pessoa;
import com.financeiro.repository.InvestimentoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class InvestimentoService {

    private final InvestimentoRepository investimentoRepository;
    private final PessoaService pessoaService;

    public List<Investimento> listarPorPessoa(Long pessoaId) {
        return investimentoRepository.findByPessoaIdOrderByDataInvestimentoDesc(pessoaId);
    }

    public BigDecimal totalInvestido(Long pessoaId) {
        return investimentoRepository.somarTotalInvestido(pessoaId);
    }

    /** Agrupa os investimentos por banco e soma os valores */
    public Map<String, BigDecimal> totalPorBanco(Long pessoaId) {
        return listarPorPessoa(pessoaId).stream()
                .collect(Collectors.groupingBy(
                        Investimento::getBanco,
                        Collectors.reducing(BigDecimal.ZERO, Investimento::getValor, BigDecimal::add)
                ));
    }

    public Investimento buscarPorId(Long id) {
        return investimentoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Investimento não encontrado: " + id));
    }

    public Investimento salvar(Investimento investimento, Long pessoaId) {
        Pessoa pessoa = pessoaService.buscarPorId(pessoaId);
        investimento.setPessoa(pessoa);
        return investimentoRepository.save(investimento);
    }

    public void excluir(Long id) {
        investimentoRepository.deleteById(id);
    }
}
