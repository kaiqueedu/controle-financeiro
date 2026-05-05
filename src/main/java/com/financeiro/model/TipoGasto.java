package com.financeiro.model;

public enum TipoGasto {
    FIXO("Fixo"),
    VARIAVEL("Variável");

    private final String descricao;

    TipoGasto(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
