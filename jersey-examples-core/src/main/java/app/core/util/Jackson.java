package app.core.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.IOException;
import java.io.InputStream;

public class Jackson {
    private Jackson() { /**/
    }

    public static ObjectMapper newObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        // mapper.registerModule(...);
        // mapper.setPropertyNamingStrategy(...);
        // mapper.setSubtypeResolver(...);
        return mapper;
    }

    public static <T> T fromYAML(InputStream input, Class<T> type) throws IOException {
        JsonParser parser = new YAMLFactory().createParser(input);
        ObjectMapper mapper = Jackson.newObjectMapper();
        return mapper.readValue(parser, type);
    }
}
