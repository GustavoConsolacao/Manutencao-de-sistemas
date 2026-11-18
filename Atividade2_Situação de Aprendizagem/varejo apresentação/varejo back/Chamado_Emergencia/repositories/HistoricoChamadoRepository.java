package com.example.SIST25.Chamado_Emergencia.repositories;

import com.example.SIST25.Chamado_Emergencia.models.HistoricoChamado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HistoricoChamadoRepository extends JpaRepository<HistoricoChamado, Long> {
    List<HistoricoChamado> findByChamadoIdOrderByDataRegistroDesc(Long chamadoId);
    List<HistoricoChamado> findByChamadoIdOrderByDataRegistroAsc(Long chamadoId);
    List<HistoricoChamado> findByUsuarioId(Long usuarioId);
}
