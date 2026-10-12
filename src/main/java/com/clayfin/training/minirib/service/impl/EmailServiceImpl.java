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
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Map;

@Slf4j
@Service
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    private final String from;

    public EmailServiceImpl(JavaMailSender mailSender, TemplateEngine templateEngine,
                            @Value("${app.mail.from}") String from) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
        this.from = from;
    }

    @Override
    public void sendTemplateWithAttachment(String to, String subject, String templateName,
                                           Map<String, Object> variables, ReportFile attachment) {
        if (to == null || to.isBlank()) {
            log.warn("User has no registered email, {} not emailed", attachment.fileName());
            return;
        }
        try {
            // 1. HTML template + variables -> final HTML
            Context context = new Context();
            context.setVariables(variables);
            String html = templateEngine.process(templateName, context);

            // 2. build and send the email
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(html, true);                       // true = this is HTML
            helper.addAttachment(attachment.fileName(), new ByteArrayResource(attachment.content()),
                    attachment.contentType());
            mailSender.send(message);
            log.info("Email sent using template {} with {}", templateName, attachment.fileName());
        } catch (Exception e) {
            log.error("Could not send email with {}", attachment.fileName(), e);
        }
    }
}