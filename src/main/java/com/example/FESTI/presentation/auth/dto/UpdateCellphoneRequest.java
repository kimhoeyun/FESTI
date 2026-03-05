package com.example.FESTI.presentation.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record UpdateCellphoneRequest(
        @NotBlank(message = "cellphone is required")
        @Pattern(regexp = "^01\\d{8,9}$", message = "invalid cellphone format")
        String cellphone
) {
}
