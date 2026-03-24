package com.ravcube.lib.data.view;

import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.IdMapping;
import com.ravcube.lib.data.entity.TestItem;

@EntityView(TestItem.class)
public interface PolicyDto {

    @IdMapping
    Long getId();

    String getName();
}
