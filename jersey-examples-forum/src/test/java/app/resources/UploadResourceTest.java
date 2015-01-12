package app.resources;

import app.core.util.FileHelper;
import app.models.FileHelperFactory;
import app.models.TempFileHelper;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import javax.ws.rs.core.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.*;

public class UploadResourceTest {
    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Test
    public void testPostAndGetTempFile() throws IOException {
        File tempDir = tempFolder.newFolder("temp");
        File uploadDir = tempFolder.newFolder("upload");

        TempFileHelper tempFileHelper = new TempFileHelper(tempDir);
        FileHelper accountsIconFinder = FileHelperFactory.createAccountsIconFinder(uploadDir);

        UploadResource resource = new UploadResource(tempFileHelper, accountsIconFinder);
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
    }

    @Test
    public void testProfileIcon() throws IOException {
        File tempDir = tempFolder.newFolder("temp");
        File uploadDir = tempFolder.newFolder("upload");

        TempFileHelper tempFileHelper = new TempFileHelper(tempDir);
        FileHelper accountsIconFinder = FileHelperFactory.createAccountsIconFinder(uploadDir);

        InputStream input = getClass().getResourceAsStream("/test/upload/jersey_logo.png");
        accountsIconFinder.save(input, "1234", "png");

        UploadResource resource = new UploadResource(tempFileHelper, accountsIconFinder);

        Response response = resource.accountsIcon(1234L);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        File f = (File) response.getEntity();
        assertEquals("1234.png", f.getName());
    }
}
