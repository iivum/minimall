package com.minimall.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        Schema<?> errorSchema = new Schema<>();
        errorSchema.setType("object");
        errorSchema.addProperty("error", createProperty("string", "Forbidden"));
        errorSchema.addProperty("message", createProperty("string", "您没有权限访问此资源"));

        return new OpenAPI()
            .info(new Info()
                .title("MiniMall API")
                .version("1.0.0")
                .description("MVP WeChat MiniMall Backend API"))
            .components(new Components()
                .addSchemas("ErrorResponse", errorSchema));
    }

    private static Schema<?> createProperty(String type, String example) {
        Schema<?> prop = new Schema<>();
        prop.setType(type);
        prop.setExample(example);
        return prop;
    }

    public static ApiResponses createErrorResponses() {
        return new ApiResponses()
            .addApiResponse("400", createErrorResponse("Bad Request", "Validation failed"))
            .addApiResponse("403", createErrorResponse("Forbidden", "您没有权限访问此资源"))
            .addApiResponse("404", createErrorResponse("Not Found", "商品不存在"))
            .addApiResponse("500", createErrorResponse("Internal Server Error", "服务器繁忙，请稍后重试"));
    }

    private static ApiResponse createErrorResponse(String error, String message) {
        MediaType mediaType = new MediaType();
        mediaType.setExample(Map.of("error", error, "message", message));
        Content content = new Content();
        content.addMediaType("application/json", mediaType);
        ApiResponse response = new ApiResponse();
        response.setDescription(error + " - 异常响应");
        response.setContent(content);
        return response;
    }
}
