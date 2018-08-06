package com.github.szczurmys.test.kafka.model;

import java.time.LocalDateTime;
import java.util.List;

public class DataListDto {
    private String site;
    private LocalDateTime localDateTime;
    private List<DataDto> elements;

    public DataListDto(String site, LocalDateTime localDateTime, List<DataDto> elements) {
        this.site = site;
        this.localDateTime = localDateTime;
        this.elements = elements;
    }

    public DataListDto() {
    }

    public String getSite() {
        return site;
    }

    public void setSite(String site) {
        this.site = site;
    }

    public LocalDateTime getLocalDateTime() {
        return localDateTime;
    }

    public void setLocalDateTime(LocalDateTime localDateTime) {
        this.localDateTime = localDateTime;
    }

    public List<DataDto> getElements() {
        return elements;
    }

    public void setElements(List<DataDto> elements) {
        this.elements = elements;
    }
}
