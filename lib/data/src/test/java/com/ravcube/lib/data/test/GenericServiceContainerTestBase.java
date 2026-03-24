package com.ravcube.lib.data.test;

import com.ravcube.lib.data.repo.TestItemRepository;
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
}
