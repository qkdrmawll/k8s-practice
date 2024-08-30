package com.beyond.ordersystem.product.service;

import com.beyond.ordersystem.common.service.StockInventoryService;
import com.beyond.ordersystem.product.domain.Product;
import com.beyond.ordersystem.product.dto.ProductCreateReqDto;
import com.beyond.ordersystem.product.dto.ProductResDto;
import com.beyond.ordersystem.product.dto.ProductSearchDto;
import com.beyond.ordersystem.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class ProductService {

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    private final ProductRepository productRepository;
    private final S3Client s3Client;
    private final StockInventoryService stockInventoryService;

    public ProductResDto productCreate(ProductCreateReqDto dto) {
        MultipartFile image= dto.getProductImage();
        Product product = dto.toEntity();
        product = productRepository.save(product);
        if (dto.getName().contains("sale")){
            stockInventoryService.increaseStock(product.getId(), dto.getStockQuantity());
        }
        try {
            byte[] bytes = image.getBytes();
            Path path = Paths.get("/Users/qkdrmawll/Documents/ordersystem/",
                    product.getId()+"_"+ image.getOriginalFilename());
            Files.write(path,bytes, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
            product.updateImagePath(path.toString());

            return new ProductResDto().fromEntity(product);
        } catch (IOException e) {
            throw new RuntimeException("이미지 저장 실패");
        }
    }

    public ProductResDto productAwsCreate(ProductCreateReqDto dto) {
        MultipartFile image= dto.getProductImage();
        Product product = dto.toEntity();
        Product savedProduct = productRepository.save(product);
        try {
            byte[] bytes = image.getBytes();
            String fileName = savedProduct.getId() + "_" + image.getOriginalFilename();
            Path path = Paths.get("/tmp/",
                    fileName);
//            local pc에 임시 저장-> 프론트에서 바이트로 받아와서 파일로 변환 해줘야하는데 임시 저장하면서 파일로 저장되기 때문에
            Files.write(path,bytes, StandardOpenOption.CREATE, StandardOpenOption.WRITE);

//            aws에 pc에 저장된 파일을 업로드
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(fileName)
                    .build();

            PutObjectResponse putObjectResponse = s3Client.putObject(putObjectRequest, RequestBody.fromFile(path));
            String s3Path = s3Client.utilities().getUrl(a->a.bucket(bucket).key(fileName)).toExternalForm();
            savedProduct.updateImagePath(s3Path);
            return new ProductResDto().fromEntity(savedProduct);
        } catch (IOException e) {
            throw new RuntimeException("이미지 저장 실패");
        }
    }

    public Page<ProductResDto> productList(ProductSearchDto searchDto, Pageable pageable) {
//        검색을 위해 Specification 객체 사용
//        Specification 객체는 복잡한 쿼리를 명세를 이용하여 정의하는 반식으로 쿼리를 쉽게 생성
        Specification<Product> specification = new Specification<Product>() {
            @Override
            public Predicate toPredicate(Root<Product> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                List<Predicate> predicates = new ArrayList<>();
                if (searchDto.getName() != null) {
//                    root =엔티티의 속성을 접근하기 위한 객체
//                    criteriaBuilder= 쿼리를 생성하기 위한 객체
                    predicates.add(criteriaBuilder.like(root.get("name"),"%"+searchDto.getName()+"%"));
                }
                if (searchDto.getCategory()!=null) {
                    predicates.add(criteriaBuilder.like(root.get("category"),"%"+searchDto.getCategory()+"%"));
                }

                Predicate[] predicateArr = new Predicate[predicates.size()];
                for (int i =0;i<predicateArr.length;i++) {
                    predicateArr[i] = predicates.get(i);
                }
//                두가지 쿼리는 and로 연결
                Predicate predicate = criteriaBuilder.and(predicateArr);
                return predicate;
            }
        };
        Page<Product> products = productRepository.findAll(specification, pageable);
        return products.map(a->new ProductResDto().fromEntity(a));
    }
}
