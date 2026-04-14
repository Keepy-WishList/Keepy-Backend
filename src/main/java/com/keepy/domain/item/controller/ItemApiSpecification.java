package com.keepy.domain.item.controller;

import com.keepy.domain.item.dto.ItemDetailResponse;
import com.keepy.domain.item.dto.ItemListResponse;
import com.keepy.domain.item.dto.ItemSaveRequest;
import com.keepy.domain.item.dto.MemoUpdateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;

@Tag(name = "Item", description = "위시리스트 아이템 API — 저장·조회·검색·수정·삭제")
public interface ItemApiSpecification {

    @Operation(
            summary = "위시리스트 아이템 저장",
            description = "분석된 상품 정보를 위시리스트에 저장합니다. 쇼핑 옵션(사이트별 가격·배송)도 함께 저장됩니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "저장 성공"),
            @ApiResponse(responseCode = "400", description = "입력값 유효성 오류"),
            @ApiResponse(responseCode = "401", description = "인증 필요")
    })
    ResponseEntity<com.keepy.global.common.ApiResponse<ItemDetailResponse>> saveItem(
            @Parameter(hidden = true) UserDetails userDetails,
            @Valid @RequestBody ItemSaveRequest request
    );

    @Operation(
            summary = "내 위시리스트 조회",
            description = "로그인한 사용자의 위시리스트를 최신순으로 페이징하여 반환합니다. category 파라미터로 필터링할 수 있습니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 필요")
    })
    ResponseEntity<com.keepy.global.common.ApiResponse<Page<ItemListResponse>>> getMyItems(
            @Parameter(hidden = true) UserDetails userDetails,
            @Parameter(description = "카테고리 필터 (FASHION, TECH, INTERIOR, FURNITURE, LIGHTING, DECORATION, KITCHEN, OTHER)")
            @RequestParam(required = false) String category,
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기", example = "20")
            @RequestParam(defaultValue = "20") int size
    );

    @Operation(
            summary = "아이템 검색 및 필터링",
            description = "키워드·카테고리·가격 범위로 검색합니다. sort는 latest(최신순) / price_asc(가격 낮은순) / price_desc(가격 높은순)를 지원합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "검색 성공"),
            @ApiResponse(responseCode = "401", description = "인증 필요")
    })
    ResponseEntity<com.keepy.global.common.ApiResponse<Page<ItemListResponse>>> search(
            @Parameter(hidden = true) UserDetails userDetails,
            @Parameter(description = "검색 키워드 (상품명, 브랜드)") @RequestParam(required = false) String keyword,
            @Parameter(description = "카테고리 필터") @RequestParam(required = false) String category,
            @Parameter(description = "최소 가격") @RequestParam(required = false) BigDecimal minPrice,
            @Parameter(description = "최대 가격") @RequestParam(required = false) BigDecimal maxPrice,
            @Parameter(description = "정렬 기준 (latest / price_asc / price_desc)", example = "latest")
            @RequestParam(defaultValue = "latest") String sort,
            @Parameter(description = "페이지 번호", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기", example = "20") @RequestParam(defaultValue = "20") int size
    );

    @Operation(
            summary = "아이템 상세 조회",
            description = "아이템 ID로 상세 정보와 쇼핑 옵션 목록을 반환합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 필요"),
            @ApiResponse(responseCode = "403", description = "본인 소유 아이템이 아님"),
            @ApiResponse(responseCode = "404", description = "아이템 없음")
    })
    ResponseEntity<com.keepy.global.common.ApiResponse<ItemDetailResponse>> getItem(
            @Parameter(hidden = true) UserDetails userDetails,
            @Parameter(description = "아이템 ID", example = "1") @PathVariable Long itemId
    );

    @Operation(
            summary = "메모 수정",
            description = "아이템에 메모를 추가하거나 수정합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "401", description = "인증 필요"),
            @ApiResponse(responseCode = "403", description = "본인 소유 아이템이 아님"),
            @ApiResponse(responseCode = "404", description = "아이템 없음")
    })
    ResponseEntity<com.keepy.global.common.ApiResponse<ItemDetailResponse>> updateMemo(
            @Parameter(hidden = true) UserDetails userDetails,
            @Parameter(description = "아이템 ID", example = "1") @PathVariable Long itemId,
            @RequestBody MemoUpdateRequest request
    );

    @Operation(
            summary = "구매 완료 토글",
            description = "아이템의 구매 완료 상태를 true ↔ false로 전환합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "토글 성공"),
            @ApiResponse(responseCode = "401", description = "인증 필요"),
            @ApiResponse(responseCode = "403", description = "본인 소유 아이템이 아님"),
            @ApiResponse(responseCode = "404", description = "아이템 없음")
    })
    ResponseEntity<com.keepy.global.common.ApiResponse<ItemDetailResponse>> togglePurchased(
            @Parameter(hidden = true) UserDetails userDetails,
            @Parameter(description = "아이템 ID", example = "1") @PathVariable Long itemId
    );

    @Operation(
            summary = "아이템 삭제",
            description = "아이템과 연결된 쇼핑 옵션을 함께 삭제합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "삭제 성공"),
            @ApiResponse(responseCode = "401", description = "인증 필요"),
            @ApiResponse(responseCode = "403", description = "본인 소유 아이템이 아님"),
            @ApiResponse(responseCode = "404", description = "아이템 없음")
    })
    ResponseEntity<com.keepy.global.common.ApiResponse<?>> deleteItem(
            @Parameter(hidden = true) UserDetails userDetails,
            @Parameter(description = "아이템 ID", example = "1") @PathVariable Long itemId
    );
}
