package app.core.session;

import com.google.common.base.Optional;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang.StringUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.ws.rs.core.NewCookie;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class CookieSession {
    private static final String ENCODING = "UTF-8";
    private static final String MAC_ALGORITHM = "HmacSHA1";
    private static final int MAX_VALUE_LENGTH = 2048;

    private NewCookie cookie;
    private String secret;
    private ConcurrentMap<String, String> data;

    public CookieSession(NewCookie cookie) {
        this(cookie, null);
    }

    public CookieSession(NewCookie cookie, String secret) {
        this.cookie = cookie;
        this.secret = secret;
        this.data = decode(cookie.getValue());
    }

    private String encode(Map<String, String> m) {
        String encoded = null;
        try {
            StringBuilder sb = new StringBuilder();
            for (Map.Entry<String, String> kv : m.entrySet()) {
                sb.append(URLEncoder.encode(kv.getKey(), ENCODING));
                sb.append("=");
                sb.append(URLEncoder.encode(kv.getValue(), ENCODING));
                sb.append("&");
            }
            if (sb.length() > 0)
                sb.deleteCharAt(sb.length() - 1);
            encoded = sb.toString();
            if (null != secret) {
                String sign = sign(encoded, secret.getBytes(ENCODING));
                encoded = sign + "-" + encoded;
            }
        } catch (UnsupportedEncodingException e) {
            // Returns an empty string immediately if failed.
            return "";
        }

        if (encoded.length() > MAX_VALUE_LENGTH)
            // Returns an empty string immediately if the length of the encoded
            // string is more then the capacity of the cookie value.
            return "";

        return encoded;
    }

    private ConcurrentMap<String, String> decode(String value) {
        ConcurrentMap<String, String> m = new ConcurrentHashMap<String, String>();
        String msg;
        if (null != secret) {
            String[] tokens = StringUtils.splitPreserveAllTokens(value, "-", 2);
            if (tokens.length != 2) {
                return m;
            }
            String sign = "";
            try {
                sign = sign(tokens[1], secret.getBytes(ENCODING));
            } catch (UnsupportedEncodingException e) {
                return m;
            }
            if (!safeEquals(sign, tokens[0])) {
                return m;
            }
            msg = tokens[1];
        } else {
            msg = value;
        }

        String[] pairs = StringUtils.splitPreserveAllTokens(msg, "&");
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

    public NewCookie toDiscardingCookie() {
        return new NewCookie(cookie.getName(), "",
                cookie.getPath(), cookie.getDomain(),
                cookie.getVersion(), cookie.getComment(),
                -86400, cookie.isSecure());
    }

    public Optional<String> get(String key) {
        if (data.containsKey(key))
            return Optional.of(data.get(key));
        else
            return Optional.absent();
    }

    public void put(String key, String value) {
        data.put(key, value);
    }

    public Optional<String> remove(String key) {
        Optional<String> opt;
        if (data.containsKey(key))
            opt = Optional.of(data.get(key));
        else
            opt = Optional.absent();
        data.remove(key);
        return opt;
    }

    public void clear() {
        data.clear();
    }

    public boolean isEmpty() {
        return data.isEmpty();
    }

    private static String sign(String message, byte[] key) {
        try {
            Mac mac = Mac.getInstance(MAC_ALGORITHM);
            mac.init(new SecretKeySpec(key, MAC_ALGORITHM));
            return Hex.encodeHexString(mac.doFinal(message.getBytes(ENCODING)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalArgumentException(e);
        } catch (InvalidKeyException e) {
            throw new IllegalArgumentException(e);
        } catch (IllegalStateException e) {
            throw new IllegalArgumentException(e);
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private static boolean safeEquals(String a, String b) {
        if (a.length() != b.length())
            return false;
        int diff = 0;
        // O(N) compare for timing attack
        for (int i = 0; i < a.length(); i++)
            diff |= a.charAt(i) ^ b.charAt(i);
        return diff == 0;
    }
}
