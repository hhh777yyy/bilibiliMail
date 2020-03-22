package mail;

import main.Start;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;


import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.PrintStream;
import java.util.Date;
import java.util.Properties;

public class SendMail {
    private static Logger logger = Logger.getLogger(SendMail.class.getName());

    private static String myEmailAccount = "1119848245@qq.com";//邮箱账号
    private static String myEmailPassword = "omvhcosjewojhbig";//邮箱密码
    private static String myEmailSMTPHost = "smtp.qq.com";
//    发送到指定的单个邮箱
    public static Boolean sendEmail(String text,String receiveEmail){
        boolean sendOrNot = false ;
        Transport transport;

        try {
            Properties props = new Properties();
            props.setProperty("mail.transport.protocol", "smtp");
            props.setProperty("mail.smtp.host", myEmailSMTPHost);
            props.setProperty("mail.smtp.auth", "true");
            final String smtpPort = "465";
            props.setProperty("mail.smtp.port", smtpPort);
            props.setProperty("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            props.setProperty("mail.smtp.socketFactory.fallback", "false");
            props.setProperty("mail.smtp.socketFactory.port", smtpPort);
            Session session = Session.getDefaultInstance(props);
                               // 设置为debug模式, 可以查看详细的发送 log
            session.setDebugOut(new PrintStream(new  LoggingOutStream(Logger.getRootLogger(), Level.DEBUG), true));
            session.setDebug(true);
            MimeMessage message = createMimeMessage(session, myEmailAccount, receiveEmail,text);
            transport = session.getTransport();
            transport.connect(myEmailAccount, myEmailPassword);
            transport.sendMessage(message, message.getAllRecipients());
            sendOrNot = true ;
//            transport.close();
        } catch (MessagingException e) {
            e.printStackTrace();
            logger.error(e);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sendOrNot;
    }


    private static MimeMessage createMimeMessage(Session session, String sendMail, String receiveMail, String text) throws Exception {
        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress(sendMail, "1119848245", "UTF-8"));
        //new InternetAddress中personal属性为邮件的收/发件人的名字
        message.setRecipient(MimeMessage.RecipientType.TO, new InternetAddress(receiveMail,"15626128581@163.com", "UTF-8"));
        message.setSubject("邮箱验证", "UTF-8");
        message.setContent(text, "text/html;charset=UTF-8");
        message.setSentDate(new Date());
        message.saveChanges();
        return message;
    }


    public static void main(String[] args) {
        Boolean sendEmail = sendEmail("这是内容，验证码45845", "15626128581@163.com");
        System.out.println(sendEmail);
    }

}
