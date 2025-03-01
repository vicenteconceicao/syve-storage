package org.syve.resources;

import java.io.File;

import org.jboss.resteasy.reactive.PartType;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.RestMulti;
import org.syve.services.StorageService;

import io.smallrye.mutiny.Uni;
import io.vertx.core.buffer.Buffer;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/api/storage")
public class StorageResource {

    @Inject
    StorageService storageService;

    @POST
    @Path("/{bucketName}/upload")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Uni<Response> uploadFile(
            @PathParam("bucketName") String bucketName,
            @RestForm @PartType(MediaType.MULTIPART_FORM_DATA) File file) throws Exception {

        return storageService.uploadFile(bucketName, file);
    }

    @GET
    @Path("/download/{bucketName}/{fileName}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public RestMulti<Buffer> downloadFile(
            @PathParam("bucketName") String bucketName,
            @PathParam("fileName") String fileName) {

        return storageService.downloadFile(bucketName, fileName);
    }

    @GET
    @Path("/{bucketName}/{fileName}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public RestMulti<Buffer> getFile(
            @PathParam("bucketName") String bucketName,
            @PathParam("fileName") String fileName) {

        return storageService.getFile(bucketName, fileName);
    }
}
