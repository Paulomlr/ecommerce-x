package com.paulo.ecommerceX.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.paulo.ecommerceX.domain.pk.ProductSalePK;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity(name = "tb_product_sale")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
public class ProductSale {

    @EmbeddedId
    @JsonIgnore
    private ProductSalePK id = new ProductSalePK();

    private Integer quantity;

    public ProductSale(Product product,Integer quantity) {
        id.setProduct(product);
        this.quantity = quantity;
    }

    public ProductSale(Product product, Sale sale, Integer quantity){
        id.setProduct(product);
        id.setSale(sale);
        this.quantity = quantity;
    }

    @JsonIgnore
    public Sale getSale(){
        return id.getSale();
    }

    public Product getProduct(){
        return id.getProduct();
    }

    public Double getSubTotal() {
        return getProduct().getPrice() * quantity;
    }
}
