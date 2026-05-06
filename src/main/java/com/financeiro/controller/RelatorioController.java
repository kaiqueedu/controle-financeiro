package com.financeiro.controller;

import com.financeiro.dto.ResumoMensalDTO;
import com.financeiro.service.PessoaService;
import com.financeiro.service.RelatorioService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Controller
@RequestMapping("/relatorio")
@RequiredArgsConstructor
public class RelatorioController {

    private final RelatorioService relatorioService;
    private final PessoaService pessoaService;

    @GetMapping("/{pessoaId}")
    public String relatorio(@PathVariable Long pessoaId,
                             @RequestParam(required = false) Integer mes,
                             @RequestParam(required = false) Integer ano,
                             Model model) {

        LocalDate hoje = LocalDate.now();
        int m = mes != null ? mes : hoje.getMonthValue();
        int a = ano != null ? ano : hoje.getYear();

        var pessoa = pessoaService.buscarPorId(pessoaId);
        var resumo = relatorioService.gerarResumo(pessoaId, m, a);

        model.addAttribute("pessoa", pessoa);
        model.addAttribute("resumo", resumo);
        model.addAttribute("mesAtual", m);
        model.addAttribute("anoAtual", a);
        model.addAttribute("pageTitle", pessoa.getNome() + " - Relatório");

        return "relatorio/resumo";
    }

    @GetMapping("/{pessoaId}/comparar")
    public String comparar(@PathVariable Long pessoaId,
                            @RequestParam int mes1,
                            @RequestParam int ano1,
                            @RequestParam int mes2,
                            @RequestParam int ano2,
                            Model model) {

        var pessoa = pessoaService.buscarPorId(pessoaId);
        var resumo1 = relatorioService.gerarResumo(pessoaId, mes1, ano1);
        var resumo2 = relatorioService.gerarResumo(pessoaId, mes2, ano2);

        // Diferenças (resumo2 - resumo1)
        BigDecimal diffReceitas = resumo2.getTotalReceitas().subtract(resumo1.getTotalReceitas());
        BigDecimal diffFixos = resumo2.getTotalGastosFixos().subtract(resumo1.getTotalGastosFixos());
        BigDecimal diffVariaveis = resumo2.getTotalGastosVariaveis().subtract(resumo1.getTotalGastosVariaveis());
        BigDecimal diffCartoes = resumo2.getTotalCartoes().subtract(resumo1.getTotalCartoes());
        BigDecimal diffGastos = resumo2.getTotalGastos().subtract(resumo1.getTotalGastos());
        BigDecimal diffSaldo = resumo2.getSaldo().subtract(resumo1.getSaldo());

        // Alertas de gastos que subiram 15%+
        var alertas = relatorioService.gerarAlertas(pessoaId, mes1, ano1, mes2, ano2);

        model.addAttribute("pessoa", pessoa);
        model.addAttribute("resumo1", resumo1);
        model.addAttribute("resumo2", resumo2);
        model.addAttribute("diffReceitas", diffReceitas);
        model.addAttribute("diffFixos", diffFixos);
        model.addAttribute("diffVariaveis", diffVariaveis);
        model.addAttribute("diffCartoes", diffCartoes);
        model.addAttribute("diffGastos", diffGastos);
        model.addAttribute("diffSaldo", diffSaldo);
        model.addAttribute("alertas", alertas);

        return "relatorio/comparativo";
    }
}
