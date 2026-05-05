package com.financeiro.controller;

import com.financeiro.service.PessoaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final PessoaService pessoaService;

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("pessoas", pessoaService.listarTodas());
        return "home";
    }
}
