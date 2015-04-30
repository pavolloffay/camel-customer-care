package at.tu.wmpm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by pavol on 30.4.2015.
 */
public class HelloWorld {

    private static Logger logger = LoggerFactory.getLogger(HelloWorld.class);

    public static void main(String[] args) {
        System.out.println("HL");
        logger.debug("debug");
    }
}
