package at.tu.wmpm.processor;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by pavol on 19.5.2015.
 */
@Service
public class CalendarProcessor implements Processor {

    private static final Logger log = LoggerFactory.getLogger(CalendarProcessor.class);

    @Override
    public void process(Exchange exchange) throws Exception {
        Message in = exchange.getIn();
        Event event = new Event()
                .setSummary(in.getBody(String.class).substring(0, 40))
                .setDescription(in.getBody(String.class)); //TODO also write mail body into description

        DateTime startDateTime = new DateTime(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(new Date().getTime()+5*6000));
        EventDateTime start = new EventDateTime()
                .setDateTime(startDateTime)
                .setTimeZone("Europe/Vienna");
        event.setStart(start);

        DateTime endDateTime = new DateTime(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(new Date().getTime()+500*6000));
        EventDateTime end = new EventDateTime()
                .setDateTime(endDateTime)
                .setTimeZone("Europe/Vienna");
        event.setEnd(end);

        in.setHeader("CamelGoogleCalendar.content", (com.google.api.services.calendar.model.Event)event);
        in.setBody((com.google.api.services.calendar.model.Event)event);
        exchange.setOut(in);
    }
}
