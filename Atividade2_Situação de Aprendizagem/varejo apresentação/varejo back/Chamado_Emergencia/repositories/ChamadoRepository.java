package com.example.SIST25.Chamado_Emergencia.repositories;

import com.example.SIST25.Chamado_Emergencia.enums.NivelSuporte;
import com.example.SIST25.Chamado_Emergencia.enums.StatusChamado;
import com.example.SIST25.Chamado_Emergencia.models.Chamado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChamadoRepository extends JpaRepository<Chamado, Long> {
    List<Chamado> findByUnidadeId(Long unidadeId);
    List<Chamado> findBySolicitanteId(Long solicitanteId);
    List<Chamado> findByNivelSuporte(NivelSuporte nivelSuporte);
    List<Chamado> findByStatus(StatusChamado status);
}
