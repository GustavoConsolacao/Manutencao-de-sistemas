package com.example.SIST25.Chamado_Emergencia;

import com.example.SIST25.Chamado_Emergencia.enums.TipoUnidade;
import com.example.SIST25.Chamado_Emergencia.models.Categoria;
import com.example.SIST25.Chamado_Emergencia.models.Unidade;
import com.example.SIST25.Chamado_Emergencia.repositories.CategoriaRepository;
import com.example.SIST25.Chamado_Emergencia.repositories.UnidadeRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Order(1)
public class CatalogoInitializer implements ApplicationRunner {

    private final UnidadeRepository unidadeRepository;
    private final CategoriaRepository categoriaRepository;

    public CatalogoInitializer(
            UnidadeRepository unidadeRepository,
            CategoriaRepository categoriaRepository
    ) {
        this.unidadeRepository = unidadeRepository;
        this.categoriaRepository = categoriaRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        criarUnidade("000", "Matriz", TipoUnidade.MATRIZ, "São Paulo", "SP");
        criarUnidade("001", "São Paulo · Centro", TipoUnidade.LOJA, "São Paulo", "SP");
        criarUnidade("002", "Rio de Janeiro · Copacabana", TipoUnidade.LOJA, "Rio de Janeiro", "RJ");
        criarUnidade("003", "Belo Horizonte · Savassi", TipoUnidade.LOJA, "Belo Horizonte", "MG");
        criarUnidade("004", "Curitiba · Batel", TipoUnidade.LOJA, "Curitiba", "PR");

        List.of(
                "PDV",
                "Internet",
                "Impressão",
                "Estoque",
                "Usuários",
                "Equipamentos da loja",
                "Wi-Fi",
                "Integração com matriz"
        ).forEach(this::criarCategoria);
    }

    private void criarUnidade(String codigo, String nome, TipoUnidade tipo, String cidade, String uf) {
        if (unidadeRepository.findByCodigoLoja(codigo).isPresent()) return;

        Unidade unidade = new Unidade();
        unidade.setCodigoLoja(codigo);
        unidade.setNome(nome);
        unidade.setTipo(tipo);
        unidade.setCidade(cidade);
        unidade.setUf(uf);
        unidade.setAtivo(true);
        unidadeRepository.save(unidade);
    }

    private void criarCategoria(String nome) {
        if (categoriaRepository.findByNome(nome).isPresent()) return;

        Categoria categoria = new Categoria();
        categoria.setNome(nome);
        categoriaRepository.save(categoria);
    }
}
