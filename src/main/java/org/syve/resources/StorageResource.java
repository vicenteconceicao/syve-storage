package org.syve.resources;

import java.io.File;

import org.jboss.resteasy.reactive.PartType;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.RestMulti;
import org.syve.services.StorageService;
import org.syve.utils.SecurityUtils;

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

@Path("/storage")
public class StorageResource {

    @Inject
    StorageService storageService;

    @POST
    @Path("/{bucketName}/upload")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Uni<Response> uploadFile(
            @PathParam("bucketName") String bucketName,
            @RestForm @PartType(MediaType.MULTIPART_FORM_DATA) File file) throws Exception {

        String validatedBucketName = SecurityUtils.validateBucketName(bucketName);
        
        return storageService.uploadFile(validatedBucketName, file);
    }

    @GET
    @Path("/download/{bucketName}/{fileName}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public RestMulti<Buffer> downloadFile(
            @PathParam("bucketName") String bucketName,
            @PathParam("fileName") String fileName) {

        String validatedBucketName = SecurityUtils.validateBucketName(bucketName);
        String validatedFileName = SecurityUtils.validateFileName(fileName);

        return storageService.downloadFile(validatedBucketName, validatedFileName);
    }

    @GET
    @Path("/{bucketName}/{fileName}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public RestMulti<Buffer> getFile(
            @PathParam("bucketName") String bucketName,
            @PathParam("fileName") String fileName) {

        String validatedBucketName = SecurityUtils.validateBucketName(bucketName);
        String validatedFileName = SecurityUtils.validateFileName(fileName);

        return storageService.getFile(validatedBucketName, validatedFileName);
    }
}
