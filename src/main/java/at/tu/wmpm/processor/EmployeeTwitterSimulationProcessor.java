package at.tu.wmpm.processor;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.mongodb.morphia.Morphia;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import at.tu.wmpm.exception.TwitterException;
import at.tu.wmpm.model.BusinessCaseStatus;
import at.tu.wmpm.model.TwitterBusinessCase;

import com.mongodb.DBObject;

@Service
public class EmployeeTwitterSimulationProcessor {

    private static final Logger log = LoggerFactory.getLogger(EmployeeTwitterSimulationProcessor.class);


    public void commentOnTwitterBusinessCase(Exchange e) throws Exception {
        String tweetMessage = "Reply Test only 140 char";

        String body = e.getIn().getBody(String.class);
       // e.setOut(tweets);

        Message m = addComment(e, tweetMessage);

        if (null == m) {
            throw new TwitterException("An error occured while processing business cases from mongoDB.");
        }

        //e.getOut().setBody(m.getHeader(name));
        // change the message to say Hello

        e.getOut().setBody("@"+m.getHeader("CamelTwitter.message"));

    }

    public void closeTwitterkBusinessCase(Exchange e) throws Exception {
        String tweetMessage = "Your ticket was successfully processed and is being closed now. Thank you for your patience.";
        Message m = addComment(e, tweetMessage);
        TwitterBusinessCase tbc = null;

        if (null != m) {
            tbc = m.getHeader("bc", TwitterBusinessCase.class);
            tbc.setStatus(BusinessCaseStatus.CLOSED);
            m.setHeader("bc", tbc);
            m.setBody(tbc);
        } else
            throw new TwitterException("An error occured while processing business cases from mongoDB.");
    }

    public Message addComment(Exchange e, String message) throws Exception {

        @SuppressWarnings("unchecked")
        List<DBObject> DBObjectList = e.getIn().getMandatoryBody(
                (Class<List<DBObject>>) (Class<?>) List.class);
        List<TwitterBusinessCase> businessCaseList = null;

        int count;
        Random random = new Random();
        TwitterBusinessCase chosenBusinessCase;
        Message m;
        String tweetMessage = "";

        count = e.getIn().getHeader("CamelMongoDbResultTotalSize",
                Integer.class);
        log.info("Got " + count + " DBObjects according to header");

        if (count == 0) {
            log.info("Raise TwitterException, there are no TwitterBusinessCase in the mongoDB");
            throw new TwitterException(
                    "There are no TwitterBusinessCase in the mongoDB");
        }

        if (null != DBObjectList)
            log.info("Got " + DBObjectList.size()
                    + " DBObjects according to list");

        if (null != DBObjectList) {
            businessCaseList = generateTwitterBusinessCases(DBObjectList);

            if (null != businessCaseList) {
                log.info("Got " + businessCaseList.size() + " open business cases");

                if (!businessCaseList.isEmpty()) {
                    chosenBusinessCase = businessCaseList.get(random
                            .nextInt(businessCaseList.size()));
                    log.info("Chosen BusinessCase: "
                            + chosenBusinessCase.getId());

                    if (chosenBusinessCase.getTweetID() != 0) {
                      /*  tweetMessage = "Dear " + chosenBusinessCase.getSender()
                                + "\n" + message + "\n\n" + "Yours sincerely,"
                                + "\n" + "The IT Crowd";
                               */
                        tweetMessage = chosenBusinessCase.getScreenName() + " Dear User!"
                                + "\n We are working on it!"
                                + "\n Your IT-Team";
                        log.info(tweetMessage);

                        m = e.getOut();
                        m.setHeader("CamelTwitter.tweetId",
                                chosenBusinessCase.getTweetID());
                        m.setHeader("CamelTwitter.message", tweetMessage);
                        m.setHeader("bc", chosenBusinessCase);

                        return m;
                    }
                }
            }
        }

        return null;
    }

    private List<TwitterBusinessCase> generateTwitterBusinessCases(
            List<DBObject> DBObjectList) {
        Morphia morphia = new Morphia();
        morphia.map(TwitterBusinessCase.class);
        List<TwitterBusinessCase> tempList = new ArrayList<TwitterBusinessCase>();
        TwitterBusinessCase tempObject = null;

        for (DBObject x : DBObjectList) {
            tempObject = morphia.fromDBObject(TwitterBusinessCase.class, x);

            if (null != tempObject) {

                if (tempObject.getStatus().equals(BusinessCaseStatus.OPEN))
                    tempList.add(tempObject);
            }

            else
                log.info("TwitterBusinessCase is null");
        }

        return tempList;
    }
}
