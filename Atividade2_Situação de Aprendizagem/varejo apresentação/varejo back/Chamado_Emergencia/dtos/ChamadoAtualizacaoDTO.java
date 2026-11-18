package com.example.SIST25.Chamado_Emergencia.dtos;

import com.example.SIST25.Chamado_Emergencia.enums.NivelSuporte;
import com.example.SIST25.Chamado_Emergencia.enums.StatusChamado;

public class ChamadoAtualizacaoDTO {

    private StatusChamado status;
    private NivelSuporte novoNivel;
    private String solucao;
    private Boolean suporteRemoto;
    private Long usuarioId;

    public ChamadoAtualizacaoDTO() {}

    public StatusChamado getStatus() { return status; }
    public void setStatus(StatusChamado status) { this.status = status; }
    public NivelSuporte getNovoNivel() { return novoNivel; }
    public void setNovoNivel(NivelSuporte novoNivel) { this.novoNivel = novoNivel; }
    public String getSolucao() { return solucao; }
    public void setSolucao(String solucao) { this.solucao = solucao; }
    public Boolean getSuporteRemoto() { return suporteRemoto; }
    public void setSuporteRemoto(Boolean suporteRemoto) { this.suporteRemoto = suporteRemoto; }
    public Long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }
}
