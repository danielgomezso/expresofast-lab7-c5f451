package com.expresofast.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record EnvioDTO(Integer id, String codigoRastreo, String destinatario,
        String direccionDestino, BigDecimal montoFlete, String estado, LocalDateTime fechaCreacion) {
}
