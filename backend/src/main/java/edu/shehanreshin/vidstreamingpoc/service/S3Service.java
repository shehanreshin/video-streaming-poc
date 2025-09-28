package edu.shehanreshin.vidstreamingpoc.service;

public interface S3Service {
    String getPresignedUrl(String fileName);
}
