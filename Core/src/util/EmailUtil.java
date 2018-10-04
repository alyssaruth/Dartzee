package util;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import com.sun.mail.smtp.SMTPTransport;

public class EmailUtil 
{
	private static final int MAX_ATTACHMENTS_TO_EMAIL = 10;
	
	public static void sendEmail(String title, String message, String targetEmail, 
	  String fromUsername, String fromPassword, ArrayList<File> attachments) throws MessagingException
	{
		if (message.isEmpty())
		{
			Debug.append("Not sending email " + title + " as body is empty");
			return;
		}

		//Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());
		final String SSL_FACTORY = "javax.net.ssl.SSLSocketFactory";

		// Get a Properties object
		Properties props = System.getProperties();
		props.setProperty("mail.smtps.host", "smtp.gmail.com");
		props.setProperty("mail.smtp.socketFactory.class", SSL_FACTORY);
		props.setProperty("mail.smtp.socketFactory.fallback", "false");
		props.setProperty("mail.smtp.port", "465");
		props.setProperty("mail.smtp.socketFactory.port", "465");
		props.setProperty("mail.smtps.auth", "true");

		Session session = Session.getInstance(props, null);
		
		MimeMessage msg = new MimeMessage(session);

		msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(targetEmail, false));
		msg.setSubject(title);
		msg.setSentDate(new Date());
		
		if (attachments == null
		  || attachments.isEmpty())
		{
			msg.setText(message, "utf-8");
		}
		else
		{
			BodyPart messageBodyPart = new MimeBodyPart();
	        messageBodyPart.setText(message);
	        
	        Multipart multipart = new MimeMultipart();
	        multipart.addBodyPart(messageBodyPart);

	        int size = attachments.size();
	        if (size > MAX_ATTACHMENTS_TO_EMAIL)
	        {
	        	Debug.append("Only emailing the first " + MAX_ATTACHMENTS_TO_EMAIL + " attachments (had " + size + ")");
	        	size = MAX_ATTACHMENTS_TO_EMAIL;
	        }
	        
	        for (int i=0; i<size; i++)
	        {
	        	File attachment = attachments.get(i);
	        	messageBodyPart = new MimeBodyPart();
		        String filename = attachment.getAbsolutePath();
		        DataSource source = new FileDataSource(filename);
		        messageBodyPart.setDataHandler(new DataHandler(source));
		        messageBodyPart.setFileName(filename);
		        multipart.addBodyPart(messageBodyPart);
	        }
	        
	        msg.setContent(multipart);
		}

		SMTPTransport t = (SMTPTransport)session.getTransport("smtps");

		t.connect("smtp.gmail.com", fromUsername, fromPassword);
		t.sendMessage(msg, msg.getAllRecipients());      
		t.close();
	}
}
