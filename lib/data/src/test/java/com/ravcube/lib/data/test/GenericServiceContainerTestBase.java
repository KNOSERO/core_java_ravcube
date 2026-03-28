package com.ravcube.lib.data.test;

import com.ravcube.lib.data.config.TestApplication;
import com.ravcube.lib.data.entity.SpecializedTestItem;
import com.ravcube.lib.data.entity.TestItem;
import com.ravcube.lib.data.entity.TestItemEntry;
import com.ravcube.lib.data.repo.TestItemRepository;
import com.ravcube.lib.data.service.TestItemService;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@SpringBootTest(classes = TestApplication.class)
abstract class GenericServiceContainerTestBase {

    @Autowired
    protected TestItemService service;

    @Autowired
    protected TestItemRepository repository;

    @Autowired
    protected EntityManager entityManager;

    @BeforeEach
    void clearData() {
        repository.deleteAll();
    }

    static class TestItemFixtures {
        private TestItemFixtures() {
        }

        static TestItem testItem(String name) {
            TestItem item = new TestItem();
            item.setName(name);
            return item;
        }

        static SpecializedTestItem specializedItem(String name, Long byId) {
            SpecializedTestItem item = new SpecializedTestItem();
            item.setName(name);
            item.setById(byId);
            return item;
        }

        static TestItemEntry testItemEntry(String value) {
            TestItemEntry entry = new TestItemEntry();
            entry.setValue(value);
            return entry;
        }
    }

}
