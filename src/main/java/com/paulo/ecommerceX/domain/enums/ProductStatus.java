package com.paulo.ecommerceX.domain.enums;

import lombok.Getter;

@Getter
public enum ProductStatus {
    ACTIVE(1),
    INACTIVE(2);

    private final Integer number;

    ProductStatus(Integer number) {this.number = number;}
}
