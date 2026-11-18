package com.example.SIST25.Chamado_Emergencia.controllers;

import com.example.SIST25.Chamado_Emergencia.dtos.ChamadoCriacaoDTO;
import com.example.SIST25.Chamado_Emergencia.dtos.ChamadoEscalonamentoDTO;
import com.example.SIST25.Chamado_Emergencia.dtos.ChamadoAtualizacaoDTO;
import com.example.SIST25.Chamado_Emergencia.dtos.EncerramentoChamadoDTO;
import com.example.SIST25.Chamado_Emergencia.dtos.HistoricoChamadoRespostaDTO;
import com.example.SIST25.Chamado_Emergencia.models.Chamado;
import com.example.SIST25.Chamado_Emergencia.repositories.HistoricoChamadoRepository;
import com.example.SIST25.Chamado_Emergencia.services.ChamadoService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chamados")
public class ChamadoController {

    @Autowired
    private ChamadoService chamadoService;

    @Autowired
    private HistoricoChamadoRepository historicoRepository;

    @GetMapping
    public ResponseEntity<?> listarTodos(HttpSession session) {
        Long usuarioId = obterUsuarioId(session);
        if (usuarioId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Faça login para consultar chamados.");
        return ResponseEntity.ok(chamadoService.listarTodos(usuarioId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> buscarPorId(@PathVariable Long id, HttpSession session) {
        Long usuarioId = obterUsuarioId(session);
        if (usuarioId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Faça login para consultar chamados.");
        try {
            Chamado chamado = chamadoService.buscarPorId(id, usuarioId);
            return ResponseEntity.ok(chamado);
        } catch (org.springframework.security.access.AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @GetMapping("/{id}/historico")
    public ResponseEntity<?> listarHistorico(@PathVariable Long id, HttpSession session) {
        Long usuarioId = obterUsuarioId(session);
        if (usuarioId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Faça login para consultar chamados.");
        try {
            chamadoService.buscarPorId(id, usuarioId);
            List<HistoricoChamadoRespostaDTO> historico = historicoRepository
                    .findByChamadoIdOrderByDataRegistroAsc(id)
                    .stream()
                    .map(item -> new HistoricoChamadoRespostaDTO(
                            item.getDataRegistro(),
                            item.getUsuario().getNome(),
                            item.getObservacao() == null
                                    ? (item.getMotivo() == null ? item.getAcao() : item.getMotivo())
                                    : item.getObservacao()
                    ))
                    .toList();
            return ResponseEntity.ok(historico);
        } catch (org.springframework.security.access.AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<?> criar(@RequestBody ChamadoCriacaoDTO dto, HttpSession session) {
        Long usuarioId = obterUsuarioId(session);
        if (usuarioId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Faça login para criar chamados.");
        dto.setSolicitanteId(usuarioId);
        try {
            Chamado novoChamado = chamadoService.criarChamado(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(novoChamado);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}/escalar")
    public ResponseEntity<?> escalar(@PathVariable Long id, @RequestBody ChamadoEscalonamentoDTO dto, HttpSession session) {
        Long usuarioId = obterUsuarioId(session);
        if (usuarioId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Faça login para alterar chamados.");
        dto.setUsuarioId(usuarioId);
        try {
            Chamado chamadoEscalado = chamadoService.escalarChamado(id, dto);
            return ResponseEntity.ok(chamadoEscalado);
        } catch (org.springframework.security.access.AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> atualizar(@PathVariable Long id, @RequestBody ChamadoAtualizacaoDTO dto, HttpSession session) {
        Long usuarioId = obterUsuarioId(session);
        if (usuarioId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Faça login para alterar chamados.");
        dto.setUsuarioId(usuarioId);
        try {
            return ResponseEntity.ok(chamadoService.atualizarChamado(id, dto));
        } catch (org.springframework.security.access.AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @PutMapping("/{id}/encerrar")
    public ResponseEntity<?> encerrar(@PathVariable Long id, @RequestBody EncerramentoChamadoDTO dto, HttpSession session) {
        Long usuarioId = obterUsuarioId(session);
        if (usuarioId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Faça login para encerrar chamados.");
        dto.setUsuarioId(usuarioId);
        try {
            Chamado chamadoEncerrado = chamadoService.encerrarChamado(id, dto);
            return ResponseEntity.ok(chamadoEncerrado);
        } catch (org.springframework.security.access.AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    private Long obterUsuarioId(HttpSession session) {
        Object usuarioId = session.getAttribute("usuarioId");
        return usuarioId instanceof Long id ? id : null;
    }
}