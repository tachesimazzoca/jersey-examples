package app.core;

import com.fasterxml.jackson.databind.ObjectMapper;

public class Jackson {
    private Jackson() { /**/ }

    public static ObjectMapper newObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        //mapper.registerModule(...);
        //mapper.setPropertyNamingStrategy(...);
        //mapper.setSubtypeResolver(...);
        return mapper;
    }
}
