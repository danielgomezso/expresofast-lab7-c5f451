package com.expresofast.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record EnvioRegistroDTO(
        @NotBlank @Size(max = 30) @Pattern(regexp = "[A-Za-z0-9-]+") String numeroTracking,
        @NotBlank @Size(max = 100) String destinatario,
        @NotBlank @Size(max = 200) String direccionDestino,
        @NotNull @Positive @Digits(integer = 8, fraction = 2) BigDecimal montoFlete,
        @NotNull LocalDate fechaDespacho,
        @NotNull LocalDate fechaEntregaEstimada,
        @NotEmpty List<@NotNull @Valid PaqueteDTO> paquetes) {
}
