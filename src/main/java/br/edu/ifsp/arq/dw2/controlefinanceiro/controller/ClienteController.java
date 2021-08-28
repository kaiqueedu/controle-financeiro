package br.edu.ifsp.arq.dw2.controlefinanceiro.controller;

import java.util.Arrays;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import br.edu.ifsp.arq.dw2.controlefinanceiro.domain.model.Cliente;

@RestController
public class ClienteController {

	@GetMapping("/clientes")
	public List<Cliente> listarClientes(){				
		return Arrays.asList( new Cliente(1L, "Montoro", "mont@vesp.com", "33325555"),
							  new Cliente(2L, "Fritz", "masterfritz@fightclub.com", "123654789"), 
							  new Cliente(3L, "Grandma", "ustircamp@camp.com", "99964512"));
	}
	
}
