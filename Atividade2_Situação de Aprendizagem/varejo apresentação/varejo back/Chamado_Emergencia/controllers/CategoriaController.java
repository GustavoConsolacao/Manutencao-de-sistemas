package com.example.SIST25.Chamado_Emergencia.controllers;

import com.example.SIST25.Chamado_Emergencia.models.Categoria;
import com.example.SIST25.Chamado_Emergencia.repositories.CategoriaRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/categorias")
public class CategoriaController {

    private final CategoriaRepository categoriaRepository;

    public CategoriaController(CategoriaRepository categoriaRepository) {
        this.categoriaRepository = categoriaRepository;
    }

    @GetMapping
    public List<Categoria> listarTodas() {
        return categoriaRepository.findAll();
    }
}
