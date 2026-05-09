package com.keepy.domain.analysis.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.keepy.domain.analysis.dto.AnalysisResponse;
import com.keepy.domain.item.dto.ItemDetailResponse;
import com.keepy.domain.item.dto.ItemSaveRequest;
import com.keepy.domain.item.entity.Category;
import com.keepy.domain.item.service.ItemService;
import com.keepy.domain.user.entity.User;
import com.keepy.domain.user.repository.UserRepository;
import com.keepy.global.exception.CustomException;
import com.keepy.global.exception.ErrorCode;
import com.keepy.infra.gcs.GcsService;
import com.keepy.infra.naver.NaverShoppingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
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
    private final UserRepository userRepository;
    private final NaverShoppingService naverShoppingService;

    @Value("${openai.model}")
    private String model;

    private static final String ANALYSIS_PROMPT = """
            You are a product identification AI for a Korean shopping app called Keepy.

            Analyze the product in this image and return a JSON object with this exact structure:
            {
              "productName": "exact product name in Korean or English",
              "brand": "brand name",
              "category": "one of: COSMETICS, CLOTHES, SHOES, TECH, FOOD, OTHER",
              "estimatedPrice": numeric price in KRW (no currency symbol)
            }

            Focus on identifying the product accurately from the image.
            If you cannot identify the product clearly, make your best estimate.
            CRITICAL: Return ONLY the raw JSON object. Do NOT wrap it in markdown code blocks. Do NOT add any explanation before or after. Your entire response must be valid JSON starting with { and ending with }.
            """;

    @Transactional
    public ItemDetailResponse analyze(Long userId, MultipartFile image) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (user.isAnalysisLimitExceeded()) {
            throw new CustomException(ErrorCode.ANALYSIS_RATE_LIMIT_EXCEEDED);
        }

        validateImage(image);
        user.incrementAnalysisCount();

        String screenshotUrl = gcsService.upload(image, "screenshots");

        String base64Image = encodeImageToBase64(image);
        String mimeType = image.getContentType() != null ? image.getContentType() : "image/jpeg";

        String rawResponse = callOpenAi(base64Image, mimeType);
        AnalysisResponse result = parseResponse(rawResponse);

        Category category = parseCategory(result.category());

        String searchQuery = buildSearchQuery(result.brand(), result.productName());
        List<ItemSaveRequest.ShoppingOptionSaveRequest> shoppingOptions =
                new ArrayList<>(naverShoppingService.search(searchQuery));

        if (category == Category.CLOTHES || category == Category.SHOES) {
            String encoded = UriUtils.encode(result.productName(), StandardCharsets.UTF_8);
            shoppingOptions.add(new ItemSaveRequest.ShoppingOptionSaveRequest(
                    "지그재그", "https://zigzag.kr/search?q=" + encoded, null, null));
            shoppingOptions.add(new ItemSaveRequest.ShoppingOptionSaveRequest(
                    "에이블리", "https://a-bly.com/search?keyword=" + encoded, null, null));
        }

        ItemSaveRequest saveRequest = new ItemSaveRequest(
                result.productName(),
                result.brand(),
                category,
                result.estimatedPrice(),
                null,
                screenshotUrl,
                null,
                shoppingOptions
        );

        return itemService.save(userId, saveRequest);
    }

    private String buildSearchQuery(String brand, String productName) {
        if (brand != null && !brand.isBlank()) {
            return brand + " " + productName;
        }
        return productName;
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
                "max_tokens", 500,
                "messages", List.of(
                        Map.of(
                                "role", "user",
                                "content", List.of(
                                        Map.of(
                                                "type", "image",
                                                "source", Map.of(
                                                        "type", "base64",
                                                        "media_type", mimeType,
                                                        "data", base64Image
                                                )
                                        ),
                                        Map.of(
                                                "type", "text",
                                                "text", ANALYSIS_PROMPT
                                        )
                                )
                        )
                )
        );

        try {
            String responseBody = openAiRestClient.post()
                    .body(requestBody)
                    .retrieve()
                    .body(String.class);

            JsonNode root = objectMapper.readTree(responseBody);
            return root.path("content").get(0).path("text").asText();
        } catch (Exception e) {
            log.error("OpenAI API call failed", e);
            throw new CustomException(ErrorCode.ANALYSIS_FAILED);
        }
    }

    private AnalysisResponse parseResponse(String json) {
        try {
            String cleaned = json.strip();
            if (cleaned.startsWith("```")) {
                cleaned = cleaned.replaceFirst("^```[a-zA-Z]*\\s*", "").replaceFirst("```\\s*$", "").strip();
            }
            return objectMapper.readValue(cleaned, AnalysisResponse.class);
        } catch (Exception e) {
            log.error("Failed to parse OpenAI response: {}", json, e);
            throw new CustomException(ErrorCode.ANALYSIS_FAILED);
        }
    }
}
