package app.models;

import app.core.util.FileHelper;
import app.core.util.FileHelper.NamingStrategy;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.util.Map;

public class FileHelperFactory {
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

    public static FileHelper createAccountsIconFinder(String directoryPath) {
        return createAccountsIconFinder(new File(directoryPath));
    }

    public static FileHelper createAccountsIconFinder(File directory) {
        return new FileHelper(directory, TYPES_IMAGE, NAMING_NUMERIC_TREE);
    }
}
