package com.keepy.domain.item.repository;

import com.keepy.domain.item.entity.Category;
import com.keepy.domain.item.entity.Item;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemRepository extends JpaRepository<Item, Long> {

    Page<Item> findByUserIdAndCategory(Long userId, Category category, Pageable pageable);

    Page<Item> findByUserId(Long userId, Pageable pageable);
}
