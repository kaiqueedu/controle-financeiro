# Controle Financeiro Pessoal

Aplicação web para gerenciamento financeiro pessoal. Permite cadastrar receitas, gastos fixos, gastos variáveis, cartões de crédito e investimentos por pessoa, com visualização mensal e controle de pagamentos.

---

## Como Executar

### Pré-requisito
- Java 21 ou superior instalado

### Executar (distribuição)
1. Acesse a pasta `dist/`
2. Dê dois cliques em `iniciar.cmd`
3. O navegador abre automaticamente em `http://localhost:8080`

### Executar (desenvolvimento)
```
run.cmd
```

---

## Funcionalidades

### Cadastro de Pessoas
- Cada pessoa tem seu controle financeiro independente
- Acesso a: Controle Financeiro, Cartões de Crédito e Investimentos

### Receitas
- Cadastro de fontes de renda (salário, freelance, etc.)
- Fixas — aparecem automaticamente em todos os meses
- Informar dia do recebimento e valor
- Editável a qualquer momento

### Gastos Fixos
- Financiamentos, aluguel, seguros, etc.
- Aparecem em todos os meses automaticamente
- Valor editável por mês (histórico mantido)
- Botão "Encerrar" — para de aparecer nos meses seguintes sem apagar o histórico
- Controle de pagamento independente por mês

### Gastos Variáveis
Três modalidades:

| Modalidade | Comportamento |
|-----------|--------------|
| **Simples** | Aparece apenas no mês em que foi cadastrado |
| **Recorrente** | Aparece em todos os meses, valor editável por mês |
| **Parcelado** | Aparece nos próximos N meses (ex: 5x), com indicação de parcela |

- Valor editável individualmente por mês (sem afetar os outros)
- Controle de pagamento independente por mês

### Cartões de Crédito
- Cadastro de cartões (nome, bandeira, dia de vencimento)
- Gastos do cartão divididos em:
  - **Fixos mensais** — assinaturas, academia (aparecem em todas as faturas)
  - **Extras** — compras pontuais (aparecem só no mês)
  - **Parcelados** — compras parceladas com indicação de parcela (ex: 3/10)
- Total da fatura calculado automaticamente
- Controle de pagamento da fatura por mês
- Integrado ao painel principal (soma nos gastos totais)

### Investimentos
- Cadastro por pessoa com: descrição, banco/corretora, valor, data, tipo e observações
- Tipos: Renda Fixa, CDB, LCI/LCA, Tesouro Direto, Ações, FII, Crypto, Poupança, Previdência
- Resumo por banco/corretora com total investido
- Listagem completa ordenada por data

### Painel de Controle (visão mensal)
- Navegação entre meses com cor diferente por mês
- Cards de resumo: Total Receitas, Total Gastos, Total Pago, Saldo Previsto
- Seções expansíveis/retraíveis (Receitas, Fixos, Variáveis, Cartões)
- Indicador visual de vencido (vermelho) e pago (verde claro)
- Gastos pagos ficam com fundo verde e texto tachado

---

## Arquitetura Técnica

### Stack

| Camada | Tecnologia |
|--------|-----------|
| Linguagem | Java 21 |
| Framework | Spring Boot 3.3.5 |
| Persistência | Spring Data JPA + Hibernate 6 |
| Banco de dados | H2 (embarcado, arquivo local) |
| Front-end | Thymeleaf + Bootstrap 5.3 + Bootstrap Icons |
| Build | Maven (wrapper incluso) |

### Estrutura do Projeto

```
src/main/java/com/financeiro/
├── ControleFinanceiroApplication.java
├── model/
│   ├── Pessoa.java
│   ├── Receita.java
│   ├── Gasto.java
│   ├── TipoGasto.java (enum: FIXO, VARIAVEL)
│   ├── PagamentoMensal.java
│   ├── ValorMensalGasto.java
│   ├── CartaoCredito.java
│   ├── GastoCartao.java
│   ├── PagamentoCartaoMensal.java
│   └── Investimento.java
├── repository/
│   ├── PessoaRepository.java
│   ├── ReceitaRepository.java
│   ├── GastoRepository.java
│   ├── PagamentoMensalRepository.java
│   ├── ValorMensalGastoRepository.java
│   ├── CartaoCreditoRepository.java
│   ├── GastoCartaoRepository.java
│   ├── PagamentoCartaoMensalRepository.java
│   └── InvestimentoRepository.java
├── service/
│   ├── PessoaService.java
│   ├── ReceitaService.java
│   ├── GastoService.java
│   ├── CartaoCreditoService.java
│   └── InvestimentoService.java
├── controller/
│   ├── HomeController.java
│   ├── PessoaController.java
│   ├── ControleController.java
│   ├── CartaoController.java
│   └── InvestimentoController.java
└── dto/
    ├── GastoMensalDTO.java
    ├── GastoCartaoMensalDTO.java
    └── CartaoResumoDTO.java
```

### Modelo de Dados

```
Pessoa (1) ──── (N) Receita
       (1) ──── (N) Gasto ──── (N) PagamentoMensal
                         ──── (N) ValorMensalGasto
       (1) ──── (N) CartaoCredito ──── (N) GastoCartao
                                  ──── (N) PagamentoCartaoMensal
       (1) ──── (N) Investimento
```

### Decisões de Design

- **Pagamento por mês**: `PagamentoMensal` e `PagamentoCartaoMensal` rastreiam o status de pagamento de cada gasto/cartão em cada mês/ano de forma independente.
- **Valor por mês**: `ValorMensalGasto` permite que gastos fixos e recorrentes tenham valores diferentes por mês, mantendo histórico.
- **Encerramento**: campo `mesEncerramento/anoEncerramento` no `Gasto` permite desativar um gasto fixo sem perder o histórico.
- **Parcelamento**: campo `totalParcelas` + `mesOriginal/anoOriginal` calcula automaticamente em quais meses o gasto aparece e qual parcela é.
- **Banco H2 em arquivo**: dados persistidos em `data/financeiro.mv.db`, portátil entre máquinas.
- **Session tracking por cookie**: evita `jsessionid` na URL que quebrava redirects.

### Banco de Dados

- Arquivo: `data/financeiro.mv.db` (criado automaticamente)
- Console H2: `http://localhost:8080/h2-console`
  - JDBC URL: `jdbc:h2:file:./data/financeiro`
  - User: `sa` / Password: *(vazio)*

### Distribuição

A pasta `dist/` contém o JAR executável auto-suficiente (inclui Tomcat embarcado, todas as dependências). Basta Java 21+ instalado na máquina destino.

```
dist/
├── controle-financeiro-1.0.0.jar   (aplicação completa)
├── iniciar.cmd                      (inicia + abre navegador)
└── abrir-navegador.cmd              (auxiliar)
```

Para migrar dados entre máquinas: copiar o arquivo `data/financeiro.mv.db`.
