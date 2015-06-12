package at.tu.wmpm.processor;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.camel.Body;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.impl.DefaultMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

import at.tu.wmpm.model.BusinessCase;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;

@Configuration
public class CalendarProcessor {

    private static final Logger log = LoggerFactory.getLogger(CalendarProcessor.class);

    public void process(@Body BusinessCase body, Exchange e) throws Exception {
        Message in = new DefaultMessage();
        com.google.api.services.calendar.model.Event event = new Event()
                .setSummary(body.getSender()+": "+body.getId())
                .setDescription(body.toString()+", Message: "+body.getLastMessage());

        DateTime startDateTime = new DateTime(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(((long)new Date().getTime())-(120*60000)+(5*60000)));
        EventDateTime start = new EventDateTime()
                .setDateTime(startDateTime)
                .setTimeZone("Europe/Vienna");
        event.setStart(start);

        DateTime endDateTime = new DateTime(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(((long)new Date().getTime())-(120*60000)+(60*60000)));
        EventDateTime end = new EventDateTime()
                .setDateTime(endDateTime)
                .setTimeZone("Europe/Vienna");
        event.setEnd(end);

        in.setHeader("CamelGoogleCalendar.content", (com.google.api.services.calendar.model.Event)event);
        in.setBody((com.google.api.services.calendar.model.Event)event);
        e.setOut(in);
    }
}
