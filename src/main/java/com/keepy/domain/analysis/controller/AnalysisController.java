package com.keepy.domain.analysis.controller;

import com.keepy.domain.analysis.service.AnalysisService;
import com.keepy.domain.item.dto.ItemDetailResponse;
import com.keepy.global.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/analysis")
@RequiredArgsConstructor
public class AnalysisController implements AnalysisApiSpecification {

    private final AnalysisService analysisService;

    @PostMapping(value = "/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<ItemDetailResponse>> analyzeImage(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestPart("image") MultipartFile image) {
        Long userId = Long.parseLong(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(analysisService.analyze(userId, image)));
    }
}
