package com.beyond.ordersystem.product.domain;

import com.beyond.ordersystem.common.domain.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class Product extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String name;

    private String category;

    private Integer price;

    private Integer stockQuantity;

    private String imagePath;
    private String delYn;
    public void updateImagePath(String imagePath){
        this.imagePath = imagePath;
    }

    public void decreaseStockQuantity(Integer productCount) {
        this.stockQuantity -= productCount;
    }
}
