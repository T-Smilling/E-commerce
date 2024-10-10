package com.javaweb.controllers;

import com.javaweb.services.impl.MailService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/mail")
@SecurityRequirement(name = "E-Commerce")
@RequiredArgsConstructor
public class MailController {
    private final MailService mailService;

    @PostMapping("/send-email")
    public ResponseEntity<String> sendMail(@RequestParam String recipients,@RequestParam String subject,@RequestParam String content,@RequestParam(required = false) MultipartFile[] files) throws MessagingException {
        try {
            String responseMessage = mailService.sendEmail(recipients, subject, content, files);
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(responseMessage);
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Sending fail");
        }

    }
}
