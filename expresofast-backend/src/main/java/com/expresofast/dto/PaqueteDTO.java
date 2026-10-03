package com.expresofast.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record PaqueteDTO(
        @NotBlank @Size(max = 255) String descripcion,
        @NotNull @Positive @Digits(integer = 3, fraction = 2) BigDecimal pesoKg) {
}
