package at.tu.wmpm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Created by pavol on 30.4.2015.
 */
@Component
public class SimpleBean {

    private static final Logger log = LoggerFactory.getLogger(SimpleBean.class);

    public SimpleBean() {
        log.debug("Spring bean\n\n");
    }
}
