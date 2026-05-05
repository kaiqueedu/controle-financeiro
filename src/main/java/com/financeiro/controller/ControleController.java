package com.financeiro.controller;

import com.financeiro.model.Gasto;
import com.financeiro.model.Receita;
import com.financeiro.model.TipoGasto;
import com.financeiro.dto.CartaoResumoDTO;
import com.financeiro.service.CartaoCreditoService;
import com.financeiro.service.GastoService;
import com.financeiro.service.PessoaService;
import com.financeiro.service.ReceitaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.Locale;

@Controller
@RequestMapping("/controle")
@RequiredArgsConstructor
public class ControleController {

    private final PessoaService pessoaService;
    private final ReceitaService receitaService;
    private final GastoService gastoService;
    private final CartaoCreditoService cartaoService;

    @GetMapping("/{pessoaId}")
    public String painel(@PathVariable Long pessoaId,
                         @RequestParam(required = false) Integer mes,
                         @RequestParam(required = false) Integer ano,
                         Model model) {

        LocalDate hoje = LocalDate.now();
        int mesAtual = mes != null ? mes : hoje.getMonthValue();
        int anoAtual = ano != null ? ano : hoje.getYear();

        var pessoa = pessoaService.buscarPorId(pessoaId);

        // Receitas (fixas — aparecem em todos os meses)
        var receitas = receitaService.listarPorPessoa(pessoaId);
        BigDecimal totalReceitas = receitaService.totalReceitas(pessoaId);

        // Gastos fixos (aparecem em todos os meses) e variáveis (recorrentes + do mês)
        var gastosFixos = gastoService.listarFixosPorMes(pessoaId, mesAtual, anoAtual);
        var gastosVariaveis = gastoService.listarVariaveisPorMes(pessoaId, mesAtual, anoAtual);

        BigDecimal totalFixos = gastoService.totalGastosFixos(pessoaId, mesAtual, anoAtual);
        BigDecimal totalVariaveis = gastoService.totalGastosVariaveis(pessoaId, mesAtual, anoAtual);
        BigDecimal totalCartoes = cartaoService.totalCartoesMensal(pessoaId, mesAtual, anoAtual);
        BigDecimal totalGastos = totalFixos.add(totalVariaveis).add(totalCartoes);
        BigDecimal totalPagos = gastoService.totalPagosMensal(pessoaId, mesAtual, anoAtual)
                .add(cartaoService.totalCartoesPagosMensal(pessoaId, mesAtual, anoAtual));

        // Cartões de crédito resumo
        var cartoesResumo = cartaoService.listarPorPessoa(pessoaId).stream()
                .map(c -> new CartaoResumoDTO(
                        c.getId(), c.getNome(), c.getBandeira(), c.getDiaVencimento(),
                        cartaoService.totalFaturaMensal(c.getId(), mesAtual, anoAtual),
                        cartaoService.isFaturaPaga(c.getId(), mesAtual, anoAtual)))
                .toList();

        // Saldo
        BigDecimal saldo = totalReceitas.subtract(totalGastos);

        // Nome do mês
        String nomeMes = Month.of(mesAtual).getDisplayName(TextStyle.FULL, new Locale("pt", "BR"));

        // Cor do mês
        String[] coresMeses = {
            "#e74c3c", "#e67e22", "#f1c40f", "#2ecc71", "#1abc9c", "#3498db",
            "#9b59b6", "#e84393", "#00b894", "#fd79a8", "#636e72", "#2d3436"
        };
        String corMes = coresMeses[mesAtual - 1];

        // Navegação de meses
        LocalDate mesRef = LocalDate.of(anoAtual, mesAtual, 1);
        LocalDate mesAnterior = mesRef.minusMonths(1);
        LocalDate mesSeguinte = mesRef.plusMonths(1);

        model.addAttribute("pessoa", pessoa);
        model.addAttribute("receitas", receitas);
        model.addAttribute("gastosFixos", gastosFixos);
        model.addAttribute("gastosVariaveis", gastosVariaveis);
        model.addAttribute("totalReceitas", totalReceitas);
        model.addAttribute("totalFixos", totalFixos);
        model.addAttribute("totalVariaveis", totalVariaveis);
        model.addAttribute("totalCartoes", totalCartoes);
        model.addAttribute("totalGastos", totalGastos);
        model.addAttribute("totalPagos", totalPagos);
        model.addAttribute("saldo", saldo);
        model.addAttribute("cartoesResumo", cartoesResumo);
        model.addAttribute("mesAtual", mesAtual);
        model.addAttribute("anoAtual", anoAtual);
        model.addAttribute("nomeMes", nomeMes);
        model.addAttribute("mesAnteriorMes", mesAnterior.getMonthValue());
        model.addAttribute("mesAnteriorAno", mesAnterior.getYear());
        model.addAttribute("mesSeguinteMes", mesSeguinte.getMonthValue());
        model.addAttribute("mesSeguinteAno", mesSeguinte.getYear());
        model.addAttribute("corMes", corMes);

        return "controle/painel";
    }

    // ==================== RECEITAS ====================

    @GetMapping("/{pessoaId}/receita/nova")
    public String formularioNovaReceita(@PathVariable Long pessoaId,
                                         @RequestParam(required = false) Integer mes,
                                         @RequestParam(required = false) Integer ano,
                                         Model model) {
        Receita receita = new Receita();
        receita.setDiaRecebimento(1);

        int m = mes != null ? mes : LocalDate.now().getMonthValue();
        int a = ano != null ? ano : LocalDate.now().getYear();

        model.addAttribute("receita", receita);
        model.addAttribute("pessoa", pessoaService.buscarPorId(pessoaId));
        model.addAttribute("mesAtual", m);
        model.addAttribute("anoAtual", a);
        return "controle/receita-form";
    }

    @GetMapping("/{pessoaId}/receita/{id}/editar")
    public String formularioEditarReceita(@PathVariable Long pessoaId,
                                           @PathVariable Long id,
                                           @RequestParam int mes,
                                           @RequestParam int ano,
                                           Model model) {
        model.addAttribute("receita", receitaService.buscarPorId(id));
        model.addAttribute("pessoa", pessoaService.buscarPorId(pessoaId));
        model.addAttribute("mesAtual", mes);
        model.addAttribute("anoAtual", ano);
        return "controle/receita-form";
    }

    @PostMapping("/{pessoaId}/receita/salvar")
    public String salvarReceita(@PathVariable Long pessoaId,
                                 @Valid @ModelAttribute Receita receita,
                                 BindingResult result,
                                 @RequestParam int mesAtual,
                                 @RequestParam int anoAtual,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("pessoa", pessoaService.buscarPorId(pessoaId));
            model.addAttribute("mesAtual", mesAtual);
            model.addAttribute("anoAtual", anoAtual);
            return "controle/receita-form";
        }
        receitaService.salvar(receita, pessoaId);
        redirectAttributes.addFlashAttribute("mensagem", "Receita salva com sucesso!");
        return "redirect:/controle/" + pessoaId + "?mes=" + mesAtual + "&ano=" + anoAtual;
    }

    @GetMapping("/{pessoaId}/receita/{id}/excluir")
    public String excluirReceita(@PathVariable Long pessoaId,
                                  @PathVariable Long id,
                                  @RequestParam int mes,
                                  @RequestParam int ano,
                                  RedirectAttributes redirectAttributes) {
        receitaService.excluir(id);
        redirectAttributes.addFlashAttribute("mensagem", "Receita excluída com sucesso!");
        return "redirect:/controle/" + pessoaId + "?mes=" + mes + "&ano=" + ano;
    }

    // ==================== GASTOS ====================

    @GetMapping("/{pessoaId}/gasto/novo")
    public String formularioNovoGasto(@PathVariable Long pessoaId,
                                       @RequestParam(required = false) Integer mes,
                                       @RequestParam(required = false) Integer ano,
                                       @RequestParam(required = false) String tipo,
                                       Model model) {
        Gasto gasto = new Gasto();
        gasto.setDiaVencimento(1);

        int m = mes != null ? mes : LocalDate.now().getMonthValue();
        int a = ano != null ? ano : LocalDate.now().getYear();

        if (tipo != null) {
            gasto.setTipo(TipoGasto.valueOf(tipo));
        }

        model.addAttribute("gasto", gasto);
        model.addAttribute("pessoa", pessoaService.buscarPorId(pessoaId));
        model.addAttribute("tipos", TipoGasto.values());
        model.addAttribute("mesAtual", m);
        model.addAttribute("anoAtual", a);
        return "controle/gasto-form";
    }

    @GetMapping("/{pessoaId}/gasto/{id}/editar")
    public String formularioEditarGasto(@PathVariable Long pessoaId,
                                         @PathVariable Long id,
                                         @RequestParam int mes,
                                         @RequestParam int ano,
                                         Model model) {
        model.addAttribute("gasto", gastoService.buscarPorId(id));
        model.addAttribute("pessoa", pessoaService.buscarPorId(pessoaId));
        model.addAttribute("tipos", TipoGasto.values());
        model.addAttribute("mesAtual", mes);
        model.addAttribute("anoAtual", ano);
        return "controle/gasto-form";
    }

    @PostMapping("/{pessoaId}/gasto/salvar")
    public String salvarGasto(@PathVariable Long pessoaId,
                               @Valid @ModelAttribute Gasto gasto,
                               BindingResult result,
                               @RequestParam int mesAtual,
                               @RequestParam int anoAtual,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("pessoa", pessoaService.buscarPorId(pessoaId));
            model.addAttribute("tipos", TipoGasto.values());
            model.addAttribute("mesAtual", mesAtual);
            model.addAttribute("anoAtual", anoAtual);
            return "controle/gasto-form";
        }
        gastoService.salvar(gasto, pessoaId, mesAtual, anoAtual);
        redirectAttributes.addFlashAttribute("mensagem", "Gasto salvo com sucesso!");
        return "redirect:/controle/" + pessoaId + "?mes=" + mesAtual + "&ano=" + anoAtual;
    }

    @GetMapping("/{pessoaId}/gasto/{id}/pagar")
    public String marcarComoPago(@PathVariable Long pessoaId,
                                  @PathVariable Long id,
                                  @RequestParam int mes,
                                  @RequestParam int ano,
                                  RedirectAttributes redirectAttributes) {
        gastoService.marcarComoPago(id, mes, ano);
        redirectAttributes.addFlashAttribute("mensagem", "Gasto marcado como pago!");
        return "redirect:/controle/" + pessoaId + "?mes=" + mes + "&ano=" + ano;
    }

    @GetMapping("/{pessoaId}/gasto/{id}/despagar")
    public String desmarcarPagamento(@PathVariable Long pessoaId,
                                      @PathVariable Long id,
                                      @RequestParam int mes,
                                      @RequestParam int ano,
                                      RedirectAttributes redirectAttributes) {
        gastoService.desmarcarPagamento(id, mes, ano);
        redirectAttributes.addFlashAttribute("mensagem", "Pagamento desmarcado!");
        return "redirect:/controle/" + pessoaId + "?mes=" + mes + "&ano=" + ano;
    }

    @GetMapping("/{pessoaId}/gasto/{id}/excluir")
    public String excluirGasto(@PathVariable Long pessoaId,
                                @PathVariable Long id,
                                @RequestParam int mes,
                                @RequestParam int ano,
                                RedirectAttributes redirectAttributes) {
        gastoService.excluir(id);
        redirectAttributes.addFlashAttribute("mensagem", "Gasto excluído com sucesso!");
        return "redirect:/controle/" + pessoaId + "?mes=" + mes + "&ano=" + ano;
    }

    // ==================== EDITAR VALOR DO MÊS ====================

    @GetMapping("/{pessoaId}/gasto/{id}/valor-mes")
    public String formularioValorMes(@PathVariable Long pessoaId,
                                      @PathVariable Long id,
                                      @RequestParam int mes,
                                      @RequestParam int ano,
                                      Model model) {
        var gasto = gastoService.buscarPorId(id);
        var valorAtual = gastoService.getValorMensal(id, mes, ano);

        model.addAttribute("gasto", gasto);
        model.addAttribute("pessoa", pessoaService.buscarPorId(pessoaId));
        model.addAttribute("valorAtual", valorAtual);
        model.addAttribute("mesAtual", mes);
        model.addAttribute("anoAtual", ano);
        return "controle/valor-mes-form";
    }

    @PostMapping("/{pessoaId}/gasto/{id}/valor-mes")
    public String salvarValorMes(@PathVariable Long pessoaId,
                                  @PathVariable Long id,
                                  @RequestParam int mes,
                                  @RequestParam int ano,
                                  @RequestParam java.math.BigDecimal valor,
                                  RedirectAttributes redirectAttributes) {
        gastoService.salvarValorMensal(id, mes, ano, valor);
        redirectAttributes.addFlashAttribute("mensagem", "Valor do mês atualizado!");
        return "redirect:/controle/" + pessoaId + "?mes=" + mes + "&ano=" + ano;
    }

    // ==================== ENCERRAR GASTO FIXO ====================

    @GetMapping("/{pessoaId}/gasto/{id}/encerrar")
    public String encerrarGasto(@PathVariable Long pessoaId,
                                 @PathVariable Long id,
                                 @RequestParam int mes,
                                 @RequestParam int ano,
                                 RedirectAttributes redirectAttributes) {
        // Encerra a partir do próximo mês (o mês atual ainda aparece)
        java.time.LocalDate proximo = java.time.LocalDate.of(ano, mes, 1).plusMonths(1);
        gastoService.encerrarGasto(id, proximo.getMonthValue(), proximo.getYear());
        redirectAttributes.addFlashAttribute("mensagem", "Gasto encerrado a partir do próximo mês!");
        return "redirect:/controle/" + pessoaId + "?mes=" + mes + "&ano=" + ano;
    }
}
