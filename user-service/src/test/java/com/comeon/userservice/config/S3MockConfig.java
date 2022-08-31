package com.comeon.userservice.config;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.AnonymousAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import io.findify.s3mock.S3Mock;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

@Slf4j
@Profile("test")
@TestConfiguration
public class S3MockConfig {

    @Value("${cloud.aws.region.static}")
    String region;

    @Value("${cloud.aws.s3.bucket}")
    String bucket;

    @Bean
    public S3Mock s3Mock() {
        return new S3Mock.Builder()
                .withPort(8001)
                .withInMemoryBackend()
                .build();
    }

    @Bean
    @Primary
    public AmazonS3 amazonS3(S3Mock s3Mock) {
        log.info("[amazonS3Config] start");
        s3Mock.start();

        AwsClientBuilder.EndpointConfiguration endpoint =
                new AwsClientBuilder.EndpointConfiguration("http://127.0.0.1:8001", region);

        AmazonS3 client = AmazonS3ClientBuilder
                .standard()
                .withPathStyleAccessEnabled(true)
                .withEndpointConfiguration(endpoint)
                .withCredentials(
                        new AWSStaticCredentialsProvider(
                                new AnonymousAWSCredentials()
                        )
                )
                .build();

        client.createBucket(bucket);

        log.info("[amazonS3Config] end");
        return client;
    }

}
