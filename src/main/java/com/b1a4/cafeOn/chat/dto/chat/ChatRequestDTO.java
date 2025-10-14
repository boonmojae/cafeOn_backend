package com.b1a4.cafeOn.chat.dto.chat;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatRequestDTO {

    @Size(max = 1000)
    private String message;

//    @Size(max = 500)
//    private String imageUrl;

//    @AssertTrue(message = "message 또는 imageUrl 중 하나는 반드시 필요합니다.")
//    public boolean hasAtLeastOneContent() {
//        boolean hasMsg = message != null && !message.isBlank();
//        boolean hasImg = imageUrl != null && !imageUrl.isBlank();
//        return hasMsg || hasImg;
//    }

}
