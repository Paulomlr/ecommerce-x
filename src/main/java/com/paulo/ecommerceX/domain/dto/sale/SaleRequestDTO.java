package com.paulo.ecommerceX.domain.dto.sale;

import com.paulo.ecommerceX.domain.dto.productSale.ProductSaleRequestDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record SaleRequestDTO (@NotNull @Size(min = 1) Set<@Valid ProductSaleRequestDTO> items){
}
