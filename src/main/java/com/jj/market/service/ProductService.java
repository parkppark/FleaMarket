package com.jj.market.service;

import com.jj.market.entity.Product;
import com.jj.market.entity.User;
import com.jj.market.repository.ProductRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;
    private final UserService userService;
    
    @Value("${file.upload.directory}")
    private String uploadDirectory;

    @Transactional
    public Product registerProduct(String productName, String category, 
                             double price, String condition, 
                             String description, MultipartFile[] files,
                             String userID) throws IOException {
    
    // 현재 로그인한 사용자 정보 가져오기
    User user = userService.getCurrentUser(userID);
    
    // 이미지 파일 처리
    List<String> imageUrls = new ArrayList<>();
    
    // 업로드 디렉토리 존재 확인 및 생성
    Path uploadPath = Path.of(uploadDirectory);
    if (!Files.exists(uploadPath)) {
        Files.createDirectories(uploadPath);
    }
    
    for (MultipartFile file : files) {
        if (!file.isEmpty()) {
            String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
            Path filePath = uploadPath.resolve(fileName);
            log.info("파일 저장 경로: {}", filePath.toAbsolutePath());
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            imageUrls.add(fileName);
        }
    }

    // 상품 엔티티 생성 및 저장
    Product product = Product.builder()
            .p_name(productName)
            .p_content(description)
            .p_price(price)
            .p_imageUrl(String.join(",", imageUrls))
            .p_category(category)
            .p_status(condition)
            .user(user)
            .createDate(LocalDateTime.now())
            .build();

    return productRepository.save(product);
}

    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(Path.of(uploadDirectory));
        } catch (IOException e) {
            log.error("Failed to create upload directory: {}", e.getMessage());
        }
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Product getProductById(long id) {
        return productRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다. ID: " + id));
    }


}