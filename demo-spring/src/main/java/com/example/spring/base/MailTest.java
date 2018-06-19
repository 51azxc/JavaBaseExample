package com.example.spring.base;

import java.io.File;
import java.util.Properties;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;

/*
 * 利用Spring发送邮件
 */

public class MailTest {
	
	private final static String EMAIL_HOST = "";
	private final static String EMAIL_USERNAME = "";
	private final static String EMAIL_PASSWORD = "";
	
	public void sendEmail(String fileName, String to) throws MessagingException {
		JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
		MimeMessage mimeMessage = mailSender.createMimeMessage();
		
		FileSystemResource attach = new FileSystemResource(new File(fileName));
		MimeMessageHelper helper = new MimeMessageHelper(mimeMessage,true);
		
		//发送邮件地址
		mailSender.setHost(EMAIL_HOST);
		//发送人用户名
		mailSender.setUsername(EMAIL_USERNAME);
		//发送人密码
		mailSender.setPassword(EMAIL_PASSWORD);
		mailSender.setDefaultEncoding("GB2312");
		Properties properties = new Properties();
		// 如果为true,邮件服务器会去验证用户名和密码
	    properties.put("mail.smtp.auth", "true");
	    properties.put("mail.smtp.timeout", "25000");  
	    mailSender.setJavaMailProperties(properties);
	    
	    //接收人
	    helper.setTo(to);
	    //来自
	    helper.setFrom(EMAIL_USERNAME);
	    //邮件标题
	    helper.setSubject("title");
	    //邮件正文，第二个参数指定是否以html为脚本编辑邮件正文
	    helper.setText("test", false);
	    //添加附件
	    helper.addAttachment(fileName, attach);
	    //发送邮件
		mailSender.send(mimeMessage);
	}
}
