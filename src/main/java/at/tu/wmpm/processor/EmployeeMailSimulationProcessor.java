package at.tu.wmpm.processor;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.mongodb.morphia.Morphia;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

import at.tu.wmpm.exception.MailException;
import at.tu.wmpm.model.BusinessCaseStatus;
import at.tu.wmpm.model.MailBusinessCase;

import com.mongodb.DBObject;

@Configuration
public class EmployeeMailSimulationProcessor {

    private static final Logger log = LoggerFactory
            .getLogger(EmployeeMailSimulationProcessor.class);

    public void answerMailBusinessCase(Exchange e) throws Exception {
        String mailAnswer = "Please restart your device and try it again. If further problems occur don't hesitate to contact us again. Your ticket will be closed. ";

        Message m = addComment(e, mailAnswer);

        if (m == null) {
            throw new MailException(
                    "An error occured while processing business cases from mongoDB.");
        }
        log.debug("Mail answer set");
    }

    public Message addComment(Exchange e, String message) throws Exception {

        @SuppressWarnings("unchecked")
        List<DBObject> DBObjectList = e.getIn().getMandatoryBody(
                (Class<List<DBObject>>) (Class<?>) List.class);
        List<MailBusinessCase> businessCaseList = null;

        int count;
        Random random = new Random();
        MailBusinessCase chosenBusinessCase;
        Message m;
        String mailAnswer = "";

        count = e.getIn().getHeader("CamelMongoDbResultTotalSize",
                Integer.class);
        log.info("Got " + count + " DBObjects according to header");

        if (count == 0) {
            throw new MailException(
                    "There are no Mail business cases in the mongoDB");
        }
        if (DBObjectList != null) {
            businessCaseList = generateMailBusinessCases(DBObjectList);
            if (businessCaseList != null) {
                log.info("Got " + businessCaseList.size()
                        + " open business cases");
                if (!businessCaseList.isEmpty()) {
                    chosenBusinessCase = businessCaseList.get(random
                            .nextInt(businessCaseList.size()));
                    log.info("Chosen BusinessCase: "
                            + chosenBusinessCase.getId());
                    if (chosenBusinessCase.getId() != null) {
                        mailAnswer = "Dear "
                                + chosenBusinessCase.getSender()
                                + "\n"
                                + message
                                + "\n\nYours sincerely,\n The IT Crowd\n\n\n\nYour request:\n\n"
                                + chosenBusinessCase.getLastMessage();
                        m = e.getOut();
                        m.setHeader("To", chosenBusinessCase.getSender());
                        m.setHeader("From", "customer.care.tu.wien@gmail.com");
                        m.setHeader("Subject", "Ticket-ID:"
                                + chosenBusinessCase.getId());
                        m.setHeader("Body", mailAnswer);
                        chosenBusinessCase.setStatus(BusinessCaseStatus.CLOSED);
                        m.setBody(chosenBusinessCase);
                        e.setOut(m);
                        return m;
                    }
                }
            }
        }
        return null;
    }

    private List<MailBusinessCase> generateMailBusinessCases(
            List<DBObject> DBObjectList) {
        Morphia morphia = new Morphia();
        morphia.map(MailBusinessCase.class);
        List<MailBusinessCase> tempList = new ArrayList<MailBusinessCase>();
        MailBusinessCase tempObject = null;
        for (DBObject x : DBObjectList) {
            tempObject = morphia.fromDBObject(MailBusinessCase.class, x);

            if (tempObject != null) {
                if (tempObject.getStatus().equals(BusinessCaseStatus.OPEN))
                    tempList.add(tempObject);
            } else {
                log.info("MailBusinessCase is null");
            }
        }
        return tempList;
    }
}