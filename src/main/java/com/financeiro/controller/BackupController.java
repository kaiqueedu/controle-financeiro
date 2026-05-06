package com.financeiro.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.financeiro.dto.BackupDTO;
import com.financeiro.service.BackupService;
import com.financeiro.service.PessoaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/backup")
@RequiredArgsConstructor
public class BackupController {

    private final BackupService backupService;
    private final PessoaService pessoaService;

    @GetMapping("/{pessoaId}/exportar")
    public ResponseEntity<byte[]> exportar(@PathVariable Long pessoaId) throws Exception {
        var backup = backupService.exportar(pessoaId);
        var pessoa = pessoaService.buscarPorId(pessoaId);

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        byte[] json = mapper.writeValueAsBytes(backup);

        String filename = "backup-" + pessoa.getNome().toLowerCase().replaceAll("\\s+", "-") + ".json";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_JSON)
                .body(json);
    }

    @GetMapping("/importar")
    public String formularioImportar(Model model) {
        return "backup/importar";
    }

    @PostMapping("/importar")
    public String importar(@RequestParam("arquivo") MultipartFile arquivo,
                            RedirectAttributes redirectAttributes) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());

            BackupDTO backup = mapper.readValue(arquivo.getInputStream(), BackupDTO.class);
            var pessoa = backupService.importar(backup);

            redirectAttributes.addFlashAttribute("mensagem",
                    "Dados importados com sucesso! Pessoa: " + pessoa.getNome());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro",
                    "Erro ao importar: " + e.getMessage());
        }
        return "redirect:/";
    }
}
