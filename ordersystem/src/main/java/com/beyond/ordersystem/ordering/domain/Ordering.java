package com.beyond.ordersystem.ordering.domain;

import com.beyond.ordersystem.common.domain.BaseEntity;
import com.beyond.ordersystem.member.domain.Member;
import com.beyond.ordersystem.ordering.dto.OrderListResDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class Ordering extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @Enumerated(EnumType.STRING)
    private OrderStatus orderStatus;

    @OneToMany(mappedBy = "ordering", cascade = CascadeType.PERSIST)
    @Builder.Default
    private List<OrderDetail> orderDetails = new ArrayList<>();

    @Column(nullable = false, columnDefinition = "char(1) default 'N'")
    @Builder.Default
    private String delYn = "N";

    public OrderListResDto fromEntity() {
        List<OrderListResDto.OrderDetailResDto> orderDetailResDtos = new ArrayList<>();
        for (OrderListResDto.OrderDetailResDto orderDetailResDto : orderDetailResDtos) {
            orderDetailResDtos.add(orderDetailResDto);
        }

        return OrderListResDto.builder()
                .id(this.id)
                .memberEmail(member.getEmail())
                .orderStatus(this.orderStatus)
                .OrderDetailDtos(orderDetailResDtos)
                .build();
    }

    public void updateStatus(OrderStatus orderStatus) {
        this.orderStatus = orderStatus;
    }
}
