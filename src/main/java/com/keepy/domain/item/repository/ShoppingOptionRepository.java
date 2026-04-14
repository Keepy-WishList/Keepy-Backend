package com.keepy.domain.item.repository;

import com.keepy.domain.item.entity.ShoppingOption;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ShoppingOptionRepository extends JpaRepository<ShoppingOption, Long> {

    List<ShoppingOption> findByItemId(Long itemId);
}
