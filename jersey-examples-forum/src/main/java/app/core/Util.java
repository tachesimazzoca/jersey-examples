package app.core;

import com.google.common.collect.ImmutableMap;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javax.ws.rs.core.UriBuilderException;
import javax.ws.rs.core.UriInfo;

import java.net.URI;
import java.net.URISyntaxException;

import java.util.Map;

public class Util {
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

    public static String objectToBase64(Object obj) {
        String ser = null;
        ByteArrayOutputStream os = null;
        ObjectOutputStream oos = null;
        try {
            os = new ByteArrayOutputStream();
            oos = new ObjectOutputStream(os);
            oos.writeObject(obj);
            ser = Base64.encodeBase64String(os.toByteArray());
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        } finally {
            IOUtils.closeQuietly(oos);
            IOUtils.closeQuietly(os);
        }
        return ser;
    }

    @SuppressWarnings("unchecked")
    public static <T> T base64ToObject(String ser, Class<T> type) {
        T obj = null;
        ByteArrayInputStream is = null;
        ObjectInputStream ois = null;
        try {
            is = new ByteArrayInputStream(Base64.decodeBase64(ser));
            ois = new ObjectInputStream(is);
            obj = (T) ois.readObject();
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException(e);
        } finally {
            IOUtils.closeQuietly(ois);
            IOUtils.closeQuietly(is);
        }
        return obj;
    }

    public static boolean safeEquals(String a, String b) {
        if (a.length() != b.length())
            return false;
        int diff = 0;
        // O(N) compare for timing attack
        for (int i = 0; i < a.length(); i++)
            diff |= a.charAt(i) ^ b.charAt(i);
        return diff == 0;
    }

    public static URI safeURI(UriInfo uinfo, String path) {
        if (!path.startsWith("/") || path.isEmpty())
            throw new IllegalArgumentException("The parameter path must be an absolute path.");
        URI uri = null;
        try {
            uri = new URI(path);
        } catch (UriBuilderException e) {
            throw new IllegalArgumentException(e);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(e);
        }
        return uinfo.getBaseUriBuilder().uri(uri).build();
    }
}
