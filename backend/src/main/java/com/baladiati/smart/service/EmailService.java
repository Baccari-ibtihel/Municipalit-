package com.baladiati.smart.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public void sendActivationCode(String toEmail, String agentName, String code) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("بلديتي | Code d'activation de votre compte agent");

            String htmlContent = """
                <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; background: #f8fafc; padding: 40px 20px;">
                    <div style="background: linear-gradient(135deg, #1e40af, #3b82f6); padding: 30px; border-radius: 16px 16px 0 0; text-align: center;">
                        <h1 style="color: white; margin: 0; font-size: 24px;">🏛️ بلديتي | Baladiati</h1>
                        <p style="color: #bfdbfe; margin: 8px 0 0 0;">Plateforme Numérique Municipale</p>
                    </div>
                    <div style="background: white; padding: 40px; border-radius: 0 0 16px 16px; box-shadow: 0 4px 20px rgba(0,0,0,0.1);">
                        <h2 style="color: #1e293b; margin-top: 0;">Activation de votre compte Agent Municipal</h2>
                        <p style="color: #475569;">Bonjour <strong>%s</strong>,</p>
                        <p style="color: #475569;">Votre demande d'inscription a été reçue. Voici votre <strong>code d'activation unique</strong> :</p>
                        
                        <div style="background: #eff6ff; border: 2px dashed #3b82f6; border-radius: 12px; padding: 24px; text-align: center; margin: 24px 0;">
                            <p style="color: #64748b; margin: 0 0 8px 0; font-size: 14px;">IDENTIFIANT UNIQUE D'ACTIVATION</p>
                            <div style="font-size: 48px; font-weight: bold; color: #1e40af; letter-spacing: 12px; font-family: monospace;">%s</div>
                        </div>
                        
                        <div style="background: #fef3c7; border-left: 4px solid #f59e0b; padding: 12px 16px; border-radius: 4px; margin: 20px 0;">
                            <p style="color: #92400e; margin: 0; font-size: 14px;">
                                ⚠️ Ce code est valable pendant <strong>15 minutes</strong>. Ne le partagez avec personne.
                            </p>
                        </div>
                        
                        <p style="color: #475569;">Rendez-vous sur la page d'activation et saisissez :</p>
                        <ul style="color: #475569;">
                            <li>Ce code unique</li>
                            <li>Votre adresse e-mail</li>
                            <li>Votre mot de passe choisi</li>
                        </ul>
                        
                        <hr style="border: none; border-top: 1px solid #e2e8f0; margin: 24px 0;">
                        <p style="color: #94a3b8; font-size: 12px; text-align: center; margin: 0;">
                            بلديتي — Plateforme Numérique Municipale de Tunisie<br>
                            Si vous n'avez pas créé ce compte, ignorez cet email.
                        </p>
                    </div>
                </div>
                """.formatted(agentName, code);

            helper.setText(htmlContent, true);
            mailSender.send(message);
            
            System.out.println("✅ E-mail d'activation envoyé avec succès à : " + toEmail);

        } catch (Exception e) {
            System.err.println("❌ ÉCHEC de l'envoi de l'email : " + e.getMessage());
            Throwable cause = e.getCause();
            while (cause != null) {
                System.err.println("   Cause : " + cause.getMessage());
                cause = cause.getCause();
            }
            System.err.println("📌 TRÈS IMPORTANT : Le code d'activation pour " + agentName + " (" + toEmail + ") est : " + code);
        }
    }
}
