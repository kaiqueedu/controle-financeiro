package br.edu.ifsp.arq.dw2.controlefinanceiro.controller;

import java.util.Arrays;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import br.edu.ifsp.arq.dw2.controlefinanceiro.domain.model.Categoria;

@RestController
public class CategoriaController {

	@GetMapping("/categorias")
	public List<Categoria> listarCategorias(){				
		var cat = new Categoria();
		cat.setCodigo(1L);
		cat.setNome("Mercado");

		var cat2 = new Categoria();
		cat2.setCodigo(2L);
		cat2.setNome("Investimentos");
		
		return Arrays.asList(cat, cat2);
		
	}
	
}
