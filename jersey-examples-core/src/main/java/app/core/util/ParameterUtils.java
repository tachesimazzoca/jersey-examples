package app.core.util;

import com.google.common.collect.ImmutableMap;

import java.util.Map;

public class ParameterUtils {
    public static Map<String, Object> params(Object... args) {
        ImmutableMap.Builder<String, Object> builder =
                new ImmutableMap.Builder<String, Object>();
        if (args.length % 2 != 0)
            throw new IllegalArgumentException(
                    "The number of args must be even.");
        for (int i = 0; i < args.length; i += 2) {
            if (args[i + 1] != null)
                builder.put((String) args[i], args[i + 1]);
        }
        return builder.build();
    }

    public static <T> T nullTo(T... args) {
        for (T arg : args) {
            if (arg != null)
                return arg;
        }
        return null;
    }

    public static String emptyTo(String... args) {
        for (String arg : args) {
            if (arg != null && !arg.isEmpty())
                return arg;
        }
        return null;
    }
}
