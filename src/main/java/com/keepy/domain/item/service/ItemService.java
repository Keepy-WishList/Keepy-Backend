package com.keepy.domain.item.service;

import com.keepy.domain.item.dto.*;
import com.keepy.domain.item.entity.Category;
import com.keepy.domain.item.entity.Item;
import com.keepy.domain.item.entity.ShoppingOption;
import com.keepy.domain.item.repository.ItemRepository;
import com.keepy.domain.item.repository.ShoppingOptionRepository;
import com.keepy.global.exception.CustomException;
import com.keepy.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;
    private final ShoppingOptionRepository shoppingOptionRepository;

    @Transactional
    public ItemDetailResponse save(Long userId, ItemSaveRequest request) {
        Item item = Item.of(
                userId,
                request.productName(),
                request.brand(),
                request.category(),
                request.price(),
                request.imageUrl(),
                request.screenshotUrl(),
                request.description(),
                request.memo()
        );
        itemRepository.save(item);

        List<ShoppingOption> options = List.of();
        if (request.shoppingOptions() != null) {
            options = request.shoppingOptions().stream()
                    .map(opt -> ShoppingOption.of(
                            item.getId(),
                            opt.siteName(),
                            opt.siteUrl(),
                            opt.price(),
                            opt.deliveryFee()
                    ))
                    .map(shoppingOptionRepository::save)
                    .toList();
        }

        return ItemDetailResponse.from(item, options);
    }

    @Transactional(readOnly = true)
    public Page<ItemListResponse> getMyItems(Long userId, String categoryStr, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<Item> items;
        if (categoryStr != null && !categoryStr.isBlank()) {
            Category category = Category.valueOf(categoryStr.toUpperCase());
            items = itemRepository.findByUserIdAndCategoryOrderByCreatedAtDesc(userId, category, pageable);
        } else {
            items = itemRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        }

        return items.map(item -> {
            List<ShoppingOption> options = shoppingOptionRepository.findByItemId(item.getId());
            String bestSiteName = options.isEmpty() ? null : options.getFirst().getSiteName();
            return ItemListResponse.from(item, bestSiteName);
        });
    }

    @Transactional(readOnly = true)
    public ItemDetailResponse getItem(Long userId, Long itemId) {
        Item item = findItemWithOwnerCheck(userId, itemId);
        List<ShoppingOption> options = shoppingOptionRepository.findByItemId(itemId);
        return ItemDetailResponse.from(item, options);
    }

    @Transactional
    public ItemDetailResponse updateMemo(Long userId, Long itemId, MemoUpdateRequest request) {
        Item item = findItemWithOwnerCheck(userId, itemId);
        item.updateMemo(request.memo());
        List<ShoppingOption> options = shoppingOptionRepository.findByItemId(itemId);
        return ItemDetailResponse.from(item, options);
    }

    @Transactional
    public ItemDetailResponse togglePurchased(Long userId, Long itemId) {
        Item item = findItemWithOwnerCheck(userId, itemId);
        item.togglePurchased();
        List<ShoppingOption> options = shoppingOptionRepository.findByItemId(itemId);
        return ItemDetailResponse.from(item, options);
    }

    @Transactional
    public void deleteItem(Long userId, Long itemId) {
        Item item = findItemWithOwnerCheck(userId, itemId);
        shoppingOptionRepository.deleteAll(shoppingOptionRepository.findByItemId(itemId));
        itemRepository.delete(item);
    }

    @Transactional(readOnly = true)
    public Page<ItemListResponse> search(Long userId, String keyword, String categoryStr,
                                         BigDecimal minPrice, BigDecimal maxPrice,
                                         String sort, int page, int size) {
        Category category = null;
        if (categoryStr != null && !categoryStr.isBlank()) {
            category = Category.valueOf(categoryStr.toUpperCase());
        }

        Sort sortOrder = switch (sort != null ? sort : "latest") {
            case "price_asc" -> Sort.by("price").ascending();
            case "price_desc" -> Sort.by("price").descending();
            default -> Sort.by("createdAt").descending();
        };
        Pageable pageable = PageRequest.of(page, size, sortOrder);

        return itemRepository.search(userId, keyword, category, minPrice, maxPrice, pageable)
                .map(item -> {
                    List<ShoppingOption> options = shoppingOptionRepository.findByItemId(item.getId());
                    String bestSiteName = options.isEmpty() ? null : options.getFirst().getSiteName();
                    return ItemListResponse.from(item, bestSiteName);
                });
    }

    private Item findItemWithOwnerCheck(Long userId, Long itemId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new CustomException(ErrorCode.ITEM_NOT_FOUND));
        if (!item.getUserId().equals(userId)) {
            throw new CustomException(ErrorCode.UNAUTHORIZED_ACCESS);
        }
        return item;
    }
}
