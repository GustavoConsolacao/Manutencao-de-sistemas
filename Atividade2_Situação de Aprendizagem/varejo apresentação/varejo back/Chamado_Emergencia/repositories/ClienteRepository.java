package com.example.SIST25.Chamado_Emergencia.repositories;

import com.example.SIST25.Chamado_Emergencia.models.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {
}
