package com.beyond.ordersystem.product.dto;

import com.beyond.ordersystem.product.domain.Product;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductResDto {
    private Long id;
    private String imagePath;
    private String name;
    private String category;
    private Integer price;
    private Integer stockQuantity;

    public ProductResDto fromEntity(Product savedProduct) {
        this.id = savedProduct.getId();
        this.imagePath = savedProduct.getImagePath();
        this.name = savedProduct.getName();
        this.category = savedProduct.getCategory();
        this.price = savedProduct.getPrice();
        this.stockQuantity = savedProduct.getStockQuantity();
        return this;
    }
}
