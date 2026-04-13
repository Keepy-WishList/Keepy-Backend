package com.keepy.domain.item.service;

import com.keepy.domain.item.dto.*;
import com.keepy.domain.item.entity.Category;
import com.keepy.domain.item.entity.Item;
import com.keepy.domain.item.entity.ShoppingOption;
import com.keepy.domain.item.repository.ItemRepository;
import com.keepy.domain.user.entity.User;
import com.keepy.domain.user.repository.UserRepository;
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
    private final UserRepository userRepository;

    @Transactional
    public ItemDetailResponse save(Long userId, ItemSaveRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        Item item = Item.builder()
                .user(user)
                .productName(request.productName())
                .brand(request.brand())
                .category(request.category())
                .price(request.price())
                .currency(request.currency())
                .imageUrl(request.imageUrl())
                .screenshotUrl(request.screenshotUrl())
                .description(request.description())
                .memo(request.memo())
                .build();

        if (request.shoppingOptions() != null) {
            request.shoppingOptions().forEach(opt -> {
                ShoppingOption option = ShoppingOption.builder()
                        .item(item)
                        .siteName(opt.siteName())
                        .siteUrl(opt.siteUrl())
                        .price(opt.price())
                        .currency(opt.currency())
                        .deliveryDays(opt.deliveryDays())
                        .deliveryFee(opt.deliveryFee())
                        .build();
                item.addShoppingOption(option);
            });
        }

        return ItemDetailResponse.from(itemRepository.save(item));
    }

    @Transactional(readOnly = true)
    public Page<ItemListResponse> getMyItems(Long userId, String categoryStr, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        if (categoryStr != null && !categoryStr.isBlank()) {
            Category category = Category.valueOf(categoryStr.toUpperCase());
            return itemRepository.findByUserIdAndCategoryOrderByCreatedAtDesc(userId, category, pageable)
                    .map(ItemListResponse::from);
        }
        return itemRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(ItemListResponse::from);
    }

    @Transactional(readOnly = true)
    public ItemDetailResponse getItem(Long userId, Long itemId) {
        Item item = findItemWithOwnerCheck(userId, itemId);
        return ItemDetailResponse.from(item);
    }

    @Transactional
    public ItemDetailResponse updateMemo(Long userId, Long itemId, MemoUpdateRequest request) {
        Item item = findItemWithOwnerCheck(userId, itemId);
        item.updateMemo(request.memo());
        return ItemDetailResponse.from(item);
    }

    @Transactional
    public ItemDetailResponse togglePurchased(Long userId, Long itemId) {
        Item item = findItemWithOwnerCheck(userId, itemId);
        item.togglePurchased();
        return ItemDetailResponse.from(item);
    }

    @Transactional
    public void deleteItem(Long userId, Long itemId) {
        Item item = findItemWithOwnerCheck(userId, itemId);
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
                .map(ItemListResponse::from);
    }

    private Item findItemWithOwnerCheck(Long userId, Long itemId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new CustomException(ErrorCode.ITEM_NOT_FOUND));
        if (!item.getUser().getId().equals(userId)) {
            throw new CustomException(ErrorCode.UNAUTHORIZED_ACCESS);
        }
        return item;
    }
}
