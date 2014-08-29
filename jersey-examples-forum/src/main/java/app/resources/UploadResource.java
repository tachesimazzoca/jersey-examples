package app.resources;

import javax.ws.rs.*;
import javax.ws.rs.core.*;

import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataParam;

import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableList;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import app.core.FileStreamStorage;

@Path("/api/upload")
public class UploadResource {
    private static long MAX_UPLOAD_SIZE = 1000000;
    private static final Map<String, List<String>> SUPPORTED_IMAGES =
            ImmutableMap.<String, List<String>> of(
                    "image/jpeg", ImmutableList.of("jpg", "jpeg"),
                    "image/gif", ImmutableList.of("gif"),
                    "image/png", ImmutableList.of("png"));

    private final FileStreamStorage tmpStorage;

    public UploadResource(FileStreamStorage tmpStorage) {
        this.tmpStorage = tmpStorage;
    }

    public UploadResource(String tmpDir) {
        this.tmpStorage = new FileStreamStorage(new File(tmpDir));
    }

    private Optional<String> detectContentType(String filename) {
        String ext = FilenameUtils.getExtension(filename);
        for (Map.Entry<String, List<String>> format : SUPPORTED_IMAGES.entrySet()) {
            if (format.getValue().contains(ext)) {
                return Optional.of(format.getKey());
            }
        }
        return Optional.absent();
    }

    @GET
    @Path("image/{filename}")
    @Produces("image/*")
    public Response image(@PathParam("filename") String filename) throws IOException {
        Optional<String> contentType = detectContentType(filename);
        if (!contentType.isPresent())
            return Response.status(Response.Status.FORBIDDEN).build();
        Optional<InputStream> in = tmpStorage.read(FilenameUtils.getBaseName(filename));
        if (!in.isPresent())
            return Response.status(Response.Status.NOT_FOUND).build();

        return Response.ok(in.get()).type(contentType.get()).build();
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

        Optional<String> contentType = detectContentType(fileName);
        if (!contentType.isPresent())
            return Response.status(Response.Status.FORBIDDEN).entity(
                    "Unsupported file format").build();

        String extension = SUPPORTED_IMAGES.get(contentType.get()).get(0);
        String key = tmpStorage.create(file);
        Optional<File> tmpfile = tmpStorage.getFile(key);
        if (!tmpfile.isPresent())
            return Response.status(Response.Status.FORBIDDEN).entity(
                    "Uploading failed").build();
        if (FileUtils.sizeOf(tmpfile.get()) > MAX_UPLOAD_SIZE) {
            tmpStorage.delete(key);
            return Response.status(Response.Status.FORBIDDEN).entity(
                    String.format("The size of the file must be less than %,d KBytes",
                            MAX_UPLOAD_SIZE / 1000)).build();
        }

        // TODO: Avoid invalid images by parsing.

        return Response.ok(key + "." + extension).build();
    }
}
