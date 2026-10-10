package com.clayfin.training.minirib.service.impl;

import com.clayfin.training.minirib.dto.response.ReportFile;
import com.clayfin.training.minirib.service.EmailService;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final String from;

    public EmailServiceImpl(JavaMailSender mailSender, @Value("${app.mail.from}") String from) {
        this.mailSender = mailSender;
        this.from = from;
    }

    @Override
    public void sendWithAttachment(String to, String subject, String body, ReportFile attachment) {
        if (to == null || to.isBlank()) {
            log.warn("User has no registered email, report {} not emailed", attachment.fileName());
            return;
        }
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body);
            helper.addAttachment(attachment.fileName(), new ByteArrayResource(attachment.content()),
                    attachment.contentType());
            mailSender.send(message);
            log.info("Report {} emailed to registered address", attachment.fileName());
        } catch (Exception e) {
            log.error("Could not send email with {}", attachment.fileName(), e);
        }
    }
}