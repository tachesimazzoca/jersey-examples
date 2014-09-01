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
import app.core.Finder;
import app.core.Uploader;
import app.models.FinderFactory;

public class UploadResourceTest {
    private final File testDirectory;
    private final File tempDirectory;
    private final File uploadDirectory;

    public UploadResourceTest() {
        Config config = Config.load("conf/application");
        testDirectory = new File(config.get("path.tmp") + "/test");
        tempDirectory = new File(testDirectory, "tmp");
        uploadDirectory = new File(testDirectory, "upload");
    }

    @Test
    public void testPostAndGetTempfile() throws IOException {
        FileUtils.forceMkdir(tempDirectory);
        FileUtils.forceMkdir(uploadDirectory);

        Uploader uploader = new Uploader(tempDirectory);
        Finder accountsIconFinder = FinderFactory.createAccountsIconFinder(
                uploadDirectory.getAbsolutePath() + "/accounts/icon");

        UploadResource resource = new UploadResource(uploader, accountsIconFinder);
        FormDataContentDisposition dispo = FormDataContentDisposition
                .name("file")
                .fileName("jersey_logo.png")
                .size(1628)
                .build();
        InputStream is = getClass().getResourceAsStream("/test/upload/jersey_logo.png");
        Response postImageResponse = resource.postTempfile(is, dispo);
        assertEquals(Response.Status.OK.getStatusCode(), postImageResponse.getStatus());
        String token = (String) postImageResponse.getEntity();
        assertFalse(token.isEmpty());
        assertTrue(token.endsWith(".png"));

        Response imageResponse = resource.tempfile(token);
        assertEquals(Response.Status.OK.getStatusCode(), imageResponse.getStatus());

        FileUtils.deleteDirectory(testDirectory);
    }

    @Test
    public void testProfileIcon() throws IOException {
        FileUtils.forceMkdir(tempDirectory);
        FileUtils.forceMkdir(uploadDirectory);

        Uploader uploader = new Uploader(tempDirectory);
        Finder accountsIconFinder = FinderFactory.createAccountsIconFinder(
                uploadDirectory.getAbsolutePath() + "/accounts/icon");

        InputStream input = getClass().getResourceAsStream("/test/upload/jersey_logo.png");
        accountsIconFinder.save(input, "1234", "png");

        UploadResource resource = new UploadResource(uploader, accountsIconFinder);

        Response response = resource.accountsIcon(1234L);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        File f = (File) response.getEntity();
        assertEquals("1234.png", f.getName());

        FileUtils.deleteDirectory(testDirectory);
    }
}
