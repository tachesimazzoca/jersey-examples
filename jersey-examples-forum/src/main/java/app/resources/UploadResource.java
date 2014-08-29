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

@Path("/api/upload")
public class UploadResource {
    private static long MAX_UPLOAD_SIZE = 5000;
    private static final String[] SUPPORTED_IMAGES = { ".jpg", ".png", ".gif" };
    private final File tmpDir;

    public UploadResource(File tmpDir) {
        this.tmpDir = tmpDir;
    }

    public UploadResource(String tmpDir) {
        this.tmpDir = new File(tmpDir);
    }

    @GET
    @Path("image/{token}")
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
    @Path("image")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.TEXT_PLAIN)
    public Response postImage(
            @FormDataParam("file") InputStream file,
            @FormDataParam("file") FormDataContentDisposition disposition)
            throws IOException {
        if (file == null)
            return Response.status(Response.Status.FORBIDDEN).entity(
                    "The file is empty").build();

        String fileName = disposition.getFileName();
        if (fileName == null)
            return Response.status(Response.Status.FORBIDDEN).entity(
                    "The file has no content disposition").build();

        String suffix = null;
        for (int i = 0; i < SUPPORTED_IMAGES.length; i++) {
            if (fileName.endsWith(SUPPORTED_IMAGES[i])) {
                suffix = SUPPORTED_IMAGES[i];
                break;
            }
        }
        if (suffix == null)
            return Response.status(Response.Status.FORBIDDEN).entity(
                    "Unsupported file format").build();

        String token = UUID.randomUUID().toString() + suffix;
        File tmpfile = new File(tmpDir, token);
        FileUtils.copyInputStreamToFile(file, tmpfile);
        if (FileUtils.sizeOf(tmpfile) > MAX_UPLOAD_SIZE) {
            FileUtils.deleteQuietly(tmpfile);
            return Response.status(Response.Status.FORBIDDEN).entity(
                    String.format("The size of the file must be less than %,d KBytes",
                            MAX_UPLOAD_SIZE / 1000)).build();
        }

        // TODO: Avoid invalid images by parsing.

        return Response.ok(token).build();
    }
}
