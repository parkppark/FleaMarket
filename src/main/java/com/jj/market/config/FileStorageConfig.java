package com.jj.market.config;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import lombok.extern.slf4j.Slf4j;
import java.io.IOException;

@Component
@Slf4j
public class FileStorageConfig implements InitializingBean {

    @Value("${file.upload.directory}")
    private String uploadDirectory;

    @Override
    public void afterPropertiesSet() throws Exception {
        Path uploadPath = Path.of(uploadDirectory);
        if (!Files.exists(uploadPath)) {
            try {
                Files.createDirectories(uploadPath);
                log.info("Upload directory created: {}", uploadPath);
            } catch (IOException e) {
                log.error("Could not create upload directory: {}", uploadPath, e);
                throw e;
            }
        }
    }
}