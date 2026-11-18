package com.example.SIST25.Chamado_Emergencia.services;

import com.example.SIST25.Chamado_Emergencia.dtos.ChamadoCriacaoDTO;
import com.example.SIST25.Chamado_Emergencia.dtos.ChamadoAtualizacaoDTO;
import com.example.SIST25.Chamado_Emergencia.dtos.ChamadoEscalonamentoDTO;
import com.example.SIST25.Chamado_Emergencia.dtos.EncerramentoChamadoDTO;
import com.example.SIST25.Chamado_Emergencia.enums.*;
import com.example.SIST25.Chamado_Emergencia.models.*;
import com.example.SIST25.Chamado_Emergencia.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
public class ChamadoService {

    @Autowired private ChamadoRepository chamadoRepository;
    @Autowired private UnidadeRepository unidadeRepository;
    @Autowired private CategoriaRepository categoriaRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private HistoricoChamadoRepository historicoRepository;

    public List<Chamado> listarTodos() {
        return chamadoRepository.findAll();
    }

    public List<Chamado> listarTodos(Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        if (usuario.getPerfil() == Perfil.SOLICITANTE) {
            return chamadoRepository.findBySolicitanteId(usuarioId);
        }
        return chamadoRepository.findAll();
    }

    public Chamado buscarPorId(Long id, Long usuarioId) {
        Chamado chamado = buscarPorId(id);
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        if (usuario.getPerfil() == Perfil.SOLICITANTE
                && !chamado.getSolicitante().getId().equals(usuarioId)) {
            throw new AccessDeniedException("Você não pode consultar este chamado.");
        }
        return chamado;
    }

    public Chamado buscarPorId(Long id) {
        return chamadoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Chamado não encontrado com o ID: " + id));
    }

    @Transactional
    public Chamado criarChamado(ChamadoCriacaoDTO dto) {
        Usuario solicitante = usuarioRepository.findById(dto.getSolicitanteId())
                .orElseThrow(() -> new RuntimeException("Solicitante não encontrado"));
        Unidade unidade = solicitante.getPerfil() == Perfil.SOLICITANTE
                ? solicitante.getUnidade()
                : unidadeRepository.findById(dto.getUnidadeId())
                        .orElseThrow(() -> new RuntimeException("Unidade não encontrada"));
        Categoria categoria = categoriaRepository.findById(dto.getCategoriaId())
                .orElseThrow(() -> new RuntimeException("Categoria não encontrada"));

        Chamado chamado = new Chamado();
        chamado.setTitulo(dto.getTitulo());
        chamado.setDescricao(dto.getDescricao());
        chamado.setTipo(dto.getTipo());
        chamado.setImpacto(dto.getImpacto());
        chamado.setUrgencia(dto.getUrgencia());
        chamado.setUnidade(unidade);
        chamado.setCategoria(categoria);
        chamado.setSolicitante(solicitante);
        chamado.setSuporteRemoto(dto.getSuporteRemoto());
        chamado.setStatus(StatusChamado.NOVO);
        chamado.setNivelSuporte(NivelSuporte.N1);

        Prioridade prioridade = calcularPrioridade(dto.getImpacto(), dto.getUrgencia(), categoria, unidade);
        chamado.setPrioridade(prioridade);
        chamado.setPrazoLimiteSLA(calcularPrazoSLA(prioridade));

        Chamado chamadoSalvo = chamadoRepository.save(chamado);
        registrarHistorico(chamadoSalvo, solicitante, "CRIACAO", "Chamado aberto no sistema", null, null);

        return chamadoSalvo;
    }

    @Transactional
    public Chamado escalarChamado(Long id, ChamadoEscalonamentoDTO dto) {
        validarTecnico(dto.getUsuarioId());
        Chamado chamado = buscarPorId(id);
        Usuario usuario = usuarioRepository.findById(dto.getUsuarioId())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        NivelSuporte nivelOrigem = chamado.getNivelSuporte();
        chamado.setNivelSuporte(dto.getNovoNivel());
        chamado.setStatus(StatusChamado.ESCALADO);
        chamado.setTecnicoAtribuido(null);

        Chamado chamadoSalvo = chamadoRepository.save(chamado);
        registrarHistorico(chamadoSalvo, usuario, "ESCALONAMENTO", dto.getMotivo(), nivelOrigem, dto.getNovoNivel());

        return chamadoSalvo;
    }

    @Transactional
    public Chamado encerrarChamado(Long id, EncerramentoChamadoDTO dto) {
        validarTecnico(dto.getUsuarioId());
        Chamado chamado = buscarPorId(id);
        Usuario usuario = usuarioRepository.findById(dto.getUsuarioId())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        if (dto.getSolucao() == null || dto.getSolucao().trim().isEmpty()) {
            throw new IllegalArgumentException("Erro: É obrigatório registrar a solução antes de encerrar o chamado.");
        }

        chamado.setSolucaoRegistrada(dto.getSolucao());
        chamado.setStatus(StatusChamado.FECHADO);
        chamado.setDataEnvolvimento(LocalDateTime.now());

        Chamado chamadoSalvo = chamadoRepository.save(chamado);
        registrarHistorico(chamadoSalvo, usuario, "ENCERRAMENTO", "Solução: " + dto.getSolucao(), null, null);

        return chamadoSalvo;
    }

    @Transactional
    public Chamado atualizarChamado(Long id, ChamadoAtualizacaoDTO dto) {
        validarTecnico(dto.getUsuarioId());
        Chamado chamado = buscarPorId(id);
        Usuario usuario = usuarioRepository.findById(dto.getUsuarioId())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        String solucao = dto.getSolucao() == null ? "" : dto.getSolucao().trim();
        StatusChamado novoStatus = dto.getStatus();
        if (novoStatus == null) {
            throw new IllegalArgumentException("O status do chamado é obrigatório.");
        }
        if ((novoStatus == StatusChamado.RESOLVIDO || novoStatus == StatusChamado.FECHADO)
                && solucao.isEmpty()) {
            throw new IllegalArgumentException("Registre a solução antes de resolver ou encerrar o chamado.");
        }

        NivelSuporte nivelAnterior = chamado.getNivelSuporte();
        if (dto.getNovoNivel() != null && dto.getNovoNivel() != nivelAnterior) {
            chamado.setNivelSuporte(dto.getNovoNivel());
            chamado.setTecnicoAtribuido(null);
            registrarHistorico(chamado, usuario, "ESCALONAMENTO",
                    "Chamado escalonado de " + nivelAnterior + " para " + dto.getNovoNivel(),
                    nivelAnterior, dto.getNovoNivel());
        }

        if (chamado.getStatus() != novoStatus) {
            StatusChamado statusAnterior = chamado.getStatus();
            chamado.setStatus(novoStatus);
            registrarHistorico(chamado, usuario, "ATUALIZACAO",
                    "Status alterado de " + statusAnterior + " para " + novoStatus, null, null);
        }
        if (!solucao.isEmpty() && !solucao.equals(chamado.getSolucaoRegistrada())) {
            chamado.setSolucaoRegistrada(solucao);
            registrarHistorico(chamado, usuario, "SOLUCAO", "Solução: " + solucao, null, null);
        }
        if (dto.getSuporteRemoto() != null
                && !dto.getSuporteRemoto().equals(chamado.getSuporteRemoto())) {
            chamado.setSuporteRemoto(dto.getSuporteRemoto());
            registrarHistorico(chamado, usuario, "SUPORTE_REMOTO",
                    dto.getSuporteRemoto() ? "Suporte remoto autorizado." : "Suporte remoto desativado.",
                    null, null);
        }

        return chamadoRepository.save(chamado);
    }

    private void validarTecnico(Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        if (usuario.getPerfil() == Perfil.SOLICITANTE) {
            throw new AccessDeniedException("Somente a equipe de suporte pode alterar chamados.");
        }
    }

    private Prioridade calcularPrioridade(Impacto impacto, Urgencia urgencia, Categoria categoria, Unidade unidade) {
        LocalTime agora = LocalTime.now();
        boolean horarioComercial = agora.isAfter(LocalTime.of(8, 0)) && agora.isBefore(LocalTime.of(19, 0));

        if (unidade.getTipo() == TipoUnidade.LOJA && horarioComercial) {
            String nomeCat = categoria.getNome().toUpperCase();
            if (nomeCat.contains("PDV") || nomeCat.contains("INTERNET")) {
                return Prioridade.CRITICA;
            }
        }

        if (impacto == Impacto.ALTO && urgencia == Urgencia.ALTA) return Prioridade.CRITICA;
        if (impacto == Impacto.ALTO || urgencia == Urgencia.ALTA) return Prioridade.ALTA;
        if (impacto == Impacto.MEDIO && urgencia == Urgencia.MEDIA) return Prioridade.MEDIA;

        return Prioridade.BAIXA;
    }

    private LocalDateTime calcularPrazoSLA(Prioridade prioridade) {
        LocalDateTime agora = LocalDateTime.now();
        return switch (prioridade) {
            case CRITICA -> agora.plusHours(2);
            case ALTA    -> agora.plusHours(8);
            case MEDIA   -> agora.plusHours(24);
            case BAIXA   -> agora.plusHours(48);
        };
    }

    private void registrarHistorico(Chamado chamado, Usuario usuario, String acao, String observacao, NivelSuporte origem, NivelSuporte destino) {
        HistoricoChamado hist = new HistoricoChamado();
        hist.setChamado(chamado);
        hist.setUsuario(usuario);
        hist.setAcao(acao);
        hist.setMotivo(observacao == null || observacao.isBlank() ? acao : observacao);
        hist.setObservacao(observacao);
        hist.setNivelOrigem(origem);
        hist.setNivelDestino(destino);
        historicoRepository.save(hist);
    }
}
