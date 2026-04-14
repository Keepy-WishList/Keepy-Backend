package com.keepy.domain.analysis.controller;

import com.keepy.domain.analysis.dto.AnalysisResponse;
import com.keepy.domain.analysis.service.AnalysisService;
import com.keepy.global.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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

    /**
     * 스크린샷 이미지를 업로드하면 LLM이 제품을 분석하고
     * 여러 쇼핑 사이트의 가격 비교 결과를 반환합니다.
     */
    @PostMapping(value = "/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<AnalysisResponse>> analyzeImage(
            @RequestPart("image") MultipartFile image) {
        return ResponseEntity.ok(ApiResponse.success(analysisService.analyze(image)));
    }
}
