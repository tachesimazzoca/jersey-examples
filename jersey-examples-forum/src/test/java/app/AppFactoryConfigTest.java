package app;

import static org.junit.Assert.*;
import org.junit.Test;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import app.core.Jackson;
import app.mail.TextMailerFactory;

public class AppFactoryConfigTest {
    @Test
    public void testParser() throws IOException {
        JsonParser parser = new YAMLFactory().createParser(
                this.getClass().getResourceAsStream("/conf/factory.yml"));
        ObjectMapper mapper = Jackson.newObjectMapper();
        AppFactoryConfig appConfig = mapper.readValue(parser, AppFactoryConfig.class);

        TextMailerFactory signupMailerFactory = appConfig.getSignupMailerFactory();
        assertEquals("localhost", signupMailerFactory.getHostName());
        assertEquals(2525, signupMailerFactory.getSmtpPort());
    }
}
