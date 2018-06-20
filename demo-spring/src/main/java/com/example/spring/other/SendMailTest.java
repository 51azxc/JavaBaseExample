package com.example.spring.other;

import javax.mail.Address;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.event.TransportEvent;
import javax.mail.event.TransportListener;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

public class SendMailTest {
    private enum DeliveredState {
        INITIAL, MESSAGE_DELIVERED, MESSAGE_NOT_DELIVERED, MESSAGE_PARTIALLY_DELIVERED,
    }
    private static class DeliveredStateFuture {
        private DeliveredState state = DeliveredState.INITIAL;
        synchronized void waitForReady() throws InterruptedException {
            if (state == DeliveredState.INITIAL) {
                wait();
            }
        }
        @SuppressWarnings("unused")
		synchronized DeliveredState getState() {
            return state;
        }
        synchronized void setState(DeliveredState newState) {
            state = newState;
            notifyAll();
        }
    }

    public void sendEmail(String subject, String text) {

        String from = "";
        String to = "";
        String host = "smtp.126.com";
        String password = "";

        Properties props = new Properties();
        props.setProperty("mail.debug", "true");
        props.setProperty("mail.smtp.auth", "true");
        props.setProperty("mail.host", host);
        props.setProperty("mail.transport.protocol", "smtp");

        Session session = Session.getInstance(props);
        try {
            MimeMessage msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress(from));
            msg.setSubject(subject);
            msg.setText(text);
            Transport transport = session.getTransport();
            transport.connect(from, password);
            final DeliveredStateFuture future = new DeliveredStateFuture();
            transport.addTransportListener(new TransportListener() {
                public void messageDelivered(TransportEvent arg0) {
                    future.setState(DeliveredState.MESSAGE_DELIVERED);
                }
                public void messageNotDelivered(TransportEvent arg0) {
                    future.setState(DeliveredState.MESSAGE_NOT_DELIVERED);
                }
                public void messagePartiallyDelivered(TransportEvent arg0) {
                    future.setState(DeliveredState.MESSAGE_PARTIALLY_DELIVERED);
                }
            });
            transport.sendMessage(msg,
                    new Address[] { new InternetAddress(to) });
            future.waitForReady();
            transport.close();
        } catch (MessagingException | InterruptedException mex) {
            System.out.println(mex.getLocalizedMessage());
        }

    }

    public static void main(String[] args) throws MessagingException {
        SendMailTest mail = new SendMailTest();
        mail.sendEmail("Test email", "String text");
    }
}
