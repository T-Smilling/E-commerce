package com.javaweb.services.impl;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import jakarta.validation.constraints.Email;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Service
@Slf4j
@RequiredArgsConstructor
public class MailService {
    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private SpringTemplateEngine templateEngine;

    @Value("${spring.mail.from}")
    private String emailFrom;

    public String sendEmail(String recipients, String subject, String content, MultipartFile[] files) throws MessagingException {
        log.info("Sending email...");
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage,true,"UTF-8");
        helper.setFrom(emailFrom);

        if (recipients.contains(",")){
            helper.setTo(InternetAddress.parse(recipients));
        } else {
            helper.setTo(recipients);
        }

        if (files != null) {
            for (MultipartFile file : files) {
                helper.addAttachment(Objects.requireNonNull(file.getOriginalFilename()),file);
            }
        }

        helper.setSubject(subject);
        helper.setText(content, true);

        mailSender.send(mimeMessage);

        log.info("Email has been send successfully, recipients ={}",recipients);
        return "sent";
    }

    public void sendConfirmLink(@Email String email, Long userId, String secretCode) throws MessagingException, UnsupportedEncodingException {
        log.info("Sending email confirm account...");

        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage,MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,"UTF-8");

        Context context = new Context();
        String linkConfirm = String.format("http://localhost:8085/api/v1/confirm/%d?secretCode=%s",userId,secretCode);

        Map<String,Object> properties = new HashMap<>();
        properties.put("linkConfirm",linkConfirm);

        context.setVariables(properties);
        helper.setFrom(emailFrom,"Chu Thắng");
        helper.setTo(email);
        helper.setSubject("Please confirm your account");

        String html = templateEngine.process("confirm-email.html",context);
        helper.setText(html, true);
        mailSender.send(mimeMessage);
        log.info("Email has been send successfully, recipients ={}",email);
    }

    @KafkaListener(topics = "confirm-account-topic", groupId = "confirm-account-group")
    public void sendConfirmLinkByKafka(String message) throws MessagingException, UnsupportedEncodingException {
        log.info("Sending link to user, email={}", message);

        String[] arr = message.split(",");
        String emailTo = arr[0].substring(arr[0].indexOf('=') + 1);
        String userId = arr[1].substring(arr[1].indexOf('=') + 1);
        String secretCode = arr[2].substring(arr[2].indexOf('=') + 1);

        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED, StandardCharsets.UTF_8.name());
        Context context = new Context();

        String linkConfirm = String.format("http://localhost:8085/api/v1/confirm/%s?secretCode=%s",userId,secretCode);

        Map<String, Object> properties = new HashMap<>();
        properties.put("linkConfirm", linkConfirm);
        context.setVariables(properties);

        helper.setFrom(emailFrom,"Chu Thắng");
        helper.setTo(emailTo);
        helper.setSubject("Please confirm your account");
        String html = templateEngine.process("confirm-email.html", context);
        helper.setText(html, true);

        mailSender.send(mimeMessage);
        log.info("Link has sent to user, email={}, linkConfirm={}", emailTo, linkConfirm);
    }
}
