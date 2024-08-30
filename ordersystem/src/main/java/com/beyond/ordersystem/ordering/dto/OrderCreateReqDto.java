package com.beyond.ordersystem.ordering.dto;

import com.beyond.ordersystem.member.domain.Member;
import com.beyond.ordersystem.ordering.domain.OrderStatus;
import com.beyond.ordersystem.ordering.domain.Ordering;
import com.fasterxml.jackson.databind.util.JSONPObject;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderCreateReqDto {
//    private List<OrderDetailReqDto> orderDetailReqDtos;
    private Long productId;
    private Integer productCount;

    public Ordering toEntity(Member member) {
        return Ordering.builder()
                .member(member)
                .orderStatus(OrderStatus.ORDERED)
                .build();
    }
}
