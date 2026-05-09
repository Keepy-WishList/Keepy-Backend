package com.keepy.domain.item.controller;

import com.keepy.domain.item.dto.ItemDetailResponse;
import com.keepy.domain.item.dto.ItemListResponse;
import com.keepy.domain.item.dto.MemoUpdateRequest;
import com.keepy.domain.item.service.ItemService;
import com.keepy.global.common.ApiResponse;
import com.keepy.global.common.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/items")
@RequiredArgsConstructor
public class ItemController implements ItemApiSpecification {

    private final ItemService itemService;

    // 내 위시리스트 조회
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<ItemListResponse>>> getMyItems(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "latest") String sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Long userId = Long.parseLong(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(itemService.getMyItems(userId, category, sort, page, size))));
    }

    // 아이템 상세 조회
    @GetMapping("/{itemId}")
    public ResponseEntity<ApiResponse<ItemDetailResponse>> getItem(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long itemId) {
        Long userId = Long.parseLong(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(itemService.getItem(userId, itemId)));
    }

    // 메모 수정
    @PatchMapping("/{itemId}/memo")
    public ResponseEntity<ApiResponse<ItemDetailResponse>> updateMemo(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long itemId,
            @RequestBody MemoUpdateRequest request) {
        Long userId = Long.parseLong(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("메모가 수정되었습니다.",
                itemService.updateMemo(userId, itemId, request)));
    }

    // 구매 완료 토글
    @PatchMapping("/{itemId}/purchased")
    public ResponseEntity<ApiResponse<ItemDetailResponse>> togglePurchased(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long itemId) {
        Long userId = Long.parseLong(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(itemService.togglePurchased(userId, itemId)));
    }

    // 아이템 삭제
    @DeleteMapping("/{itemId}")
    public ResponseEntity<ApiResponse<?>> deleteItem(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long itemId) {
        Long userId = Long.parseLong(userDetails.getUsername());
        itemService.deleteItem(userId, itemId);
        return ResponseEntity.ok(ApiResponse.success("아이템이 삭제되었습니다."));
    }
}
