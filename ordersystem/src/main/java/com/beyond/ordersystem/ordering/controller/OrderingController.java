package com.beyond.ordersystem.ordering.controller;

import com.beyond.ordersystem.common.dto.CommonResDto;
import com.beyond.ordersystem.ordering.domain.Ordering;
import com.beyond.ordersystem.ordering.dto.OrderCreateReqDto;
import com.beyond.ordersystem.ordering.dto.OrderListResDto;
import com.beyond.ordersystem.ordering.dto.OrderResDto;
import com.beyond.ordersystem.ordering.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class OrderingController {
    private final OrderService orderService;
    @PostMapping("/order/create")
    public Long orderCreate(@RequestBody List<OrderCreateReqDto> dto) {
        Long orderId = orderService.orderCreate(dto);
        return orderId;
    }
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/order/list")
    public List<OrderListResDto> orderList() {
        return orderService.orderList();
    }

    @GetMapping("/order/myorders")
    public ResponseEntity<CommonResDto> myOderList(Pageable pageable) {
        Page<OrderListResDto> dtos = orderService.myOrderList(pageable);
        return new ResponseEntity<>(new CommonResDto(HttpStatus.OK,"주문이 성공적으로 조회되었습니다.",dtos),HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/order/{id}/cancel")
    public ResponseEntity<CommonResDto> orderCancel(@PathVariable Long id) {
        Ordering ordering = orderService.orderCancel(id);
        return new ResponseEntity<>(new CommonResDto(HttpStatus.OK,"주문이 성공적으로 취소되었습니다.",null),HttpStatus.OK);
    }
}
