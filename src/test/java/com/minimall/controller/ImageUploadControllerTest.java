package com.minimall.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ImageUploadControllerTest {

    private final ImageUploadController controller = new ImageUploadController();

    @Test
    void uploadImage_returnsBadRequest_whenFileIsEmpty() {
        MockMultipartFile emptyFile = new MockMultipartFile(
            "file", "test.jpg", "image/jpeg", new byte[0]);

        ResponseEntity<Map<String, String>> response = controller.uploadImage(emptyFile);

        assertEquals(400, response.getStatusCode().value());
        assertEquals("请选择要上传的文件", response.getBody().get("error"));
    }

    @Test
    void uploadImage_returnsBadRequest_whenFileExceedsMaxSize() {
        byte[] largeContent = new byte[3 * 1024 * 1024]; // 3MB
        MockMultipartFile largeFile = new MockMultipartFile(
            "file", "test.jpg", "image/jpeg", largeContent);

        ResponseEntity<Map<String, String>> response = controller.uploadImage(largeFile);

        assertEquals(400, response.getStatusCode().value());
        assertEquals("文件大小不能超过 2MB", response.getBody().get("error"));
    }

    @Test
    void uploadImage_returnsBadRequest_whenContentTypeIsNotImage() {
        MockMultipartFile txtFile = new MockMultipartFile(
            "file", "test.txt", "text/plain", "content".getBytes());

        ResponseEntity<Map<String, String>> response = controller.uploadImage(txtFile);

        assertEquals(400, response.getStatusCode().value());
        assertEquals("只能上传图片文件", response.getBody().get("error"));
    }

    @Test
    void uploadImageBase64_returnsBadRequest_whenDataIsNull() {
        Map<String, String> request = new HashMap<>();
        request.put("image", null);
        ResponseEntity<Map<String, String>> response = controller.uploadImageBase64(request);

        assertEquals(400, response.getStatusCode().value());
        assertEquals("请提供图片数据", response.getBody().get("error"));
    }

    @Test
    void uploadImageBase64_returnsBadRequest_whenDataIsBlank() {
        ResponseEntity<Map<String, String>> response = controller.uploadImageBase64(Map.of("image", "   "));

        assertEquals(400, response.getStatusCode().value());
        assertEquals("请提供图片数据", response.getBody().get("error"));
    }

    @Test
    void uploadImageBase64_returnsBadRequest_whenBase64IsInvalid() {
        ResponseEntity<Map<String, String>> response = controller.uploadImageBase64(Map.of("image", "not-valid-base64!!!"));

        assertEquals(400, response.getStatusCode().value());
        assertEquals("无效的Base64图片数据", response.getBody().get("error"));
    }
}