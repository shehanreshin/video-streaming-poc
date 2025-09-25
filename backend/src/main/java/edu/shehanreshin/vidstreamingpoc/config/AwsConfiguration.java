package edu.shehanreshin.vidstreamingpoc.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Configuration
public class AwsConfiguration {
    @Value("${config.aws.region}")
    private String configAwsRegion;

    @Value("${config.aws.access-key-id}")
    private String configAwsAccessKeyId;
    @Value("${config.aws.secret-access-key}")
    private String configAwsSecretAccessKey;

    @Bean
    public S3Client s3Client() {
        return S3Client.builder()
                .region(Region.of(configAwsRegion))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(configAwsAccessKeyId, configAwsSecretAccessKey)
                ))
                .build();
    }

    @Bean
    public S3Presigner s3Presigner() {
        return S3Presigner.builder()
                .region(Region.of(configAwsRegion))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(configAwsAccessKeyId, configAwsSecretAccessKey)
                ))
                .build();
    }
}
