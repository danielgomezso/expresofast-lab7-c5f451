package com.expresofast.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record CrearEnvioDTO(
        @NotBlank @Size(max = 100) String destinatario,
        @NotBlank @Size(max = 200) String direccionDestino,
        @NotNull @Positive @Digits(integer = 8, fraction = 2) BigDecimal montoFlete) {
}
