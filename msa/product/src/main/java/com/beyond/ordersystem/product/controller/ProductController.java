package com.beyond.ordersystem.product.controller;

import com.beyond.ordersystem.common.dto.CommonResDto;
import com.beyond.ordersystem.product.dto.ProductCreateReqDto;
import com.beyond.ordersystem.product.dto.ProductResDto;
import com.beyond.ordersystem.product.dto.ProductSearchDto;
import com.beyond.ordersystem.product.dto.ProductUpdateStockDto;
import com.beyond.ordersystem.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
//해당 어노테이션 사용시 아래 빈은 실시간 config 변경 사항의 대상이 됨
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

    @GetMapping("/product/{id}")
    public ResponseEntity<CommonResDto> productDetail(@PathVariable Long id){
        ProductResDto productResDto = productService.productDetail(id);
        return new ResponseEntity<>(new CommonResDto(HttpStatus.OK, "상품이 성공적으로 조회되었습니다.", productResDto), HttpStatus.OK);
    }

    @PutMapping("/product/updatestock")
    public ResponseEntity<CommonResDto> productStockUpdate(@RequestBody ProductUpdateStockDto dto){
        productService.productUpdateStock(dto);
        return new ResponseEntity<>(new CommonResDto(HttpStatus.OK, "상품이 업데이트되었습니다.", "ok"), HttpStatus.OK);
    }
}
