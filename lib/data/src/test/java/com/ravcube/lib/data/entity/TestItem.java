package com.ravcube.lib.data.entity;

import com.ravcube.lib.data.entity.EntityClass;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "test_item")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public class TestItem implements EntityClass<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @OneToMany(mappedBy = "item", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TestItemEntry> entries = new ArrayList<>();

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<TestItemEntry> getEntries() {
        return entries;
    }

    public void addEntry(TestItemEntry entry) {
        entries.add(entry);
        entry.setItem(this);
    }
}
