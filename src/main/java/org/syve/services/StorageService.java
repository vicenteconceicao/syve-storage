package org.syve.services;

import java.io.File;

import org.jboss.resteasy.reactive.RestMulti;

import io.smallrye.mutiny.Uni;
import io.vertx.core.buffer.Buffer;
import jakarta.ws.rs.core.Response;

public interface StorageService {
    Uni<Response> uploadFile(String bucketName, File file);
    RestMulti<Buffer> downloadFile(String bucketName, String fileName);
    RestMulti<Buffer> getFile(String bucketName, String fileName);
}
