package app.resources;

import javax.ws.rs.*;
import javax.ws.rs.core.*;

import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataParam;

import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import java.util.UUID;

import javax.activation.MimetypesFileTypeMap;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.io.FileUtils;

@Path("/api/uploader")
public class UploaderResource {
    private static long MAX_UPLOAD_SIZE = 2000;
    private static final String[] SUPPORTED_IMAGES = { ".jpg", ".png", ".gif" };
    private final File tmpDir;

    public UploaderResource(File tmpDir) {
        this.tmpDir = tmpDir;
    }

    public UploaderResource(String tmpDir) {
        this.tmpDir = new File(tmpDir);
    }

    @GET
    @Path("images/{token}")
    @Produces("image/*")
    public Response image(@PathParam("token") String token) throws IOException {
        if (!StringUtils.endsWithAny(token, SUPPORTED_IMAGES))
            return Response.status(Response.Status.FORBIDDEN).build();

        File f = new File(tmpDir, token);
        if (!f.exists())
            return Response.status(Response.Status.NOT_FOUND).build();

        String mt = new MimetypesFileTypeMap().getContentType(f);
        return Response.ok(f, mt).build();
    }

    @POST
    @Path("images")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.TEXT_PLAIN)
    public Response postImage(
            @FormDataParam("file") InputStream file,
            @FormDataParam("file") FormDataContentDisposition disposition)
            throws IOException {
        String fileName = disposition.getFileName();
        String suffix = null;
        for (int i = 0; i < SUPPORTED_IMAGES.length; i++) {
            if (fileName.endsWith(SUPPORTED_IMAGES[i])) {
                suffix = SUPPORTED_IMAGES[i];
                break;
            }
        }
        if (suffix == null)
            return Response.status(Response.Status.FORBIDDEN).build();
        if (disposition.getSize() > MAX_UPLOAD_SIZE)
            return Response.status(Response.Status.FORBIDDEN).build();

        String token = UUID.randomUUID().toString() + suffix;
        File tmpfile = new File(tmpDir, token);
        // TODO: Avoid invalid images by parsing.
        FileUtils.copyInputStreamToFile(file, tmpfile);
        return Response.ok(token).build();
    }
}
