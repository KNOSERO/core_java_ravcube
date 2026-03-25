package com.ravcube.lib.data.test;


import com.ravcube.lib.data.entity.QTestItem;
import com.ravcube.lib.data.entity.TestItem;
import com.ravcube.lib.data.service.GenericService;
import org.springframework.stereotype.Service;



@Service
class TestItemService extends GenericService<TestItem, QTestItem, Long> {

}
