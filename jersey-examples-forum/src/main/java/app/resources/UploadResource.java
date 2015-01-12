package app.resources;

import javax.ws.rs.*;
import javax.ws.rs.core.*;

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

import app.models.TempFileHelper;
import app.core.util.FileHelper;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

@Path("/api/upload")
public class UploadResource {
    private static final long MAX_UPLOAD_SIZE = 1000000;

    private static final Map<String, List<String>> SUPPORTED_TYPES =
            ImmutableMap.<String, List<String>> of(
                    "image/jpeg", ImmutableList.of("jpg", "jpeg"),
                    "image/gif", ImmutableList.of("gif"),
                    "image/png", ImmutableList.of("png"));

    private static final CacheControl NO_CACHE;
    static {
        NO_CACHE = new CacheControl();
        NO_CACHE.setNoCache(true);
    }

    private final TempFileHelper tempFileHelper;
    private final FileHelper accountsIconFinder;

    public UploadResource(TempFileHelper tempFileHelper, FileHelper accountsIconFinder) {
        this.tempFileHelper = tempFileHelper;
        this.accountsIconFinder = accountsIconFinder;
    }

    private Optional<String> detectContentType(String filename) {
        String ext = FilenameUtils.getExtension(filename);
        for (Map.Entry<String, List<String>> format : SUPPORTED_TYPES.entrySet()) {
            if (format.getValue().contains(ext)) {
                return Optional.of(format.getKey());
            }
        }
        return Optional.absent();
    }

    @GET
    @Path("accounts/icon/{id}")
    public Response accountsIcon(@PathParam("id") Long id) {
        Optional<FileHelper.Result> resultOpt = accountsIconFinder.find(id.toString());
        if (!resultOpt.isPresent())
            return Response.status(Response.Status.NOT_FOUND).build();
        FileHelper.Result result = resultOpt.get();
        return Response.ok(result.getFile())
                .type(result.getMimeType())
                .cacheControl(NO_CACHE)
                .build();
    }

    @GET
    @Path("tempfile/{filename}")
    public Response tempfile(@PathParam("filename") String filename) throws IOException {
        Optional<String> contentType = detectContentType(filename);
        if (!contentType.isPresent())
            return Response.status(Response.Status.FORBIDDEN).build();
        Optional<File> tempfile = tempFileHelper.read(filename);
        if (!tempfile.isPresent())
            return Response.status(Response.Status.NOT_FOUND).build();
        return Response.ok(tempfile.get()).type(contentType.get())
                .cacheControl(NO_CACHE).build();
    }

    @POST
    @Path("tempfile")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.TEXT_PLAIN)
    public Response postTempfile(
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

        String extension = SUPPORTED_TYPES.get(contentType.get()).get(0);
        File tmpfile = tempFileHelper.create(file, "tmp-", "." + extension);

        if (FileUtils.sizeOf(tmpfile) > MAX_UPLOAD_SIZE) {
            FileUtils.deleteQuietly(tmpfile);
            return Response.status(Response.Status.FORBIDDEN).entity(
                    String.format("The size of the file must be less than %,d KBytes",
                            MAX_UPLOAD_SIZE / 1000)).build();
        }

        // TODO: Avoid invalid images by parsing.

        return Response.ok(tmpfile.getName()).build();
    }
}
