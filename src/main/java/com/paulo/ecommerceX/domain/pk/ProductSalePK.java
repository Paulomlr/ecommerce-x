package com.paulo.ecommerceX.domain.pk;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.paulo.ecommerceX.domain.Product;
import com.paulo.ecommerceX.domain.Sale;
import jakarta.persistence.Embeddable;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Embeddable
@Getter
@Setter
@EqualsAndHashCode
public class ProductSalePK implements Serializable {

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "sale_id")
    private Sale sale;
}
