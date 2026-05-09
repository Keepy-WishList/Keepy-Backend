package com.keepy.domain.analysis.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.keepy.domain.analysis.dto.AnalysisResponse;
import com.keepy.domain.item.dto.ItemDetailResponse;
import com.keepy.domain.item.dto.ItemSaveRequest;
import com.keepy.domain.item.entity.Category;
import com.keepy.domain.item.service.ItemService;
import com.keepy.global.exception.CustomException;
import com.keepy.global.exception.ErrorCode;
import com.keepy.infra.gcs.GcsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnalysisService {

    private final RestClient openAiRestClient;
    private final GcsService gcsService;
    private final ObjectMapper objectMapper;
    private final ItemService itemService;

    @Value("${openai.model}")
    private String model;

    private static final String ANALYSIS_PROMPT = """
            You are a product identification and price comparison AI for a Korean shopping app called Keepy.

            Analyze the product in this image and return a JSON object with this exact structure:
            {
              "productName": "exact product name in Korean or English",
              "brand": "brand name",
              "category": "one of: COSMETICS, CLOTHES, SHOES, TECH, FOOD, OTHER",
              "estimatedPrice": numeric price in KRW (no currency symbol),
              "description": "brief product description in Korean (2-3 sentences)",
              "shoppingOptions": [
                {
                  "siteName": "shopping site name",
                  "siteUrl": "product URL on that site",
                  "price": numeric price in KRW,
                  "deliveryFee": "delivery fee info e.g. 무료배송 or 3,000원 or 오프라인 구매"
                }
              ]
            }

            Find this product on these Korean shopping sites in order of priority: 지그재그, 에이블리, 쿠팡, 올리브영, 네이버쇼핑.
            Try to find 5~6 purchase options total.
            If the product is not sold online, find offline store locations in Korea (e.g. department stores, brand stores) and include them in shoppingOptions with deliveryDays as null.
            If you cannot identify the product clearly, make your best estimate.
            CRITICAL: Return ONLY the raw JSON object. Do NOT wrap it in markdown code blocks. Do NOT add any explanation before or after. Your entire response must be valid JSON starting with { and ending with }.
            """;

    public ItemDetailResponse analyze(Long userId, MultipartFile image) {
        validateImage(image);

        String screenshotUrl = gcsService.upload(image, "screenshots");

        String base64Image = encodeImageToBase64(image);
        String mimeType = image.getContentType() != null ? image.getContentType() : "image/jpeg";

        String rawResponse = callOpenAi(base64Image, mimeType);
        AnalysisResponse result = parseResponse(rawResponse);

        Category category = parseCategory(result.category());

        List<ItemSaveRequest.ShoppingOptionSaveRequest> shoppingOptions = null;
        if (result.shoppingOptions() != null) {
            shoppingOptions = result.shoppingOptions().stream()
                    .map(opt -> new ItemSaveRequest.ShoppingOptionSaveRequest(
                            opt.siteName(),
                            opt.siteUrl(),
                            opt.price(),
                            opt.deliveryFee()
                    ))
                    .toList();
        }

        ItemSaveRequest saveRequest = new ItemSaveRequest(
                result.productName(),
                result.brand(),
                category,
                result.estimatedPrice(),
                null,
                screenshotUrl,
                result.description(),
                null,
                shoppingOptions
        );

        return itemService.save(userId, saveRequest);
    }

    private Category parseCategory(String categoryStr) {
        if (categoryStr == null) return Category.OTHER;
        try {
            return Category.valueOf(categoryStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            return Category.OTHER;
        }
    }

    private void validateImage(MultipartFile image) {
        if (image == null || image.isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_IMAGE);
        }
        String contentType = image.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new CustomException(ErrorCode.INVALID_IMAGE);
        }
    }

    private String encodeImageToBase64(MultipartFile image) {
        try {
            return Base64.getEncoder().encodeToString(image.getBytes());
        } catch (IOException e) {
            throw new CustomException(ErrorCode.ANALYSIS_FAILED);
        }
    }

    private String callOpenAi(String base64Image, String mimeType) {
        Map<String, Object> requestBody = Map.of(
                "model", model,
                "messages", List.of(
                        Map.of(
                                "role", "user",
                                "content", List.of(
                                        Map.of(
                                                "type", "image_url",
                                                "image_url", Map.of(
                                                        "url", "data:" + mimeType + ";base64," + base64Image
                                                )
                                        ),
                                        Map.of(
                                                "type", "text",
                                                "text", ANALYSIS_PROMPT
                                        )
                                )
                        )
                ),
                "max_tokens", 1500
        );

        try {
            String responseBody = openAiRestClient.post()
                    .uri("/v1/chat/completions")
                    .body(requestBody)
                    .retrieve()
                    .body(String.class);

            JsonNode root = objectMapper.readTree(responseBody);
            return root.path("choices").get(0).path("message").path("content").asText();
        } catch (Exception e) {
            log.error("OpenAI API call failed", e);
            throw new CustomException(ErrorCode.ANALYSIS_FAILED);
        }
    }

    private AnalysisResponse parseResponse(String json) {
        try {
            return objectMapper.readValue(json, AnalysisResponse.class);
        } catch (Exception e) {
            log.error("Failed to parse OpenAI response: {}", json, e);
            throw new CustomException(ErrorCode.ANALYSIS_FAILED);
        }
    }
}
