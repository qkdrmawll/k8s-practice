//package com.beyond.ordersystem.ordering.dto;
//
//import com.beyond.ordersystem.ordering.domain.OrderDetail;
//import com.beyond.ordersystem.ordering.domain.Ordering;
//import com.beyond.ordersystem.product.domain.Product;
//import lombok.AllArgsConstructor;
//import lombok.Data;
//import lombok.NoArgsConstructor;
//
//@Data
//@AllArgsConstructor
//@NoArgsConstructor
//public class OrderDetailReqDto {
//    private Long productId;
//    private Integer productCount;
//
//    public OrderDetail toEntity(Ordering ordering, Product product) {
//        return OrderDetail.builder()
//                .ordering(ordering)
//                .product(product)
//                .quantity(this.productCount).build();
//    }
//}
