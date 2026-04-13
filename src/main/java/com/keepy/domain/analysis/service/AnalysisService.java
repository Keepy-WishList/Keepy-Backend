package com.keepy.domain.analysis.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.keepy.domain.analysis.dto.AnalysisResponse;
import com.keepy.global.exception.CustomException;
import com.keepy.global.exception.ErrorCode;
import com.keepy.infra.s3.S3Service;
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
    private final S3Service s3Service;
    private final ObjectMapper objectMapper;

    @Value("${openai.model}")
    private String model;

    private static final String ANALYSIS_PROMPT = """
            You are a product identification and price comparison AI for a Korean shopping app called Keepy.

            Analyze the product in this image and return a JSON object with this exact structure:
            {
              "productName": "exact product name in Korean or English",
              "brand": "brand name",
              "category": "one of: FASHION, TECH, INTERIOR, FURNITURE, LIGHTING, DECORATION, KITCHEN, OTHER",
              "estimatedPrice": numeric price (no currency symbol),
              "currency": "KRW or USD or EUR",
              "description": "brief product description in Korean (2-3 sentences)",
              "shoppingOptions": [
                {
                  "siteName": "shopping site name (e.g. 쿠팡, 네이버쇼핑, 11번가, 지마켓, Amazon)",
                  "siteUrl": "product URL on that site",
                  "price": numeric price,
                  "currency": "KRW or USD",
                  "deliveryDays": estimated delivery days as integer,
                  "deliveryFee": "delivery fee info e.g. 무료배송 or 3,000원"
                }
              ]
            }

            Find this product on at least 3 major Korean shopping sites (쿠팡, 네이버쇼핑, 11번가, 지마켓) and/or international sites if relevant.
            If you cannot identify the product clearly, make your best estimate.
            Return ONLY the JSON object, no additional text.
            """;

    public AnalysisResponse analyze(MultipartFile image) {
        validateImage(image);

        // S3에 스크린샷 업로드
        String screenshotUrl = s3Service.upload(image, "screenshots");

        // 이미지를 base64로 인코딩
        String base64Image = encodeImageToBase64(image);
        String mimeType = image.getContentType() != null ? image.getContentType() : "image/jpeg";

        // OpenAI API 호출
        String rawResponse = callOpenAi(base64Image, mimeType);

        // 응답 파싱
        AnalysisResponse result = parseResponse(rawResponse);

        // screenshotUrl 주입 후 반환
        return new AnalysisResponse(
                result.productName(),
                result.brand(),
                result.category(),
                result.estimatedPrice(),
                result.currency(),
                result.description(),
                screenshotUrl,
                result.shoppingOptions()
        );
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
                "max_tokens", 1500,
                "response_format", Map.of("type", "json_object")
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
