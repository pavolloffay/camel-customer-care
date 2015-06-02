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
        Map<String, Object> inHeaders = in.getHeaders();
        String inMessageBody = in.getBody(String.class);
        String subject = inHeaders.get("Subject").toString();
        log.debug("\n\nMail body:\n" + inMessageBody + "\n");

        /**
         * Set new Business case to exchange message
         */
        MailBusinessCase mailBusinessCase = new MailBusinessCase();
        mailBusinessCase.setSender(inHeaders.get("Return-Path").toString());
        mailBusinessCase.setSubject(subject);
        mailBusinessCase.setNew(true);
        mailBusinessCase.setIncomingDate(inHeaders.get("Date").toString());
        
        Comment comment = new Comment();
        comment.setFrom(inHeaders.get("Return-Path").toString());
        comment.setMessage(inMessageBody);
        //comment.setDate(inHeaders.get("Date"));
        
        mailBusinessCase.addComment(comment);

        Message message = new DefaultMessage();
       
        Matcher matcher = ID_PATTERN.matcher(subject);
        String parentId = null;
        if (matcher.find()) {
            parentId = matcher.group(2);
        }
        
        if(parentId != null){
        	message.setHeader("hasParent", true);
        	message.setHeader("parentId", parentId);
        	message.setHeader("mail", in);
        	message.setBody(parentId);
        }else{
        	message.setBody(mailBusinessCase);
        }
       
        exchange.setOut(message);
    }
}
