package org.example.gavebackend.services;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class MailService {

    private final JavaMailSender sender;

    @Value("${app.mail.from:}")
    private String from;

    public MailService(JavaMailSender sender) { this.sender = sender; }

    // ===== API PÚBLICA =====

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

    // ===== ENVÍO GENÉRICO =====

    private void send(String to, String subject, String htmlBody, String textFallback) {
        MimeMessage msg = sender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(msg, true, "UTF-8");
            if (from != null && !from.isBlank()) helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);
            // set texto como fallback y HTML como principal
            helper.setText(textFallback, htmlBody);
            sender.send(msg);
        } catch (MessagingException e) {
            // Podés loguear el error; no reventar flujo de negocio
            throw new RuntimeException("No se pudo enviar mail a " + to, e);
        }
    }

    // ===== TEMPLATES HTML / TEXTO =====

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
