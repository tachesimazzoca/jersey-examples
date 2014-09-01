package app.models;

import app.core.Finder.NamingStrategy;

import java.io.File;
import java.util.Map;

import com.google.common.collect.ImmutableMap;

import org.apache.commons.lang.StringUtils;

import app.core.Finder;

public class FinderFactory {
    public static final Map<String, String> TYPES_IMAGE = ImmutableMap.of(
            "jpg", "image/jpeg",
            "gif", "image/gif",
            "png", "image/png");

    public static final NamingStrategy NAMING_NUMERIC_TREE = new NamingStrategy() {
        public String buildRelativePath(String name, String extension) {
            if (!StringUtils.isNumeric(name))
                throw new IllegalArgumentException("The name must contain only digits.");
            StringBuilder sb = new StringBuilder();
            int max = name.length() - 1;
            for (int i = 0; i < max; i++) {
                sb.append(name.substring(i, i + 1));
                sb.append("/");
            }
            sb.append(name);
            sb.append(".");
            sb.append(extension);
            return sb.toString();
        }
    };

    public static Finder createAccountsIconFinder(String directoryPath) {
        return new Finder(new File(directoryPath),
                TYPES_IMAGE, NAMING_NUMERIC_TREE);
    }
}
