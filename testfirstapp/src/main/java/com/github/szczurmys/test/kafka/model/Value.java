package com.github.szczurmys.test.kafka.model;

import com.google.common.base.MoreObjects;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

public class Value {
    private BigDecimal value;
    private LocalDateTime localDateTime;

    public Value(BigDecimal value, LocalDateTime localDateTime) {
        this.value = value;
        this.localDateTime = localDateTime;
    }

    public Value() {
    }

    public BigDecimal getValue() {
        return value;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
    }

    public LocalDateTime getLocalDateTime() {
        return localDateTime;
    }

    public void setLocalDateTime(LocalDateTime localDateTime) {
        this.localDateTime = localDateTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Value value1 = (Value) o;
        return Objects.equals(value, value1.value) &&
                Objects.equals(localDateTime, value1.localDateTime);
    }

    @Override
    public int hashCode() {

        return Objects.hash(value, localDateTime);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("value", value)
                .add("localDateTime", localDateTime)
                .toString();
    }
}
