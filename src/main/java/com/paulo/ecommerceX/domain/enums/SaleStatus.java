package com.paulo.ecommerceX.domain.enums;

import lombok.Getter;

@Getter
public enum SaleStatus {
    COMPLETED(1),
    CANCELED(2);

    private final Integer number;

    SaleStatus(Integer number) {this.number = number;}
}
