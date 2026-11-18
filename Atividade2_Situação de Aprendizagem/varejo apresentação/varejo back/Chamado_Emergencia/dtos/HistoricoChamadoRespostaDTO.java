package com.example.SIST25.Chamado_Emergencia.dtos;

import java.time.LocalDateTime;

public record HistoricoChamadoRespostaDTO(LocalDateTime at, String user, String text) {}
