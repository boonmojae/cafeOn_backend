package com.b1a4.cafeOn.user.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;

@Service
@RequiredArgsConstructor
public class EmailService {
    private final JavaMailSender mailSender;

    public void sendTempPasswordEmail(String to, String tempPassword) throws MessagingException, UnsupportedEncodingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setTo(to);
        helper.setSubject("[CafeOn] 임시 비밀번호 안내");
        String content = "안녕하세요. CafeOn입니다.<br><br>"
                + "임시 비밀번호는 다음과 같습니다:<br><b>" + tempPassword + "</b><br><br>"
                + "로그인 후 반드시 비밀번호를 변경해주세요.";

        helper.setText(content, true);  // 두 번째 인자 true -> HTML 형식
        helper.setFrom("cafeonteam@gmail.com", "CafeOn 운영팀");

        mailSender.send(message);
    }
}