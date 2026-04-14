package com.keepy.domain.item.repository;

import com.keepy.domain.item.entity.Category;
import com.keepy.domain.item.entity.Item;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;

public interface ItemRepository extends JpaRepository<Item, Long> {

    // 내 위시리스트 조회 (카테고리 필터)
    Page<Item> findByUserIdAndCategoryOrderByCreatedAtDesc(Long userId, Category category, Pageable pageable);

    Page<Item> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    // 검색 + 필터링
    @Query("""
            SELECT i FROM Item i
            WHERE i.userId = :userId
              AND (:keyword IS NULL OR LOWER(i.productName) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(i.brand) LIKE LOWER(CONCAT('%', :keyword, '%')))
              AND (:category IS NULL OR i.category = :category)
              AND (:minPrice IS NULL OR i.price >= :minPrice)
              AND (:maxPrice IS NULL OR i.price <= :maxPrice)
            """)
    Page<Item> search(
            @Param("userId") Long userId,
            @Param("keyword") String keyword,
            @Param("category") Category category,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            Pageable pageable
    );

    // 전체 검색 (로그인 없이도 가능한 탐색)
    @Query("""
            SELECT i FROM Item i
            WHERE (:keyword IS NULL OR LOWER(i.productName) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(i.brand) LIKE LOWER(CONCAT('%', :keyword, '%')))
              AND (:category IS NULL OR i.category = :category)
              AND (:minPrice IS NULL OR i.price >= :minPrice)
              AND (:maxPrice IS NULL OR i.price <= :maxPrice)
            """)
    Page<Item> searchAll(
            @Param("keyword") String keyword,
            @Param("category") Category category,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            Pageable pageable
    );
}
