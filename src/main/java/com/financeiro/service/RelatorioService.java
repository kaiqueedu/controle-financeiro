package com.financeiro.service;

import com.financeiro.dto.AlertaGastoDTO;
import com.financeiro.dto.GastoMensalDTO;
import com.financeiro.dto.ResumoMensalDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RelatorioService {

    private final ReceitaService receitaService;
    private final GastoService gastoService;
    private final CartaoCreditoService cartaoService;

    private static final double PERCENTUAL_ALERTA = 15.0;

    public ResumoMensalDTO gerarResumo(Long pessoaId, int mes, int ano) {
        BigDecimal totalReceitas = receitaService.totalReceitas(pessoaId);
        BigDecimal totalFixos = gastoService.totalGastosFixos(pessoaId, mes, ano);
        BigDecimal totalVariaveis = gastoService.totalGastosVariaveis(pessoaId, mes, ano);
        BigDecimal totalCartoes = cartaoService.totalCartoesMensal(pessoaId, mes, ano);
        BigDecimal totalGastos = totalFixos.add(totalVariaveis).add(totalCartoes);
        BigDecimal totalPagos = gastoService.totalPagosMensal(pessoaId, mes, ano)
                .add(cartaoService.totalCartoesPagosMensal(pessoaId, mes, ano));
        BigDecimal saldo = totalReceitas.subtract(totalGastos);

        String nomeMes = Month.of(mes).getDisplayName(TextStyle.FULL, new Locale("pt", "BR"));

        return ResumoMensalDTO.builder()
                .mes(mes)
                .ano(ano)
                .nomeMes(nomeMes)
                .totalReceitas(totalReceitas)
                .totalGastosFixos(totalFixos)
                .totalGastosVariaveis(totalVariaveis)
                .totalCartoes(totalCartoes)
                .totalGastos(totalGastos)
                .totalPagos(totalPagos)
                .saldo(saldo)
                .build();
    }

    /**
     * Compara gastos individuais entre dois meses e retorna alertas
     * para os que subiram 15% ou mais.
     */
    public List<AlertaGastoDTO> gerarAlertas(Long pessoaId, int mes1, int ano1, int mes2, int ano2) {
        List<AlertaGastoDTO> alertas = new ArrayList<>();

        // Gastos fixos
        var fixosMes1 = gastoService.listarFixosPorMes(pessoaId, mes1, ano1);
        var fixosMes2 = gastoService.listarFixosPorMes(pessoaId, mes2, ano2);
        alertas.addAll(compararGastos(fixosMes1, fixosMes2, "Gasto Fixo"));

        // Gastos variáveis
        var varMes1 = gastoService.listarVariaveisPorMes(pessoaId, mes1, ano1);
        var varMes2 = gastoService.listarVariaveisPorMes(pessoaId, mes2, ano2);
        alertas.addAll(compararGastos(varMes1, varMes2, "Gasto Variável"));

        // Cartões (total por cartão)
        var cartoes = cartaoService.listarPorPessoa(pessoaId);
        for (var cartao : cartoes) {
            BigDecimal fatura1 = cartaoService.totalFaturaMensal(cartao.getId(), mes1, ano1);
            BigDecimal fatura2 = cartaoService.totalFaturaMensal(cartao.getId(), mes2, ano2);
            verificarAlerta(cartao.getNome(), "Cartão de Crédito", fatura1, fatura2, alertas);
        }

        return alertas;
    }

    private List<AlertaGastoDTO> compararGastos(List<GastoMensalDTO> gastosMes1,
                                                  List<GastoMensalDTO> gastosMes2,
                                                  String categoria) {
        List<AlertaGastoDTO> alertas = new ArrayList<>();

        // Mapeia gastos do mês 1 por ID
        Map<Long, GastoMensalDTO> mapMes1 = gastosMes1.stream()
                .collect(Collectors.toMap(GastoMensalDTO::getGastoId, g -> g, (a, b) -> a));

        // Para cada gasto do mês 2, verifica se existe no mês 1 e se subiu
        for (var gasto2 : gastosMes2) {
            var gasto1 = mapMes1.get(gasto2.getGastoId());
            if (gasto1 != null) {
                // Remove sufixo de parcela da descrição para exibição limpa
                String desc = gasto2.getDescricao().replaceAll("\\s*\\(\\d+/\\d+\\)$", "");
                verificarAlerta(desc, categoria, gasto1.getValor(), gasto2.getValor(), alertas);
            }
        }

        return alertas;
    }

    private void verificarAlerta(String descricao, String categoria,
                                  BigDecimal valorMes1, BigDecimal valorMes2,
                                  List<AlertaGastoDTO> alertas) {
        if (valorMes1 == null || valorMes2 == null) return;
        if (valorMes1.signum() == 0) return; // evita divisão por zero

        BigDecimal diferenca = valorMes2.subtract(valorMes1);
        if (diferenca.signum() <= 0) return; // só alertar aumentos

        double percentual = diferenca.multiply(BigDecimal.valueOf(100))
                .divide(valorMes1, 2, RoundingMode.HALF_UP)
                .doubleValue();

        if (percentual >= PERCENTUAL_ALERTA) {
            alertas.add(new AlertaGastoDTO(
                    descricao, categoria, valorMes1, valorMes2, diferenca, percentual));
        }
    }
}
