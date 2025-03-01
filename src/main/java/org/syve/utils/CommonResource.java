package org.syve.utils;

import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

abstract public class CommonResource {
   
    protected PutObjectRequest buildPutRequest(String bucketName, String fileName, String mimeType) {
        return PutObjectRequest.builder()
                .bucket(bucketName)
                .key(fileName)
                .contentType(mimeType)
                .build();
    }

    protected GetObjectRequest buildGetRequest(String bucketName, String fileName) {
        return GetObjectRequest.builder()
                .bucket(bucketName)
                .key(fileName)
                .build();
    }
}
