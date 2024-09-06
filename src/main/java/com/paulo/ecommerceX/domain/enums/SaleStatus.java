package com.paulo.ecommerceX.domain.enums;

import lombok.Getter;

@Getter
public enum SaleStatus {
    PENDING(1),
    COMPLETED(1),
    CANCELED(3);

    private final Integer number;

    SaleStatus(Integer number) {this.number = number;}
}
