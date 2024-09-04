package com.paulo.ecommerceX.domain;

import com.paulo.ecommerceX.domain.enums.ProductStatus;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity(name = "tb_product")
@Getter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = "productId")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "product_id")
    private UUID productId;

    @Setter
    @Column(nullable = false, unique = true)
    private String name;

    @Setter
    @Column(nullable = false)
    private Double price;

    @Setter
    @Column(nullable = false)
    private Integer stockQuantity;

    @Setter
    private ProductStatus productStatus = ProductStatus.ACTIVE;

    @OneToMany(mappedBy = "id.product")
    private Set<ProductSale> items = new HashSet<>();
}
