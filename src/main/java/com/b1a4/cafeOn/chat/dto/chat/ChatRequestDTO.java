package com.b1a4.cafeOn.chat.dto.chat;

import jakarta.validation.constraints.Size;

public record ChatRequestDTO(

        @Size(max = 1000)
        String message

) {
}



