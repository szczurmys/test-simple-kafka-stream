package com.github.szczurmys.test.kafka.model;

import com.google.common.base.MoreObjects;

import java.time.LocalDate;
import java.util.Objects;

public class Key {
    private String site;
    private LocalDate localDate;
    private String product;

    public Key(String site, LocalDate localDate, String product) {
        this.site = site;
        this.localDate = localDate;
        this.product = product;
    }

    public Key() {
    }

    public String getSite() {
        return site;
    }

    public void setSite(String site) {
        this.site = site;
    }

    public LocalDate getLocalDate() {
        return localDate;
    }

    public void setLocalDate(LocalDate localDate) {
        this.localDate = localDate;
    }

    public String getProduct() {
        return product;
    }

    public void setProduct(String product) {
        this.product = product;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Key key = (Key) o;
        return Objects.equals(site, key.site) &&
                Objects.equals(localDate, key.localDate) &&
                Objects.equals(product, key.product);
    }

    @Override
    public int hashCode() {

        return Objects.hash(site, localDate, product);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("site", site)
                .add("localDate", localDate)
                .add("product", product)
                .toString();
    }
}
