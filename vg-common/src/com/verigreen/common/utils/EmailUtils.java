/*******************************************************************************
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.verigreen.common.utils;

import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class EmailUtils {

    private EmailUtils() {
    }

    public static void send(
            String subject,
            String messageText,
            String[] recipients,
            String sender,
            String mailServer,
            String signature) throws MessagingException {
        
        // Get system properties
        Properties properties = System.getProperties();
        // Setup mail server
        properties.setProperty("mail.smtp.host", mailServer);
        // Get the default Session object.
        Session session = Session.getDefaultInstance(properties);
        MimeMessage message = new MimeMessage(session);
        // from
        message.setFrom(new InternetAddress(sender));
        // recipients
        for (String currRecipient : recipients) {
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(currRecipient));
        }
        message.setSubject(subject);
        // body (as html)
        message.setContent(getMessageTextAsHtml(messageText, signature), "text/html; charset=utf-8");
        Transport.send(message);
    }
    
    private static String getMessageTextAsHtml(String messageText, String signature) {
        
        return String.format(
                "<html>%s%s</html>",
                messageText.replace(StringUtils.NEW_LINE, HtmlUtils.NEW_LINE),
                signature);
    }
}
