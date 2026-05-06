package com.financeiro.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Define o pageTitle automaticamente para todas as páginas
 * baseado no atributo "pageTitle" já definido no model,
 * ou usa um título padrão baseado na URL.
 */
@ControllerAdvice
public class PageTitleAdvice {

    @ModelAttribute
    public void addPageTitle(Model model, HttpServletRequest request) {
        // Se o controller já definiu o pageTitle, não sobrescreve
        if (model.containsAttribute("pageTitle")) return;

        String path = request.getRequestURI();

        if (path.contains("/cartoes")) {
            model.addAttribute("pageTitle", "Cartões");
        } else if (path.contains("/investimentos")) {
            model.addAttribute("pageTitle", "Investimentos");
        } else if (path.contains("/relatorio")) {
            model.addAttribute("pageTitle", "Relatório");
        } else if (path.contains("/backup")) {
            model.addAttribute("pageTitle", "Backup");
        } else if (path.contains("/pessoas")) {
            model.addAttribute("pageTitle", "Pessoa");
        } else if (path.contains("/controle")) {
            model.addAttribute("pageTitle", "Controle");
        }
    }
}
