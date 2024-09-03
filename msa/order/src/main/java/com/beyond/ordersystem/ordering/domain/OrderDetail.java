package com.beyond.ordersystem.ordering.domain;

import com.beyond.ordersystem.ordering.dto.OrderListResDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class OrderDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer quantity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Ordering ordering;

    private Long productId;

    public OrderListResDto.OrderDetailResDto fromEntity() {
        return OrderListResDto.OrderDetailResDto.builder()
                .id(this.id)
                .count(this.quantity)
                .build();
    }
}
