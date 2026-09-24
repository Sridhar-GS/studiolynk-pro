package com.studiolynk.service.storage;

import java.io.InputStream;

/**
 * Pluggable Storage Service supporting AWS S3 and Local Development fallback.
 */
public interface StorageService {
    /**
     * Uploads an object with the specified key and metadata.
     *
     * @param key unique storage key (e.g. portfolio/1/2/photo.jpg)
     * @param inputStream content stream
     * @param contentLength size in bytes
     * @param contentType MIME type (e.g. image/jpeg, image/png)
     * @return public access URL for the uploaded object
     */
    String uploadFile(String key, InputStream inputStream, long contentLength, String contentType);

    /**
     * Deletes an object by key.
     *
     * @param key unique storage key
     */
    void deleteFile(String key);

    /**
     * Resolves the access URL for a given storage key.
     *
     * @param key unique storage key
     * @return URL string
     */
    String getFileUrl(String key);
}
