package com.financeiro.controller;

import com.financeiro.model.CartaoCredito;
import com.financeiro.model.GastoCartao;
import com.financeiro.service.CartaoCreditoService;
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
@RequestMapping("/cartoes")
@RequiredArgsConstructor
public class CartaoController {

    private final CartaoCreditoService cartaoService;
    private final PessoaService pessoaService;

    // ==================== CARTÕES ====================

    @GetMapping("/{pessoaId}")
    public String listar(@PathVariable Long pessoaId, Model model) {
        model.addAttribute("pessoa", pessoaService.buscarPorId(pessoaId));
        model.addAttribute("cartoes", cartaoService.listarPorPessoa(pessoaId));
        return "cartao/lista";
    }

    @GetMapping("/{pessoaId}/novo")
    public String formularioNovoCartao(@PathVariable Long pessoaId, Model model) {
        CartaoCredito cartao = new CartaoCredito();
        cartao.setDiaVencimento(10);
        model.addAttribute("cartao", cartao);
        model.addAttribute("pessoa", pessoaService.buscarPorId(pessoaId));
        return "cartao/form";
    }

    @GetMapping("/{pessoaId}/{id}/editar")
    public String formularioEditarCartao(@PathVariable Long pessoaId,
                                          @PathVariable Long id, Model model) {
        model.addAttribute("cartao", cartaoService.buscarPorId(id));
        model.addAttribute("pessoa", pessoaService.buscarPorId(pessoaId));
        return "cartao/form";
    }

    @PostMapping("/{pessoaId}/salvar")
    public String salvarCartao(@PathVariable Long pessoaId,
                                @Valid @ModelAttribute CartaoCredito cartao,
                                BindingResult result, Model model,
                                RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("pessoa", pessoaService.buscarPorId(pessoaId));
            return "cartao/form";
        }
        cartaoService.salvarCartao(cartao, pessoaId);
        redirectAttributes.addFlashAttribute("mensagem", "Cartão salvo com sucesso!");
        return "redirect:/cartoes/" + pessoaId;
    }

    @GetMapping("/{pessoaId}/{id}/excluir")
    public String excluirCartao(@PathVariable Long pessoaId, @PathVariable Long id,
                                 RedirectAttributes redirectAttributes) {
        cartaoService.excluirCartao(id);
        redirectAttributes.addFlashAttribute("mensagem", "Cartão excluído com sucesso!");
        return "redirect:/cartoes/" + pessoaId;
    }

    // ==================== DETALHES / FATURA ====================

    @GetMapping("/{pessoaId}/{cartaoId}/fatura")
    public String fatura(@PathVariable Long pessoaId, @PathVariable Long cartaoId,
                          @RequestParam(required = false) Integer mes,
                          @RequestParam(required = false) Integer ano,
                          Model model) {
        LocalDate hoje = LocalDate.now();
        int m = mes != null ? mes : hoje.getMonthValue();
        int a = ano != null ? ano : hoje.getYear();

        var cartao = cartaoService.buscarPorId(cartaoId);
        var gastos = cartaoService.listarGastosMensal(cartaoId, m, a);
        var totalFatura = cartaoService.totalFaturaMensal(cartaoId, m, a);

        // Navegação de meses
        LocalDate mesRef = LocalDate.of(a, m, 1);
        LocalDate mesAnterior = mesRef.minusMonths(1);
        LocalDate mesSeguinte = mesRef.plusMonths(1);

        model.addAttribute("pessoa", pessoaService.buscarPorId(pessoaId));
        model.addAttribute("cartao", cartao);
        model.addAttribute("gastos", gastos);
        model.addAttribute("totalFatura", totalFatura);
        model.addAttribute("faturaPaga", cartaoService.isFaturaPaga(cartaoId, m, a));
        model.addAttribute("mesAtual", m);
        model.addAttribute("anoAtual", a);
        model.addAttribute("mesAnteriorMes", mesAnterior.getMonthValue());
        model.addAttribute("mesAnteriorAno", mesAnterior.getYear());
        model.addAttribute("mesSeguinteMes", mesSeguinte.getMonthValue());
        model.addAttribute("mesSeguinteAno", mesSeguinte.getYear());

        return "cartao/fatura";
    }

    // ==================== GASTOS DO CARTÃO ====================

    @GetMapping("/{pessoaId}/{cartaoId}/gasto/novo")
    public String formularioNovoGasto(@PathVariable Long pessoaId,
                                       @PathVariable Long cartaoId,
                                       @RequestParam(required = false) Boolean fixo,
                                       @RequestParam(required = false) Integer mes,
                                       @RequestParam(required = false) Integer ano,
                                       Model model) {
        GastoCartao gasto = new GastoCartao();
        gasto.setFixo(fixo != null ? fixo : true);

        int m = mes != null ? mes : LocalDate.now().getMonthValue();
        int a = ano != null ? ano : LocalDate.now().getYear();

        model.addAttribute("gastoCartao", gasto);
        model.addAttribute("pessoa", pessoaService.buscarPorId(pessoaId));
        model.addAttribute("cartao", cartaoService.buscarPorId(cartaoId));
        model.addAttribute("mesAtual", m);
        model.addAttribute("anoAtual", a);
        return "cartao/gasto-form";
    }

    @GetMapping("/{pessoaId}/{cartaoId}/gasto/{id}/editar")
    public String formularioEditarGasto(@PathVariable Long pessoaId,
                                         @PathVariable Long cartaoId,
                                         @PathVariable Long id,
                                         @RequestParam int mes, @RequestParam int ano,
                                         Model model) {
        model.addAttribute("gastoCartao", cartaoService.buscarGastoPorId(id));
        model.addAttribute("pessoa", pessoaService.buscarPorId(pessoaId));
        model.addAttribute("cartao", cartaoService.buscarPorId(cartaoId));
        model.addAttribute("mesAtual", mes);
        model.addAttribute("anoAtual", ano);
        return "cartao/gasto-form";
    }

    @PostMapping("/{pessoaId}/{cartaoId}/gasto/salvar")
    public String salvarGasto(@PathVariable Long pessoaId,
                               @PathVariable Long cartaoId,
                               @Valid @ModelAttribute("gastoCartao") GastoCartao gasto,
                               BindingResult result,
                               @RequestParam int mesAtual, @RequestParam int anoAtual,
                               Model model, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("pessoa", pessoaService.buscarPorId(pessoaId));
            model.addAttribute("cartao", cartaoService.buscarPorId(cartaoId));
            model.addAttribute("mesAtual", mesAtual);
            model.addAttribute("anoAtual", anoAtual);
            return "cartao/gasto-form";
        }
        cartaoService.salvarGasto(gasto, cartaoId, mesAtual, anoAtual);
        redirectAttributes.addFlashAttribute("mensagem", "Gasto do cartão salvo com sucesso!");
        return "redirect:/cartoes/" + pessoaId + "/" + cartaoId + "/fatura?mes=" + mesAtual + "&ano=" + anoAtual;
    }

    @GetMapping("/{pessoaId}/{cartaoId}/gasto/{id}/excluir")
    public String excluirGasto(@PathVariable Long pessoaId, @PathVariable Long cartaoId,
                                @PathVariable Long id,
                                @RequestParam int mes, @RequestParam int ano,
                                RedirectAttributes redirectAttributes) {
        cartaoService.excluirGasto(id);
        redirectAttributes.addFlashAttribute("mensagem", "Gasto excluído com sucesso!");
        return "redirect:/cartoes/" + pessoaId + "/" + cartaoId + "/fatura?mes=" + mes + "&ano=" + ano;
    }

    // ==================== PAGAMENTO DA FATURA ====================

    @GetMapping("/{pessoaId}/{cartaoId}/pagar")
    public String marcarFaturaPaga(@PathVariable Long pessoaId, @PathVariable Long cartaoId,
                                    @RequestParam int mes, @RequestParam int ano,
                                    @RequestParam(required = false) String origem,
                                    RedirectAttributes redirectAttributes) {
        cartaoService.marcarFaturaPaga(cartaoId, mes, ano);
        redirectAttributes.addFlashAttribute("mensagem", "Fatura marcada como paga!");
        if ("painel".equals(origem)) {
            return "redirect:/controle/" + pessoaId + "?mes=" + mes + "&ano=" + ano;
        }
        return "redirect:/cartoes/" + pessoaId + "/" + cartaoId + "/fatura?mes=" + mes + "&ano=" + ano;
    }

    @GetMapping("/{pessoaId}/{cartaoId}/despagar")
    public String desmarcarFaturaPaga(@PathVariable Long pessoaId, @PathVariable Long cartaoId,
                                       @RequestParam int mes, @RequestParam int ano,
                                       @RequestParam(required = false) String origem,
                                       RedirectAttributes redirectAttributes) {
        cartaoService.desmarcarFaturaPaga(cartaoId, mes, ano);
        redirectAttributes.addFlashAttribute("mensagem", "Pagamento da fatura desmarcado!");
        if ("painel".equals(origem)) {
            return "redirect:/controle/" + pessoaId + "?mes=" + mes + "&ano=" + ano;
        }
        return "redirect:/cartoes/" + pessoaId + "/" + cartaoId + "/fatura?mes=" + mes + "&ano=" + ano;
    }
}
