package org.example.gavebackend.services;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class MailService {

    private final RestTemplate restTemplate;

    @Value("${resend.api.key}")
    private String apiKey;

    @Value("${app.mail.from}")
    private String from;

    // Opcionales (si no los querés, los podés borrar y también borrar su uso)
    @Value("${app.mail.from-name:GaVe Store}")
    private String fromName;

    @Value("${app.mail.reply-to:}")
    private String replyTo;

    @Value("${app.mail.test-mode:false}")
    private boolean testMode;

    @Value("${app.mail.test-to:}")
    private String testTo;

    // ========= MÉTODOS PÚBLICOS =========

    /** Bienvenida luego de registrarse */
    public void sendWelcomeEmail(String to, String fullName) {
        String subject = "¡Bienvenido a Gave!";
        String html = buildWelcomeHtml(fullName)
                + "<hr><p style='font-size:12px;color:#666'>"
                + "Este email era para: <b>" + safe(to) + "</b></p>";

        String text = buildWelcomeText(fullName)
                + "\n\n---\nEste email era para: " + safe(to);

        send(to, subject, html, text);
    }

    /** Recuperación de contraseña con link y horas de expiración */
    public void sendPasswordResetEmail(String to, String fullName, String link, int hoursValid) {
        String subject = "Recuperá tu contraseña — Gave";
        String html = buildResetHtml(fullName, link, hoursValid)
                + "<hr><p style='font-size:12px;color:#666'>"
                + "Este email era para: <b>" + safe(to) + "</b></p>";

        String text = buildResetText(fullName, link, hoursValid)
                + "\n\n---\nEste email era para: " + safe(to);

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
            """.formatted(fullName == null ? "" : fullName, orderId, total.toPlainString())
                + "<hr><p style='font-size:12px;color:#666'>"
                + "Este email era para: <b>" + safe(to) + "</b></p>";

        String text = "Hola %s, recibimos tu pedido #%d. Total: $ %s"
                .formatted(fullName == null ? "" : fullName, orderId, total.toPlainString())
                + "\n\n---\nEste email era para: " + safe(to);

        send(to, subject, html, text);
    }

    // ========= MÉTODO GENÉRICO DE ENVÍO =========

    private void send(String to, String subject, String htmlBody, String textFallback) {
        // Validación mínima para no hacer requests rotos
        if (to == null || to.isBlank()) {
            log.warn("MailService: destinatario vacío. Asunto='{}' (no se envía).", safe(subject));
            return;
        }
        if (subject == null || subject.isBlank()) {
            subject = "(Sin asunto)";
        }
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("MailService: RESEND_API_KEY no configurada. No se envía mail a {}.", to);
            return;
        }
        if (from == null || from.isBlank()) {
            log.warn("MailService: MAIL_FROM no configurado. No se envía mail a {}.", to);
            return;
        }

        // Test mode: fuerza envío a testTo si está seteado
        String realTo = to;
        if (testMode) {
            if (testTo == null || testTo.isBlank()) {
                log.warn("MailService: app.mail.test-mode=true pero app.mail.test-to está vacío. No se envía.");
                return;
            }
            realTo = testTo.trim();
        }

        try {
            String url = "https://api.resend.com/emails";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            // From con nombre visible: "GaVe Store <no-reply@dominio>"
            String fromFormatted = (fromName != null && !fromName.isBlank())
                    ? fromName.trim() + " <" + from.trim() + ">"
                    : from.trim();

            // Body Resend
            Map<String, Object> body = Map.of(
                    "from", fromFormatted,
                    "to", List.of(realTo),
                    "subject", subject,
                    "html", htmlBody,
                    "text", textFallback
            );

            // Si querés reply_to, Resend lo soporta. Solo lo agregamos si viene seteado.
            if (replyTo != null && !replyTo.isBlank()) {
                body = new java.util.HashMap<>(body);
                ((java.util.HashMap<String, Object>) body).put("reply_to", replyTo.trim());
            }

            log.info("Enviando email por Resend -> to={}, subject='{}' (testMode={}, logicalTo={})",
                    realTo, subject, testMode, to);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("Resend OK -> status={}, body={}", response.getStatusCode(), response.getBody());
            } else {
                log.warn("Resend NO OK -> status={}, body={}", response.getStatusCode(), response.getBody());
            }

        } catch (HttpStatusCodeException e) {
            // Captura status + body real (clave para debug)
            log.warn("Resend ERROR HTTP -> status={}, body={}, to={}, subject='{}'",
                    e.getStatusCode(), e.getResponseBodyAsString(), realTo, subject);
        } catch (ResourceAccessException e) {
            // Timeout / DNS / red
            log.warn("Resend ERROR de conexión -> to={}, subject='{}': {}",
                    realTo, subject, e.getMessage());
        } catch (Exception e) {
            log.warn("Resend ERROR inesperado -> to={}, subject='{}': {}",
                    realTo, subject, e.getMessage());
        }
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }

    // ========= PLANTILLAS =========

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