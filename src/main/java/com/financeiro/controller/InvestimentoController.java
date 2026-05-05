package com.financeiro.controller;

import com.financeiro.model.Investimento;
import com.financeiro.service.InvestimentoService;
import com.financeiro.service.PessoaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

@Controller
@RequestMapping("/investimentos")
@RequiredArgsConstructor
public class InvestimentoController {

    private final InvestimentoService investimentoService;
    private final PessoaService pessoaService;

    @GetMapping("/{pessoaId}")
    public String painel(@PathVariable Long pessoaId, Model model) {
        var pessoa = pessoaService.buscarPorId(pessoaId);
        var investimentos = investimentoService.listarPorPessoa(pessoaId);
        var totalInvestido = investimentoService.totalInvestido(pessoaId);
        var totalPorBanco = investimentoService.totalPorBanco(pessoaId);

        model.addAttribute("pessoa", pessoa);
        model.addAttribute("investimentos", investimentos);
        model.addAttribute("totalInvestido", totalInvestido);
        model.addAttribute("totalPorBanco", totalPorBanco);
        return "investimento/painel";
    }

    @GetMapping("/{pessoaId}/novo")
    public String formularioNovo(@PathVariable Long pessoaId, Model model) {
        Investimento investimento = new Investimento();
        investimento.setDataInvestimento(LocalDate.now());

        model.addAttribute("investimento", investimento);
        model.addAttribute("pessoa", pessoaService.buscarPorId(pessoaId));
        return "investimento/form";
    }

    @GetMapping("/{pessoaId}/{id}/editar")
    public String formularioEditar(@PathVariable Long pessoaId,
                                    @PathVariable Long id,
                                    Model model) {
        model.addAttribute("investimento", investimentoService.buscarPorId(id));
        model.addAttribute("pessoa", pessoaService.buscarPorId(pessoaId));
        return "investimento/form";
    }

    @PostMapping("/{pessoaId}/salvar")
    public String salvar(@PathVariable Long pessoaId,
                          @Valid @ModelAttribute Investimento investimento,
                          BindingResult result,
                          Model model,
                          RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("pessoa", pessoaService.buscarPorId(pessoaId));
            return "investimento/form";
        }
        investimentoService.salvar(investimento, pessoaId);
        redirectAttributes.addFlashAttribute("mensagem", "Investimento salvo com sucesso!");
        return "redirect:/investimentos/" + pessoaId;
    }

    @GetMapping("/{pessoaId}/{id}/excluir")
    public String excluir(@PathVariable Long pessoaId,
                           @PathVariable Long id,
                           RedirectAttributes redirectAttributes) {
        investimentoService.excluir(id);
        redirectAttributes.addFlashAttribute("mensagem", "Investimento excluído com sucesso!");
        return "redirect:/investimentos/" + pessoaId;
    }
}
