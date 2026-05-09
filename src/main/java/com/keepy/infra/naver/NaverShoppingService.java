package com.keepy.infra.naver;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.keepy.domain.item.dto.ItemSaveRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class NaverShoppingService {

    private final RestClient naverRestClient;
    private final ObjectMapper objectMapper;

    public List<ItemSaveRequest.ShoppingOptionSaveRequest> search(String query) {
        try {
            String response = naverRestClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/v1/search/shop.json")
                            .queryParam("query", query)
                            .queryParam("display", 20)
                            .queryParam("sort", "sim")
                            .build())
                    .retrieve()
                    .body(String.class);

            JsonNode items = objectMapper.readTree(response).path("items");

            Map<String, ItemSaveRequest.ShoppingOptionSaveRequest> byMall = new LinkedHashMap<>();

            for (JsonNode item : items) {
                String mallName = item.path("mallName").asText();
                String link = item.path("link").asText();
                String lpriceStr = item.path("lprice").asText();

                if (byMall.containsKey(mallName)) continue;

                BigDecimal price = null;
                if (!lpriceStr.isEmpty() && !lpriceStr.equals("0")) {
                    price = new BigDecimal(lpriceStr);
                }

                byMall.put(mallName, new ItemSaveRequest.ShoppingOptionSaveRequest(
                        mallName, link, price, null
                ));

                if (byMall.size() >= 5) break;
            }

            return new ArrayList<>(byMall.values());
        } catch (Exception e) {
            log.warn("Naver Shopping API search failed for query: {}", query, e);
            return new ArrayList<>();
        }
    }
}
