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

        UploadResource resource = new UploadResource(tmpDir);
        FormDataContentDisposition dispo = FormDataContentDisposition
                .name("file")
                .fileName("jersey_logo.png")
                .size(1628)
                .build();
        InputStream is = getClass().getResourceAsStream("/test/upload/jersey_logo.png");
        Response response = resource.postImage(is, dispo);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        String body = (String) response.getEntity();
        assertFalse(body.isEmpty());

        FileUtils.deleteDirectory(testTmpDir);
    }
}
