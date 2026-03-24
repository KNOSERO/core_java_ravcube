package com.ravcube.lib.data.entity;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("SPECIALIZED")
public class SpecializedTestItem extends TestItem {

    private Long byId;

    public Long getById() {
        return byId;
    }

    public void setById(Long byId) {
        this.byId = byId;
    }
}
