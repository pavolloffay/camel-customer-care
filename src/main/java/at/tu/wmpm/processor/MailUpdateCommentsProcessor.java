package at.tu.wmpm.processor;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.camel.impl.DefaultMessage;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.mongodb.morphia.Morphia;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.mongodb.BasicDBObject;

import at.tu.wmpm.model.Comment;
import at.tu.wmpm.model.FacebookBusinessCase;
import at.tu.wmpm.model.MailBusinessCase;

@Service
public class MailUpdateCommentsProcessor implements Processor {

    @Override
    public void process(Exchange exchange) throws Exception {

        Message in = exchange.getIn();
        
        BasicDBObject dbObject = (BasicDBObject)in.getBody();
        
        Morphia morphia = new Morphia();
        morphia.map(MailBusinessCase.class);
        MailBusinessCase bc = morphia.fromDBObject(MailBusinessCase.class, dbObject);	
        
        System.out.println(in.getHeaders().toString());
        
        Message oldMessage = (Message)in.getHeader("mail");
        
        Map<String, Object> oldHeaders = oldMessage.getHeaders();
        
        Comment c = new Comment();
        c.setFrom(oldHeaders.get("Return-Path").toString());
        c.setMessage(oldMessage.getBody(String.class));
        
        bc.addComment(c);
    
        exchange.setPattern(ExchangePattern.InOut);
       
        Message m = exchange.getOut();
        m.setBody(bc);
        
        exchange.setOut(m);

    }
}
