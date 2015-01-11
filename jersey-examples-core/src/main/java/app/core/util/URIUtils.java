package app.core.util;

import javax.ws.rs.core.UriBuilderException;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.net.URISyntaxException;

public class URIUtils {
    public static URI safeURI(UriInfo uriInfo, String path) {
        URI uri = null;
        try {
            if (path.matches("^https?://.+$")) {
                uri = new URI(path);
            } else {
                String base = uriInfo.getBaseUriBuilder().build().toString();
                if (!base.endsWith("/"))
                    base += "/";
                uri = new URI(base + path).normalize();
            }
        } catch (UriBuilderException e) {
            throw new IllegalArgumentException(e);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(e);
        }
        return uri;
    }
}
