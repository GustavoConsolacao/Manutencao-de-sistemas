package com.example.SIST25.Chamado_Emergencia.models;

import com.example.SIST25.Chamado_Emergencia.enums.TipoUnidade;
import jakarta.persistence.*;

@Entity
@Table(name = "unidades")
public class Unidade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nome;

    @Column(name = "codigo_loja", nullable = false, unique = true, length = 20)
    private String codigoLoja;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoUnidade tipo;

    @Column(nullable = false, length = 100)
    private String cidade;

    @Column(nullable = false, length = 2)
    private String uf;

    @Column(nullable = false)
    private Boolean ativo = true;

    public Unidade() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getCodigoLoja() { return codigoLoja; }
    public void setCodigoLoja(String codigoLoja) { this.codigoLoja = codigoLoja; }
    public TipoUnidade getTipo() { return tipo; }
    public void setTipo(TipoUnidade tipo) { this.tipo = tipo; }
    public String getCidade() { return cidade; }
    public void setCidade(String cidade) { this.cidade = cidade; }
    public String getUf() { return uf; }
    public void setUf(String uf) { this.uf = uf; }
    public Boolean getAtivo() { return ativo; }
    public void setAtivo(Boolean ativo) { this.ativo = ativo; }
}
