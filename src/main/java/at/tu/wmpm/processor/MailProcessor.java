package at.tu.wmpm.processor;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.camel.impl.DefaultMessage;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import at.tu.wmpm.model.Comment;
import at.tu.wmpm.model.MailBusinessCase;

/**
 * Created by pavol on 18.5.2015.
 */
@Service
public class MailProcessor implements Processor {

    private static final Logger log = LoggerFactory.getLogger(MailProcessor.class);
    private static final Pattern ID_PATTERN = Pattern.compile("(ID:)([0-9a-f]{8}(-[0-9a-f]{4}){3}-[0-9a-f]{12})");

    @Override
    public void process(Exchange exchange) throws Exception {
        log.debug(ReflectionToStringBuilder.toString(exchange));
        Message in = exchange.getIn();
        String inMessageBody = in.getBody(String.class);
        String subject = in.getHeaders().get("Subject").toString();
        log.debug("\n\nMail body:\n" + inMessageBody + "\n");

        /**
         * Transform message body to model object
         */
        Comment comment = getComment(in.getHeaders(), inMessageBody);
        MailBusinessCase mailBusinessCase = getMailBusinessCase(in.getHeaders());
        mailBusinessCase.addComment(comment);

        Matcher matcher = ID_PATTERN.matcher(subject);
        if (matcher.find()) {
            mailBusinessCase.setParentId(matcher.group(2));
        }

        Message message = new DefaultMessage();

        if(mailBusinessCase.getParentId() != null){
            message.setHeader("hasParent", true);
            message.setHeader("comment", comment);
        }

        message.setBody(mailBusinessCase);
        exchange.setOut(message);
    }

    private MailBusinessCase getMailBusinessCase(Map<String, Object> headers) {
        MailBusinessCase mailBusinessCase = new MailBusinessCase();
        mailBusinessCase.setSender(headers.get("Return-Path").toString());
        mailBusinessCase.setSubject(headers.get("Subject").toString());
        mailBusinessCase.setIncomingDate(headers.get("Date").toString());
        mailBusinessCase.setNew(true);

        return mailBusinessCase;
    }

    private Comment getComment(Map<String, Object> headers, String messageBody) {
        Comment comment = new Comment();
        comment.setFrom(headers.get("Return-Path").toString());
        comment.setMessage(messageBody);

        return comment;
    }
}
