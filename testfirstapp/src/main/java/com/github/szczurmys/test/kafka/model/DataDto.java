package com.github.szczurmys.test.kafka.model;

import java.math.BigDecimal;

public class DataDto {
    private String product;
    private BigDecimal value;

    public DataDto(String product, BigDecimal value) {
        this.product = product;
        this.value = value;
    }

    public DataDto() {
    }

    public String getProduct() {
        return product;
    }

    public void setProduct(String product) {
        this.product = product;
    }

    public BigDecimal getValue() {
        return value;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
    }
}
