package com.b1a4.cafeOn.report.dto;

import jakarta.validation.constraints.NotBlank;

public record ReportRequestDTO (
        @NotBlank String content
){}
