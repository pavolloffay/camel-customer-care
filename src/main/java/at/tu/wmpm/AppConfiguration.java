package at.tu.wmpm;

import cz.jirutka.spring.embedmongo.EmbeddedMongoFactoryBean;
import org.apache.camel.CamelContext;
import org.apache.camel.component.facebook.config.FacebookConfiguration;
import org.apache.camel.component.google.calendar.GoogleCalendarComponent;
import org.apache.camel.component.google.calendar.GoogleCalendarConfiguration;
import org.apache.camel.component.properties.PropertiesComponent;
import org.apache.camel.spring.SpringCamelContext;
import org.apache.camel.spring.javaconfig.CamelConfiguration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;


/**
 * Created by pavol on 7.6.2015.
 * http://camel.apache.org/spring-java-config.html
 * http://camel.apache.org/spring-java-config-example.html
 */
@Configuration
@PropertySource("classpath:accounts.properties")
@ComponentScan(basePackages = {"at.tu.wmpm"})
public class AppConfiguration extends CamelConfiguration {

    private static final int MONGO_PORT = 12345;
    private static final String MONGO_VERSION = "2.4.5";
    private static final String MONGO_BIND_IP = "127.0.0.1";

    @Value("${facebook.id}")
    private String FACEBOOK_ID;
    @Value("${facebook.secret}")
    private String FACEBOOK_SECRET;
    @Value("${facebook.access.token}")
    private String FACEBOOK_ACCESS_TOKEN;
    @Value("${google.refresh.token}")
    private String GOOGLE_REFRESH;
    @Value("${google.client.id}")
    private String GOOGLE_CLIENT_ID;
    @Value("${google.client.secret}")
    private String GOOGLE_CLIENT_SECRET;
    @Value("${google.application.name}")
    private String GOOGLE_APP_NAME;


    @Override
    protected CamelContext createCamelContext() throws Exception {
        return new SpringCamelContext(getApplicationContext());
    }

    @Override
    protected void setupCamelContext(CamelContext camelContext) throws Exception {
        super.setupCamelContext(camelContext);
    }


    @Bean(name = "mongo")
    public EmbeddedMongoFactoryBean embeddedMongoFactoryBean() {
        EmbeddedMongoFactoryBean embeddedMongoFactoryBean = new EmbeddedMongoFactoryBean();
        embeddedMongoFactoryBean.setVersion(MONGO_VERSION);
        embeddedMongoFactoryBean.setBindIp(MONGO_BIND_IP);
        embeddedMongoFactoryBean.setPort(MONGO_PORT);

        return embeddedMongoFactoryBean;
    }


    @Bean(name = "facebookConfiguration")
    public FacebookConfiguration facebookConfiguration() {
        FacebookConfiguration facebookConfiguration = new FacebookConfiguration();
        facebookConfiguration.setOAuthAppId(FACEBOOK_ID);
        facebookConfiguration.setOAuthAppSecret(FACEBOOK_SECRET);
        facebookConfiguration.setOAuthAccessToken(FACEBOOK_ACCESS_TOKEN);

        return facebookConfiguration;
    }

    @Bean
    public GoogleCalendarConfiguration googleCalendarConfiguration() {
        GoogleCalendarConfiguration googleCalendarConfiguration = new GoogleCalendarConfiguration();
        googleCalendarConfiguration.setRefreshToken(GOOGLE_REFRESH);
        googleCalendarConfiguration.setClientId(GOOGLE_CLIENT_ID);
        googleCalendarConfiguration.setClientSecret(GOOGLE_CLIENT_SECRET);
        googleCalendarConfiguration.setApplicationName(GOOGLE_APP_NAME);

        return googleCalendarConfiguration;
    }

    @Bean(name = "google-calendar")
    public GoogleCalendarComponent googleCalendarComponent() throws Exception {
        GoogleCalendarComponent googleCalendarComponent = new GoogleCalendarComponent();
        googleCalendarComponent.setConfiguration(googleCalendarConfiguration());
//        googleCalendarComponent.setCamelContext(camelContext()); // not necessary

        return googleCalendarComponent;
    }

    // spring @Value
    @Bean
    public static PropertySourcesPlaceholderConfigurer placeHolderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }

    // camel properties
    @Bean(name = "properties")
    public PropertiesComponent propertiesComponent() {
        PropertiesComponent propertiesComponent = new PropertiesComponent();
        propertiesComponent.setLocation("classpath:accounts.properties");

        return propertiesComponent;
    }
}
