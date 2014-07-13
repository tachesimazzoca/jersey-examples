package app.core;

import com.google.common.collect.ImmutableMap;

import java.util.Map;

public class Util {
    public static Map<String, Object> params(Object... args) {
        ImmutableMap.Builder<String, Object> builder =
                new ImmutableMap.Builder<String, Object>();
        if (args.length % 2 != 0)
            throw new IllegalArgumentException(
                    "The number of args must be even.");
        for (int i = 0; i < args.length; i += 2) {
            builder.put((String) args[i], args[i + 1]);
        }
        return builder.build();
    }
}
