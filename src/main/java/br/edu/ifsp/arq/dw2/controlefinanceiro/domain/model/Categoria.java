package br.edu.ifsp.arq.dw2.controlefinanceiro.domain.model;

import java.util.Objects;

public class Categoria {

	private Long codigo;
	private String nome;
	
	public Categoria(Long codigo, String nome) {
		super();
		this.codigo = codigo;
		this.nome = nome;
	}
	
	public Categoria() {
		
	}
	
	public Long getCodigo() {
		return codigo;
	}
	public void setCodigo(Long codigo) {
		this.codigo = codigo;
	}
	public String getNome() {
		return nome;
	}
	public void setNome(String nome) {
		this.nome = nome;
	}

	@Override
	public int hashCode() {
		return Objects.hash(codigo);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Categoria other = (Categoria) obj;
		return Objects.equals(codigo, other.codigo);
	}
	
}
