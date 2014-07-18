package app.core;

import javax.ws.rs.core.NewCookie;

import com.google.common.base.Optional;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

public class CookieBaker {
    private static final String ENCODING = "UTF-8";
    private static final int MAX_VALUE_LENGTH = 2048;
    private NewCookie cookie;
    private Map<String, String> data;

    public CookieBaker(NewCookie cookie) {
        this.cookie = cookie;
        this.data = decode(cookie.getValue());
    }

    private String encode(Map<String, String> m) {
        StringBuilder sb = new StringBuilder();
        try {
            for (Map.Entry<String, String> kv : m.entrySet()) {
                sb.append(URLEncoder.encode(kv.getKey(), ENCODING));
                sb.append("=");
                sb.append(URLEncoder.encode(kv.getValue(), ENCODING));
                sb.append("&");
            }
            if (sb.length() > 0)
                sb.deleteCharAt(sb.length() - 1);
        } catch (UnsupportedEncodingException e) {
            // Returns an empty string immediately if failed.
            return "";
        }
        if (sb.length() > MAX_VALUE_LENGTH)
            // Returns an empty string immediately if the length of the encoded
            // string is more then the capacity of the cookie value.
            return "";

        return sb.toString();
    }

    private Map<String, String> decode(String value) {
        Map<String, String> m = new HashMap<String, String>();
        String[] pairs = StringUtils.splitPreserveAllTokens(value, "&");
        for (int i = 0; i < pairs.length; i++) {
            String[] kv = StringUtils.split(pairs[i], "=");
            if (kv.length != 2) {
                // Returns an empty map immediately if the value is unknown
                // format.
                m.clear();
                return m;
            }
            try {
                String k = URLDecoder.decode(kv[0], ENCODING);
                String v = URLDecoder.decode(kv[1], ENCODING);
                m.put(k, v);
            } catch (UnsupportedEncodingException e) {
                // Returns an empty map immediately if failed.
                m.clear();
                return m;
            }
        }
        return m;
    }

    public NewCookie toCookie() {
        return new NewCookie(cookie.getName(), encode(data),
                cookie.getPath(), cookie.getDomain(),
                cookie.getVersion(), cookie.getComment(),
                cookie.getMaxAge(), cookie.isSecure());
    }

    public Optional<String> get(String key) {
        if (data.containsKey(key))
            return Optional.of(data.get(key));
        else
            return Optional.absent();
    }

    public CookieBaker put(String key, String value) {
        data.put(key, value);
        return this;
    }

    public CookieBaker clear() {
        data.clear();
        return this;
    }

    public boolean isEmpty() {
        return data.isEmpty();
    }
}
