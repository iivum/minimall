package com.minimall.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/upload")
@Tag(name = "Upload", description = "File upload APIs")
public class ImageUploadController {

    private static final String UPLOAD_DIR = "uploads/";
    private static final long MAX_FILE_SIZE = 2 * 1024 * 1024; // 2MB

    @PostMapping("/image")
    @Operation(summary = "Upload product image")
    public ResponseEntity<Map<String, String>> uploadImage(@RequestParam("file") MultipartFile file) {
        Map<String, String> response = new HashMap<>();

        if (file.isEmpty()) {
            response.put("error", "请选择要上传的文件");
            return ResponseEntity.badRequest().body(response);
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            response.put("error", "文件大小不能超过 2MB");
            return ResponseEntity.badRequest().body(response);
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            response.put("error", "只能上传图片文件");
            return ResponseEntity.badRequest().body(response);
        }

        try {
            // Create uploads directory if it doesn't exist
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Generate unique filename
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String filename = UUID.randomUUID().toString() + extension;

            // Save file
            Path filePath = uploadPath.resolve(filename);
            Files.copy(file.getInputStream(), filePath);

            // Return the URL path
            String url = "/uploads/" + filename;
            response.put("url", url);
            return ResponseEntity.ok(response);

        } catch (IOException e) {
            response.put("error", "上传失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @PostMapping("/image/base64")
    @Operation(summary = "Upload image as base64")
    public ResponseEntity<Map<String, String>> uploadImageBase64(@RequestBody Map<String, String> request) {
        Map<String, String> response = new HashMap<>();
        String base64Data = request.get("image");

        if (base64Data == null || base64Data.isBlank()) {
            response.put("error", "请提供图片数据");
            return ResponseEntity.badRequest().body(response);
        }

        try {
            // Remove data URL prefix if present
            if (base64Data.contains(",")) {
                base64Data = base64Data.split(",")[1];
            }

            byte[] imageBytes = Base64.getDecoder().decode(base64Data);

            if (imageBytes.length > MAX_FILE_SIZE) {
                response.put("error", "图片大小不能超过 2MB");
                return ResponseEntity.badRequest().body(response);
            }

            // Create uploads directory if it doesn't exist
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Generate unique filename
            String filename = UUID.randomUUID().toString() + ".png";
            Path filePath = uploadPath.resolve(filename);
            Files.write(filePath, imageBytes);

            // Return the URL path
            String url = "/uploads/" + filename;
            response.put("url", url);
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            response.put("error", "无效的Base64图片数据");
            return ResponseEntity.badRequest().body(response);
        } catch (IOException e) {
            response.put("error", "上传失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
}