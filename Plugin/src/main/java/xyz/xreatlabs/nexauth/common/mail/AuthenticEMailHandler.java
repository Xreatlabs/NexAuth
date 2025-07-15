/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.xreatlabs.nexauth.common.mail;

import org.apache.commons.mail.EmailConstants;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
import xyz.xreatlabs.nexauth.api.mail.EmailHandler;
import xyz.xreatlabs.nexauth.common.AuthenticNexAuth;
import xyz.xreatlabs.nexauth.common.config.ConfigurationKeys;

public class AuthenticEMailHandler implements EmailHandler {

    private final AuthenticNexAuth<?, ?> plugin;

    public AuthenticEMailHandler(AuthenticNexAuth<?, ?> plugin) {
        this.plugin = plugin;
    }

    @Override
    public void sendEmail(String email, String subject, String content) {
        try {
            var config = plugin.getConfiguration();
            var port = config.get(ConfigurationKeys.MAIL_PORT);

            var mail = new HtmlEmail();

            mail.setCharset(EmailConstants.UTF_8);
            mail.setHostName(config.get(ConfigurationKeys.MAIL_HOST));
            mail.setSmtpPort(port);
            mail.setSubject(subject);
            mail.setAuthentication(config.get(ConfigurationKeys.MAIL_USERNAME), config.get(ConfigurationKeys.MAIL_PASSWORD));
            mail.addTo(email);
            mail.setFrom(config.get(ConfigurationKeys.MAIL_EMAIL), config.get(ConfigurationKeys.MAIL_SENDER));

            switch (port) {
                case 465 -> {
                    mail.setSslSmtpPort(String.valueOf(port));
                    mail.setSSLOnConnect(false);
                }
                case 587 -> {
                    mail.setStartTLSEnabled(true);
                    mail.setStartTLSRequired(true);
                }
                default -> {
                    mail.setStartTLSEnabled(true);
                    mail.setSSLOnConnect(true);
                    mail.setSSLCheckServerIdentity(true);
                }
            }

            mail.setHtmlMsg(content);
            Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
            mail.send();
        } catch (EmailException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public void sendTestMail(String email) {
        sendEmail(email, "NexAuth test mail", """
                Congratulations! You have successfully configured email sending in NexAuth!<br>
                Now, your users can reset their passwords.<br>
                <i>If you have no idea what this means, block the sender.</i>
                """);
    }

    @Override
    public void sendPasswordResetMail(String email, String token, String username, String ip) {
        sendEmail(email,
                plugin.getMessages().getRawMessage("email-password-reset-subject")
                        .replace("%server%", plugin.getConfiguration().get(ConfigurationKeys.MAIL_SENDER)),
                plugin.getMessages().getRawMessage("email-password-reset-content")
                        .replace("%server%", plugin.getConfiguration().get(ConfigurationKeys.MAIL_SENDER))
                        .replace("%code%", token)
                        .replace("%ip%", ip)
                        .replace("%name%", username)
        );
    }

    @Override
    public void sendVerificationMail(String email, String token, String username) {
        sendEmail(email,
                plugin.getMessages().getRawMessage("email-verification-subject")
                        .replace("%server%", plugin.getConfiguration().get(ConfigurationKeys.MAIL_SENDER)),
                plugin.getMessages().getRawMessage("email-verification-content")
                        .replace("%name%", username)
                        .replace("%server%", plugin.getConfiguration().get(ConfigurationKeys.MAIL_SENDER))
                        .replace("%code%", token)
        );
    }
}
