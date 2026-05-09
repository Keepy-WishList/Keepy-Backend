package com.keepy.domain.analysis.controller;

import com.keepy.domain.item.dto.ItemDetailResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "Analysis", description = "AI 이미지 분석 API — 상품 스크린샷을 업로드하면 LLM이 상품 정보와 쇼핑몰별 가격 비교 결과를 반환합니다.")
public interface AnalysisApiSpecification {

    @Operation(
            summary = "상품 이미지 분석",
            description = """
                    상품 스크린샷 이미지를 업로드하면 AI(claude-sonnet-4-6)가 다음 정보를 분석하여 반환합니다.

                    - 상품명, 브랜드, 카테고리
                    - 예상 가격 및 통화
                    - 상품 설명 (한국어)
                    - 쿠팡·네이버쇼핑·11번가·지마켓 등 주요 쇼핑몰의 가격 비교 및 배송 정보

                    지원 형식: JPEG, PNG, WebP (최대 10MB)
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "분석 성공"),
            @ApiResponse(responseCode = "400", description = "이미지 형식 오류 또는 파일 없음"),
            @ApiResponse(responseCode = "401", description = "인증 필요"),
            @ApiResponse(responseCode = "500", description = "AI 분석 실패")
    })
    ResponseEntity<com.keepy.global.common.ApiResponse<ItemDetailResponse>> analyzeImage(
            @Parameter(hidden = true) UserDetails userDetails,
            @Parameter(description = "분석할 상품 스크린샷 이미지 (multipart/form-data)")
            MultipartFile image
    );
}
