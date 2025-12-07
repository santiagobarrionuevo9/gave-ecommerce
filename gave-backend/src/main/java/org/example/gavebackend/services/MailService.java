package org.example.gavebackend.services;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class MailService {

    private final JavaMailSender sender;

    @Value("${app.mail.from:}")
    private String from;

    // ===================== PÚBLICOS =====================

    /** Bienvenida luego de registrarse */
    public void sendWelcomeEmail(String to, String fullName) {
        String subject = "¡Bienvenido a Gave!";
        String html = buildWelcomeHtml(fullName);
        String text = buildWelcomeText(fullName);
        send(to, subject, html, text);
    }

    /** Recuperación de contraseña con link y horas de expiración */
    public void sendPasswordResetEmail(String to, String fullName, String link, int hoursValid) {
        String subject = "Recuperá tu contraseña — Gave";
        String html = buildResetHtml(fullName, link, hoursValid);
        String text = buildResetText(fullName, link, hoursValid);
        send(to, subject, html, text);
    }

    /** Confirmación de pedido */
    public void sendOrderConfirmationEmail(String to, String fullName, Long orderId, BigDecimal total) {
        String subject = "Tu pedido #" + orderId + " fue recibido";
        String html = """
            <div style="font-family:Arial,Helvetica,sans-serif;line-height:1.5">
              <h2>¡Gracias por tu pedido!</h2>
              <p>Hola %s, recibimos tu pedido <b>#%d</b>.</p>
              <p>Total: <b>$ %s</b></p>
              <p>Te avisaremos cuando el estado cambie.</p>
            </div>
            """.formatted(fullName == null ? "" : fullName, orderId, total.toPlainString());

        String text = "Hola %s, recibimos tu pedido #%d. Total: $ %s"
                .formatted(fullName == null ? "" : fullName, orderId, total.toPlainString());

        send(to, subject, html, text);
    }

    // ===================== PRIVADO GENÉRICO =====================

    /** Envío genérico de email con HTML y texto plano */
    private void send(String to, String subject, String htmlBody, String textFallback) {
        try {
            log.info("Preparando email a {} con asunto '{}'", to, subject);

            MimeMessage msg = sender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(msg, true, "UTF-8");

            // FROM: si viene de properties lo usamos, si no, que use el spring.mail.username
            if (from != null && !from.isBlank()) {
                helper.setFrom(from);
            }

            helper.setTo(to);
            helper.setSubject(subject);
            // texto plano (fallback) + HTML principal
            helper.setText(textFallback, htmlBody);

            sender.send(msg);
            log.info("Email enviado a {} con asunto '{}'", to, subject);
        } catch (Exception e) {
            // NO lanzamos RuntimeException: solo logueamos
            log.warn("No se pudo enviar mail a {} con asunto '{}': {}", to, subject, e.getMessage());
        }
    }

    // ===================== PLANTILLAS =====================

    /** Construye el cuerpo HTML del email de bienvenida */
    private String buildWelcomeHtml(String name) {
        String safe = name != null && !name.isBlank() ? name : "¡Hola!";
        return """
          <div style="font-family:Arial,Helvetica,sans-serif;line-height:1.5">
            <h2>¡Bienvenido a <span style="color:#2a7ae4">Gave</span>!</h2>
            <p>%s</p>
            <p>Gracias por registrarte. Ya podés iniciar sesión y explorar nuestros productos.</p>
            <p style="margin-top:24px;color:#666">¡Que lo disfrutes!<br/>Equipo Gave</p>
          </div>
          """.formatted("Hola " + safe + ",");
    }

    /** Construye el cuerpo de texto plano del email de bienvenida */
    private String buildWelcomeText(String name) {
        String safe = name != null && !name.isBlank() ? name : "";
        return """
          Hola %s,

          ¡Bienvenido a Gave!
          Gracias por registrarte. Ya podés iniciar sesión y explorar nuestros productos.

          ¡Que lo disfrutes!
          Equipo Gave
          """.formatted(safe);
    }

    /** Construye el cuerpo HTML del email de reseteo de contraseña */
    private String buildResetHtml(String name, String link, int hours) {
        String safe = (name != null && !name.isBlank()) ? name : "";
        return """
          <div style="font-family:Arial,Helvetica,sans-serif;line-height:1.5">
            <h2>Recuperación de contraseña</h2>
            <p>Hola %s,</p>
            <p>Recibimos un pedido para recuperar tu contraseña.</p>
            <p>
              <a href="%s" style="background:#2a7ae4;color:#fff;padding:10px 16px;
                 text-decoration:none;border-radius:6px;display:inline-block">
                Crear nueva contraseña
              </a>
            </p>
            <p>El enlace vence en <b>%d horas</b>. Si no fuiste vos, ignorá este mensaje.</p>
            <p style="margin-top:24px;color:#666">Saludos,<br/>Equipo Gave</p>
          </div>
          """.formatted(safe, link, hours);
    }

    /** Construye el cuerpo de texto plano del email de reseteo de contraseña */
    private String buildResetText(String name, String link, int hours) {
        String safe = name != null && !name.isBlank() ? name : "";
        return """
          Hola %s,

          Recibimos un pedido para recuperar tu contraseña.
          Usá este enlace (vence en %d horas):
          %s

          Si no fuiste vos, ignorá este mensaje.
          Saludos,
          Equipo Gave
          """.formatted(safe, hours, link);
    }
}