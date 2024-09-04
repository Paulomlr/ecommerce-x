package com.paulo.ecommerceX.domain.dto.sale;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.paulo.ecommerceX.domain.Sale;
import com.paulo.ecommerceX.domain.dto.productSale.ProductSaleResponseDTO;
import com.paulo.ecommerceX.domain.enums.SaleStatus;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public record SaleResponseDTO (UUID id,
                             @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "GMT-3")
                             Instant instant,
                             SaleStatus saleStatus, Set<ProductSaleResponseDTO> items, Double total) implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    public SaleResponseDTO(Sale sale){
        this(sale.getSaleId(), sale.getSaleDate(), sale.getSaleStatus(),
                sale.getItems().stream().map(ProductSaleResponseDTO::new).collect(Collectors.toSet()), sale.getTotal());
    }
}
