package com.paulo.ecommerceX.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.paulo.ecommerceX.domain.enums.SaleStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity(name = "tb_sale")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "saleId")
public class Sale {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "sale_id")
    private UUID saleId;

    @Setter
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "GMT-3")
    private Instant saleDate;

    @Setter
    private SaleStatus saleStatus;

    @Setter
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    public Sale(Instant saleDate, User user, Set<ProductSale> items, SaleStatus saleStatus) {
        this.saleDate = saleDate;
        this.user = user;
        this.items = items;
        this.saleStatus = saleStatus;
    }

    @OneToMany(mappedBy = "id.sale", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ProductSale> items = new HashSet<>();

    public Double getTotal() {
        double sum = 0.0;
        for(ProductSale productSale : items) {
            sum += productSale.getSubTotal();
        }
        return sum;
    }
}
