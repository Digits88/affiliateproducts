package services.impl;

import java.util.Date;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailAttachment;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.MultiPartEmail;
import org.apache.commons.mail.SimpleEmail;
import org.apache.log4j.Logger;

import batch.jobs.SKCreateBrandCategoryAllChild;
import batch.jobs.ZTestJobZ;
import play.Play;
import play.libs.Mail;
import services.EmailService;
import utils.log.Log;

public class DefaultEmailService implements EmailService {

	private static Logger logger = Logger.getLogger(DefaultEmailService.class);

	@Override
	public void sendEmail(String subject, String mail_body) {
		final String SSL_FACTORY = "javax.net.ssl.SSLSocketFactory";
		// Get a Properties object
		Properties props = System.getProperties();
		props.setProperty("mail.smtp.host", "smtp.gmail.com");
		props.setProperty("mail.smtp.socketFactory.class", SSL_FACTORY);
		props.setProperty("mail.smtp.socketFactory.fallback", "false");
		props.setProperty("mail.smtp.port", "465");// 465, 587
		props.setProperty("mail.smtp.socketFactory.port", "465"); // 465, 587
		props.put("mail.smtp.auth", "true");
		props.put("mail.debug", "true");
		props.put("mail.store.protocol", "pop3");
		props.put("mail.transport.protocol", "smtp");
		final String username = "qateam.spree@gmail.com";
		final String password = "spree2015";
		try {
			Session session = Session.getDefaultInstance(props, new Authenticator() {
				protected PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication(username, password);
				}
			});

			// -- Create a new message --
			Message msg = new MimeMessage(session);

			// -- Set the FROM and TO fields --
			msg.setFrom(new InternetAddress("qateam.spree@gmail.com"));
			msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse("li.wan@searshc.com", false));
			
			msg.setSubject(subject);
			msg.setContent(mail_body, "text/html; charset=utf-8");
			msg.setSentDate(new Date());
			Transport.send(msg);
		} catch (MessagingException e) {
			e.printStackTrace();
			logger.error(Log.message(e.getMessage()));
		}
	}

}
