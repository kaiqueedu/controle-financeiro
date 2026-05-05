package com.financeiro.controller;

import com.financeiro.model.Pessoa;
import com.financeiro.service.PessoaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/pessoas")
@RequiredArgsConstructor
public class PessoaController {

    private final PessoaService pessoaService;

    @GetMapping("/nova")
    public String formularioNova(Model model) {
        model.addAttribute("pessoa", new Pessoa());
        return "pessoa/form";
    }

    @GetMapping("/{id}/editar")
    public String formularioEditar(@PathVariable Long id, Model model) {
        model.addAttribute("pessoa", pessoaService.buscarPorId(id));
        return "pessoa/form";
    }

    @PostMapping("/salvar")
    public String salvar(@Valid @ModelAttribute Pessoa pessoa,
                         BindingResult result,
                         RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "pessoa/form";
        }
        pessoaService.salvar(pessoa);
        redirectAttributes.addFlashAttribute("mensagem", "Pessoa salva com sucesso!");
        return "redirect:/";
    }

    @GetMapping("/{id}/excluir")
    public String excluir(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        pessoaService.excluir(id);
        redirectAttributes.addFlashAttribute("mensagem", "Pessoa excluída com sucesso!");
        return "redirect:/";
    }
}
