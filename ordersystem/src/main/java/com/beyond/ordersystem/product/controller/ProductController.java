package com.beyond.ordersystem.product.controller;

import com.beyond.ordersystem.common.dto.CommonResDto;
import com.beyond.ordersystem.product.dto.ProductCreateReqDto;
import com.beyond.ordersystem.product.dto.ProductResDto;
import com.beyond.ordersystem.product.dto.ProductSearchDto;
import com.beyond.ordersystem.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/product/create")
    public ResponseEntity<CommonResDto> productCreate(ProductCreateReqDto dto) {
        ProductResDto productResDto = productService.productAwsCreate(dto);
        return new ResponseEntity<>(new CommonResDto(HttpStatus.CREATED, "상품이 성공적으로 등록되었습니다.", productResDto), HttpStatus.CREATED);
    }
    @GetMapping("/product/list")
    public ResponseEntity<CommonResDto> productList(ProductSearchDto searchDto, @PageableDefault(sort = "createdTime",direction = Sort.Direction.DESC) Pageable pageable) {
        Page<ProductResDto> productResDtos = productService.productList(searchDto, pageable);
        return new ResponseEntity<>(new CommonResDto(HttpStatus.OK, "상품 목록이 성공적으로 조회되었습니다.", productResDtos), HttpStatus.OK);
    }

}
