package com.ravcube.lib.data.test;

import com.ravcube.lib.data.entity.SpecializedTestItem;
import com.ravcube.lib.data.entity.TestItem;
import com.ravcube.lib.data.entity.TestItemEntry;

final class TestItemFixtures {
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
