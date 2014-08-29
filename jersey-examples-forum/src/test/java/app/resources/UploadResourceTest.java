package app.resources;

import static org.junit.Assert.*;

import org.junit.Test;

import javax.ws.rs.core.Response;

import com.sun.jersey.core.header.FormDataContentDisposition;

import java.io.File;
import java.io.InputStream;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

import app.core.Config;

public class UploadResourceTest {
    @Test
    public void testPostAndGetImage() throws IOException {
        Config config = Config.load("conf/application");
        File testTmpDir = new File(config.get("path.tmp") + "/test");
        File tmpDir = new File(testTmpDir, "tmp");
        FileUtils.forceMkdir(tmpDir);

        UploadResource resource = new UploadResource(tmpDir.getAbsolutePath());
        FormDataContentDisposition dispo = FormDataContentDisposition
                .name("file")
                .fileName("jersey_logo.png")
                .size(1628)
                .build();
        InputStream is = getClass().getResourceAsStream("/test/upload/jersey_logo.png");
        Response postImageResponse = resource.postImage(is, dispo);
        assertEquals(Response.Status.OK.getStatusCode(), postImageResponse.getStatus());
        String token = (String) postImageResponse.getEntity();
        assertFalse(token.isEmpty());

        Response imageResponse = resource.image(token);
        assertEquals(Response.Status.OK.getStatusCode(), imageResponse.getStatus());

        FileUtils.deleteDirectory(testTmpDir);
    }
}
