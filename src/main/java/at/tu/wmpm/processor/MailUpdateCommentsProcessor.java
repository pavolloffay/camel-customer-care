package at.tu.wmpm.processor;

import at.tu.wmpm.exception.MailException;
import at.tu.wmpm.model.Comment;
import at.tu.wmpm.model.MailBusinessCase;
import com.mongodb.BasicDBObject;
import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.Message;
import org.mongodb.morphia.Morphia;
import org.springframework.stereotype.Service;

@Service
public class MailUpdateCommentsProcessor {

    public void process(Exchange exchange) throws MailException {

        Message in = exchange.getIn();
        Comment comment = (Comment) in.getHeaders().get("comment");

        MailBusinessCase mailBusinessCase = getMailBusinessCase((BasicDBObject) in
                .getBody());
        mailBusinessCase.addComment(comment);

        Message outMessage = exchange.getOut();
        outMessage.setBody(mailBusinessCase);

        exchange.setPattern(ExchangePattern.InOut);
        exchange.setOut(outMessage);
    }

    private MailBusinessCase getMailBusinessCase(BasicDBObject basicDBObject) {
        Morphia morphia = new Morphia();
        morphia.map(MailBusinessCase.class);
        MailBusinessCase mailBusinessCase = morphia.fromDBObject(
                MailBusinessCase.class, basicDBObject);

        return mailBusinessCase;
    }
}
