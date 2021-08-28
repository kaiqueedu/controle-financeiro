package br.edu.ifsp.arq.dw2.controlefinanceiro.domain.model;

import java.util.Objects;

public class Cliente {

	private Long codigo;
	private String nome;
	private String email;
	private String telefone;
	
	public Cliente() {}
	
	public Cliente(Long codigo, String nome, String email, String telefone) {
		this.codigo = codigo;
		this.nome = nome;
		this.email = email;
		this.telefone = telefone;
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
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getTelefone() {
		return telefone;
	}
	public void setTelefone(String telefone) {
		this.telefone = telefone;
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
		Cliente other = (Cliente) obj;
		return Objects.equals(codigo, other.codigo);
	}
	
}
