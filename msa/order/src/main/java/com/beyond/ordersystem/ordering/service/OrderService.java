package com.beyond.ordersystem.ordering.service;

import com.beyond.ordersystem.common.dto.CommonResDto;
import com.beyond.ordersystem.common.service.StockInventoryService;
import com.beyond.ordersystem.ordering.controller.SseController;
import com.beyond.ordersystem.ordering.domain.OrderDetail;
import com.beyond.ordersystem.ordering.domain.OrderStatus;
import com.beyond.ordersystem.ordering.domain.Ordering;
import com.beyond.ordersystem.ordering.dto.*;
import com.beyond.ordersystem.ordering.repository.OrderingRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.*;
//import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import javax.persistence.EntityNotFoundException;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class OrderService {
    private final OrderingRepository orderingRepository;
    private final StockInventoryService stockInventoryService;
//    private final StockDecreaseEventHandler stockDecreaseEventHandler;
    private final SseController sseController;
    private final RestTemplate restTemplate;
    private final ProductFeign productFeign;
//    private final KafkaTemplate<String,Object> kafkaTemplate;

    public Long orderRestTemplateCreate(List<OrderCreateReqDto> orderDetailReqDtos) {
        String memberEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        Ordering order = Ordering.builder().memberEmail(memberEmail).orderStatus(OrderStatus.ORDERED).build();

        for (OrderCreateReqDto dto : orderDetailReqDtos) {
            Integer productCount = dto.getProductCount();

//            Product API에 요청을 통해 product 객체를 조회해야함
            String productGetUrl = "http://product-service/product/"+dto.getProductId();
            HttpHeaders httpHeaders = new HttpHeaders();
            String token = (String) SecurityContextHolder.getContext().getAuthentication().getCredentials();
            httpHeaders.set("Authorization",token);
            HttpEntity<String> entity = new HttpEntity<>(httpHeaders);
            ResponseEntity<CommonResDto> productEntity = restTemplate.exchange(productGetUrl, HttpMethod.GET, entity, CommonResDto.class);
            ObjectMapper objectMapper = new ObjectMapper();
            ProductDto productDto = objectMapper.convertValue(productEntity.getBody().getResult(),ProductDto.class);
            System.out.println("productDto = " + productDto);

            if (productDto.getName().contains("sale")) {
//                redis를 통한 재고관리 및 재고잔량 확인
                System.out.println("orderDetailDto.getProductId() = " + dto.getProductId());
                int newQuantity = stockInventoryService.decreaseStock(productDto.getId(), productCount).intValue();
                if (newQuantity < 0) {
                    throw new IllegalArgumentException("재고가 부족합니다.");
                }

//                RDB에 redis 재고를 업데이트. rabbitMQ를 통해 비동기적으로 이벤트 처리
//                stockDecreaseEventHandler.publish(new StockDecreaseEvent(productDto.getId(), productCount));

            } else {
                if (productDto.getStockQuantity() < productCount) {
                    throw new IllegalArgumentException("재고가 부족합니다.");
                }
//                restTemplate을 통한 update 요청을 보내야함
//                product.decreaseStockQuantity(productCount);
                String updateUrl = "http://product-service/product/updatestock";
                httpHeaders.setContentType(MediaType.APPLICATION_JSON);
                HttpEntity<ProductUpdateStockDto> updateEntity = new HttpEntity<>(new ProductUpdateStockDto(productDto.getId(), dto.getProductCount()),httpHeaders);
                restTemplate.exchange(updateUrl, HttpMethod.PUT, updateEntity, Void.class);
            }

            OrderDetail orderDetail = OrderDetail.builder()
                    .ordering(order)
                    .productId(productDto.getId())
                    .quantity(productCount)
                    .build();
            order.getOrderDetails().add(orderDetail);

        }
        Ordering savedOrder = orderingRepository.save(order);
        sseController.publishMessage(savedOrder.fromEntity(),"admin@test.com");
        return savedOrder.getId();
    }
    public Long orderFeignClientCreate(List<OrderCreateReqDto> dtos) {
        String memberEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        Ordering order = Ordering.builder().memberEmail(memberEmail).orderStatus(OrderStatus.ORDERED).build();

        for (OrderCreateReqDto dto : dtos) {
            Integer productCount = dto.getProductCount();

//           ResponseEntity가 기본 응답값이므로 바로 CommonResDto로 매핑
            CommonResDto commonResDto = productFeign.getProductById(dto.getProductId());
            ObjectMapper objectMapper = new ObjectMapper();
            ProductDto productDto = objectMapper.convertValue(commonResDto.getResult(),ProductDto.class);
            System.out.println("productDto = " + productDto);

            if (productDto.getName().contains("sale")) {
//                redis를 통한 재고관리 및 재고잔량 확인
                System.out.println("orderDetailDto.getProductId() = " + dto.getProductId());
                int newQuantity = stockInventoryService.decreaseStock(productDto.getId(), productCount).intValue();
                if (newQuantity < 0) {
                    throw new IllegalArgumentException("재고가 부족합니다.");
                }

//                RDB에 redis 재고를 업데이트. rabbitMQ를 통해 비동기적으로 이벤트 처리
//                stockDecreaseEventHandler.publish(new StockDecreaseEvent(productDto.getId(), productCount));

            } else {
                if (productDto.getStockQuantity() < productCount) {
                    throw new IllegalArgumentException("재고가 부족합니다.");
                }

                productFeign.updateProductStock(new ProductUpdateStockDto(productDto.getId(), dto.getProductCount()));
            }

            OrderDetail orderDetail = OrderDetail.builder()
                    .ordering(order)
                    .productId(productDto.getId())
                    .quantity(productCount)
                    .build();
            order.getOrderDetails().add(orderDetail);

        }
        Ordering savedOrder = orderingRepository.save(order);
        sseController.publishMessage(savedOrder.fromEntity(),"admin@test.com");
        return savedOrder.getId();
    }
//    public Long orderFeignKafkaCreate(List<OrderCreateReqDto> dtos) {
//        String memberEmail = SecurityContextHolder.getContext().getAuthentication().getName();
//        Ordering order = Ordering.builder().memberEmail(memberEmail).orderStatus(OrderStatus.ORDERED).build();
//
//        for (OrderCreateReqDto dto : dtos) {
//            Integer productCount = dto.getProductCount();
//
////           ResponseEntity가 기본 응답값이므로 바로 CommonResDto로 매핑
//            CommonResDto commonResDto = productFeign.getProductById(dto.getProductId());
//            ObjectMapper objectMapper = new ObjectMapper();
//            ProductDto productDto = objectMapper.convertValue(commonResDto.getResult(),ProductDto.class);
//            System.out.println("productDto = " + productDto);
//
//            if (productDto.getName().contains("sale")) {
////                redis를 통한 재고관리 및 재고잔량 확인
//                System.out.println("orderDetailDto.getProductId() = " + dto.getProductId());
//                int newQuantity = stockInventoryService.decreaseStock(productDto.getId(), productCount).intValue();
//                if (newQuantity < 0) {
//                    throw new IllegalArgumentException("재고가 부족합니다.");
//                }
//
////                RDB에 redis 재고를 업데이트. rabbitMQ를 통해 비동기적으로 이벤트 처리
//                stockDecreaseEventHandler.publish(new StockDecreaseEvent(productDto.getId(), productCount));
//
//            } else {
//                if (productDto.getStockQuantity() < productCount) {
//                    throw new IllegalArgumentException("재고가 부족합니다.");
//                }
//                ProductUpdateStockDto productUpdateStockDto = new ProductUpdateStockDto(productDto.getId(), dto.getProductCount());
//                kafkaTemplate.send("product-update-topic",productUpdateStockDto);
//            }
//
//            OrderDetail orderDetail = OrderDetail.builder()
//                    .ordering(order)
//                    .productId(productDto.getId())
//                    .quantity(productCount)
//                    .build();
//            order.getOrderDetails().add(orderDetail);
//
//        }
//        Ordering savedOrder = orderingRepository.save(order);
//        sseController.publishMessage(savedOrder.fromEntity(),"admin@test.com");
//        return savedOrder.getId();
//    }

    public List<OrderListResDto> orderList() {
        List<OrderListResDto> resDtos = new ArrayList<>();
        List<Ordering> all = orderingRepository.findAll();
        for (Ordering ordering : all) {
            OrderListResDto dto = ordering.fromEntity();
            List<OrderDetail> orderDetails = ordering.getOrderDetails();
            for (OrderDetail orderDetail : orderDetails) {
                OrderListResDto.OrderDetailResDto detailResDto = orderDetail.fromEntity();
                dto.getOrderDetailDtos().add(detailResDto);
            }
            resDtos.add(dto);
        }
        return resDtos;
    }

    public Page<OrderListResDto> myOrderList(Pageable pageable) {
        String memberEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        Page<Ordering> orderings = orderingRepository.findByMemberEmailAndDelYn(pageable, memberEmail, "N");

        List<OrderListResDto> resDtos = new ArrayList<>();

        for (Ordering ordering : orderings) {
            OrderListResDto dto = ordering.fromEntity();
            List<OrderDetail> orderDetails = ordering.getOrderDetails();
            for (OrderDetail orderDetail : orderDetails) {
                OrderListResDto.OrderDetailResDto detailResDto = orderDetail.fromEntity();
                dto.getOrderDetailDtos().add(detailResDto);
            }
            resDtos.add(dto);
        }

        return new PageImpl<>(resDtos, pageable, orderings.getTotalElements());
    }

    public Ordering orderCancel(Long id) {
        Ordering ordering = orderingRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("not found"));
        ordering.updateStatus(OrderStatus.CANCELD);
        return ordering;


    }
}
