package org.syve.services.Impl;

import java.io.File;
import java.net.URI;

import org.jboss.resteasy.reactive.RestMulti;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.syve.services.StorageService;
import org.syve.utils.CommonResource;
import org.syve.utils.FileUtils;
import org.syve.utils.SecurityUtils;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.core.buffer.Buffer;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import mutiny.zero.flow.adapters.AdaptersToFlow;
import software.amazon.awssdk.core.async.AsyncRequestBody;
import software.amazon.awssdk.core.async.AsyncResponseTransformer;
import software.amazon.awssdk.services.s3.S3AsyncClient;

@ApplicationScoped
public class S3StorageServiceImpl extends CommonResource implements StorageService {

    private static final Logger LOG = LoggerFactory.getLogger(S3StorageServiceImpl.class);

    @Inject
    private S3AsyncClient s3;

    @Context
    UriInfo uriInfo;

    private static final long MAX_FILE_SIZE = 50 * 1024 * 1024; // 50MB

    @Override
    public Uni<Response> uploadFile(String bucketName, File file) {

        if (file == null || !file.exists()) {
            return Uni.createFrom()
                    .item(Response.status(Response.Status.BAD_REQUEST)
                            .entity("File must not be empty").build());
        }

        long fileSize = file.length();
        if (!SecurityUtils.isValidFileSize(fileSize, MAX_FILE_SIZE)) {
            LOG.warn("File size {} exceeds maximum allowed size {}", fileSize, MAX_FILE_SIZE);
            return Uni.createFrom()
                    .item(Response.status(413) // 413 Payload Too Large
                            .entity("File size exceeds maximum allowed size (50MB)").build());
        }

        Optional<String> mimeType = FileUtils.detectMimeType(file);
        
        if (mimeType.isEmpty() || !SecurityUtils.isAllowedMimeType(mimeType.get())) {
            LOG.warn("File has disallowed MIME type: {}", mimeType.orElse("unknown"));
            return Uni.createFrom()
                    .item(Response.status(Response.Status.UNSUPPORTED_MEDIA_TYPE)
                            .entity("File type not allowed").build());
        }

        String fileExtension = mimeType.map(FileUtils::getExtensionFromMimeType)
                .orElse(".dat");

        String fileName = UUID.randomUUID().toString() + fileExtension;

        LOG.info("File [{}] received to be uploaded with MIME type [{}].", fileName, mimeType.get());

        return Uni.createFrom()
                .completionStage(() -> s3.putObject(
                        buildPutRequest(bucketName, fileName, mimeType.orElse("application/octet-stream")),
                        AsyncRequestBody.fromFile(file)))
                .onItem().ignore()
                .andSwitchTo(Uni.createFrom().item(() -> {
                    URI fileUri = uriInfo.getBaseUriBuilder()
                            .path("storage")
                            .path(bucketName)
                            .path(fileName)
                            .build();
                    
                    return Response.created(fileUri)
                            .header("Access-Control-Expose-Headers", "Location")
                            .build();
                }))
                .onFailure().recoverWithItem(th -> {
                    LOG.error("Error uploading file [{}]: {}", fileName, th.getMessage());
                    return Response.serverError().build();
                });
    }

    @Override
    public RestMulti<Buffer> downloadFile(String bucketName, String fileName) {
        return getFileResponse(bucketName, fileName, "attachment");
    }

    @Override
    public RestMulti<Buffer> getFile(String bucketName, String fileName) {
        return getFileResponse(bucketName, fileName, "inline");
    }

    private static Buffer toBuffer(ByteBuffer bytebuffer) {
        byte[] result = new byte[bytebuffer.remaining()];
        bytebuffer.get(result);
        return Buffer.buffer(result);
    }

    private RestMulti<Buffer> getFileResponse(String bucketName, String fileName, String dispositionType) {
        String safeFileName = SecurityUtils.sanitizeFileNameForHeader(fileName);
        
        return RestMulti.fromUniResponse(
                Uni.createFrom().completionStage(() -> s3.getObject(buildGetRequest(bucketName, fileName),
                        AsyncResponseTransformer.toPublisher())),
                response -> Multi.createFrom()
                        .safePublisher(AdaptersToFlow.publisher((Publisher<ByteBuffer>) response))
                        .map(S3StorageServiceImpl::toBuffer),
                response -> Map.of(
                        "Content-Disposition", List.of(dispositionType + "; filename=\"" + safeFileName + "\""),
                        "Content-Type", List.of(response.response().contentType())));
    }
}
