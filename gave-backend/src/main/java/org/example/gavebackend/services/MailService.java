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


    // ========= CONSTANTES DE NEGOCIO =========
    private static final String SELLER_EMAIL = "gavefiltros@gmail.com";
    private static final String WHATSAPP_NUMBER = "5493512449110";
    private static final String WHATSAPP_LINK = "https://wa.me/" + WHATSAPP_NUMBER;

    // ========= MÉTODOS PÚBLICOS =========

    /** Bienvenida luego de registrarse */
    public void sendWelcomeEmail(String to, String fullName) {
        String subject = "¡Bienvenido a GaVe Store!";

        String name = (fullName != null && !fullName.isBlank()) ? fullName : "¡Hola!";

        String body = """
          <p style="margin-top:0">Hola <b>%s</b>,</p>
          <p>Gracias por registrarte. Ya podés iniciar sesión y explorar nuestro catálogo.</p>
        """.formatted(name);

        body += softCard("""
          <div style="font-weight:800;color:#0f766e;font-size:14px;margin-bottom:8px">¿Necesitás ayuda?</div>
          <div style="color:#374151;font-size:13px;line-height:1.55">
            Podés escribirnos por WhatsApp para consultas sobre productos, stock o pedidos.
          </div>
        """);

        body += """
          <div style="margin-top:18px">%s</div>
        """.formatted(primaryButton("Contactar por WhatsApp", WHATSAPP_LINK));

        String html = wrapWithLayout(
                "Cuenta creada con éxito",
                "Bienvenido a GaVe Store",
                body,
                to
        );

        String text = """
        Hola %s,

        ¡Bienvenido a GaVe Store!
        Gracias por registrarte. Ya podés iniciar sesión y explorar el catálogo.

        WhatsApp: %s
        Email vendedor: %s

        Equipo GaVe Store
        """.formatted(name, WHATSAPP_NUMBER, SELLER_EMAIL);

        send(to, subject, html, text);
    }

    /** Recuperación de contraseña con link y horas de expiración */
    public void sendPasswordResetEmail(String to, String fullName, String link, int hoursValid) {
        String subject = "Recuperá tu contraseña — GaVe Store";

        String name = (fullName != null && !fullName.isBlank()) ? fullName : "cliente";

        String body = """
          <p style="margin-top:0">Hola <b>%s</b>,</p>
          <p>Recibimos una solicitud para restablecer tu contraseña.</p>
          <p style="margin:14px 0">%s</p>
          <p style="margin:12px 0 0 0;color:#374151;font-size:13px;line-height:1.55">
            El enlace vence en <b>%d horas</b>. Si no fuiste vos, ignorá este mensaje.
          </p>
        """.formatted(name, primaryButton("Crear nueva contraseña", link), hoursValid);

        body += mutedNote("Si tenés problemas para acceder, podés contactarnos por WhatsApp.");

        body += """
          <div style="margin-top:18px">%s</div>
        """.formatted(primaryButton("Soporte por WhatsApp", WHATSAPP_LINK));

        String html = wrapWithLayout(
                "Recuperación de contraseña",
                "Acceso seguro",
                body,
                to
        );

        String text = """
        Hola %s,

        Usá este enlace para crear una nueva contraseña (vence en %d horas):
        %s

        Si no fuiste vos, ignorá este mensaje.

        Soporte WhatsApp: %s
        Email vendedor: %s

        Equipo GaVe Store
        """.formatted(name, hoursValid, link, WHATSAPP_NUMBER, SELLER_EMAIL);

        send(to, subject, html, text);
    }

    /** Confirmación de pedido */
    public void sendOrderConfirmationEmail(String to, String fullName, Long orderId, BigDecimal total) {
        String subject = "Tu pedido #" + orderId + " fue recibido — GaVe Store";

        String name = (fullName != null && !fullName.isBlank()) ? fullName : "cliente";

        String body = """
          <p style="margin-top:0">Hola <b>%s</b>,</p>
          <p>Recibimos correctamente tu pedido <b>#%d</b>.</p>
        """.formatted(name, orderId);

        body += softCard("""
          <div style="font-size:13px;color:#065f46;font-weight:700;margin-bottom:6px">TOTAL DEL PEDIDO</div>
          <div style="font-size:24px;color:#0f766e;font-weight:800">$ %s</div>
        """.formatted(total != null ? total.toPlainString() : "0"));

        body += """
          <p style="margin:16px 0 6px 0"><b>Estado del pedido</b></p>
          <p style="margin:0">
            El estado del pedido se <b>actualiza y coordina por WhatsApp</b> con el cliente para la entrega / retiro.
          </p>
        """;

        body += softCard("""
          <div style="font-weight:800;color:#92400e;font-size:14px;margin-bottom:8px">Importante</div>
          <div style="color:#374151;font-size:13px;line-height:1.55">
            Los pagos se realizan <b>únicamente en efectivo</b>. Actualmente no se emite factura.
            Confirmamos y coordinamos entrega/retiro por WhatsApp.
          </div>
        """);

        body += mutedNote("""
          <b>Contacto oficial:</b><br/>
          WhatsApp: <b>%s</b><br/>
          Email vendedor: <b>%s</b>
        """.formatted(WHATSAPP_NUMBER, SELLER_EMAIL));

        body += """
          <div style="margin-top:18px">%s</div>
        """.formatted(primaryButton("Hablar por WhatsApp", WHATSAPP_LINK));

        String html = wrapWithLayout(
                "Pedido recibido",
                "Confirmación de compra",
                body,
                to
        );

        String text = """
        Hola %s,

        Recibimos tu pedido #%d.
        Total: $ %s

        El estado del pedido y la coordinación de entrega/retiro
        se realizan por WhatsApp con el cliente.

        WhatsApp: %s
        Email vendedor: %s
        Link: %s

        Equipo GaVe Store
        """.formatted(name, orderId, (total != null ? total.toPlainString() : "0"), WHATSAPP_NUMBER, SELLER_EMAIL, WHATSAPP_LINK);

        send(to, subject, html, text);
    }

    // ========= MÉTODO GENÉRICO DE ENVÍO (RESEND) =========

    private void send(String to, String subject, String htmlBody, String textFallback) {
        if (to == null || to.isBlank()) {
            log.warn("MailService: destinatario vacío. Asunto='{}' (no se envía).", safe(subject));
            return;
        }
        if (subject == null || subject.isBlank()) subject = "(Sin asunto)";

        if (apiKey == null || apiKey.isBlank()) {
            log.warn("MailService: RESEND_API_KEY no configurada. No se envía mail a {}.", to);
            return;
        }
        if (from == null || from.isBlank()) {
            log.warn("MailService: MAIL_FROM no configurado. No se envía mail a {}.", to);
            return;
        }

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

            String fromFormatted = (fromName != null && !fromName.isBlank())
                    ? fromName.trim() + " <" + from.trim() + ">"
                    : from.trim();

            Map<String, Object> body = Map.of(
                    "from", fromFormatted,
                    "to", List.of(realTo),
                    "subject", subject,
                    "html", htmlBody,
                    "text", textFallback
            );

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
            log.warn("Resend ERROR HTTP -> status={}, body={}, to={}, subject='{}'",
                    e.getStatusCode(), e.getResponseBodyAsString(), realTo, subject);
        } catch (ResourceAccessException e) {
            log.warn("Resend ERROR de conexión -> to={}, subject='{}': {}",
                    realTo, subject, e.getMessage());
        } catch (Exception e) {
            log.warn("Resend ERROR inesperado -> to={}, subject='{}': {}",
                    realTo, subject, e.getMessage());
        }
    }

    // ========= LAYOUT / UI HELPERS =========

    private String wrapWithLayout(String title, String subtitle, String bodyHtml, String toEmail) {
        return """
        <div style="background:#f4f6f8;padding:24px;font-family:Arial,Helvetica,sans-serif">
          <div style="max-width:640px;margin:0 auto;background:#ffffff;
                      border-radius:12px;overflow:hidden;
                      box-shadow:0 6px 18px rgba(0,0,0,.08)">

            <div style="background:#0f766e;color:#ffffff;padding:22px 22px 18px 22px">
              <div style="font-size:20px;font-weight:700;letter-spacing:.2px">GaVe Store</div>
              <div style="margin-top:6px;font-size:14px;opacity:.92">%s</div>
              <div style="margin-top:2px;font-size:12px;opacity:.85">%s</div>
            </div>

            <div style="padding:24px 22px;color:#111827">
              %s
            </div>

            <div style="background:#f9fafb;padding:14px 16px;text-align:center;
                        font-size:12px;color:#6b7280;border-top:1px solid #eef2f7">
              Consultas: <a href="mailto:%s" style="color:#0f766e;text-decoration:none;font-weight:600">%s</a>
              &nbsp;•&nbsp;
              WhatsApp: <a href="%s" style="color:#0f766e;text-decoration:none;font-weight:600">%s</a>
              <div style="margin-top:8px;color:#9ca3af">
                Este email fue enviado a <b>%s</b>
              </div>
            </div>

          </div>
        </div>
        """.formatted(subtitle, title, bodyHtml, SELLER_EMAIL, SELLER_EMAIL, WHATSAPP_LINK, WHATSAPP_NUMBER, safe(toEmail));
    }

    private String primaryButton(String text, String url) {
        return """
        <a href="%s"
           style="background:#0f766e;color:#ffffff;padding:12px 18px;
                  text-decoration:none;border-radius:10px;display:inline-block;
                  font-weight:700;font-size:14px">
          %s
        </a>
        """.formatted(url, text);
    }

    private String softCard(String innerHtml) {
        return """
        <div style="background:#f0fdfa;border:1px solid #99f6e4;
                    padding:16px;border-radius:10px;margin:16px 0">
          %s
        </div>
        """.formatted(innerHtml);
    }

    private String mutedNote(String html) {
        return """
        <div style="margin-top:14px;font-size:13px;color:#374151;line-height:1.55">
          %s
        </div>
        """.formatted(html);
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }
}