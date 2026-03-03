package com.fulfilment.application.monolith.warehouses.adapters.database;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class WarehouseRepositoryTest {

    @Inject
    WarehouseRepository repository;

    @Inject
    io.quarkus.hibernate.orm.panache.PanacheRepositoryBase<DbWarehouse, Long> panacheRepository;


    @BeforeEach
    @Transactional
    void clearDatabase() {
        repository.deleteAll();
    }

    @Test
    @Transactional
    void shouldCreateWarehouseSuccessfully() {
        Warehouse warehouse = new Warehouse();
        warehouse.businessUnitCode = "BU100";
        warehouse.location = "Pune";
        warehouse.capacity = 100;
        warehouse.stock = 50;

        repository.create(warehouse);

        Warehouse saved =
                repository.findByBusinessUnitCode("BU100");

        assertNotNull(saved);
        assertEquals("Pune", saved.location);
        assertNull(saved.archivedAt);
    }

    @Test
    @Transactional
    void shouldReturnOnlyNonArchivedWarehouses() {

        // Active warehouse
        Warehouse active = new Warehouse();
        active.businessUnitCode = "BU200";
        active.location = "Mumbai";
        active.capacity = 200;
        active.stock = 80;
        repository.create(active);

        // Archived warehouse
        Warehouse archived = new Warehouse();
        archived.businessUnitCode = "BU201";
        archived.location = "Delhi";
        archived.capacity = 300;
        archived.stock = 100;
        repository.create(archived);

        archived.archivedAt = LocalDateTime.now();
        repository.update(archived);

        List<Warehouse> result = repository.getAll();

        assertEquals(1, result.size());
        assertEquals("BU200", result.get(0).businessUnitCode);
    }

    @Test
    @Transactional
    void shouldUpdateWarehouseSuccessfully() {

        Warehouse warehouse = new Warehouse();
        warehouse.businessUnitCode = "BU300";
        warehouse.location = "Chennai";
        warehouse.capacity = 150;
        warehouse.stock = 70;
        repository.create(warehouse);

        warehouse.location = "Hyderabad";
        repository.update(warehouse);

        Warehouse updated =
                repository.findByBusinessUnitCode("BU300");

        assertEquals("Hyderabad", updated.location);
    }

    @Test
    @Transactional
    void shouldSoftDeleteWarehouseUsingUpdate() {

        Warehouse warehouse =
                new Warehouse("BU1", "Pune", 100, 50);

        repository.create(warehouse);

        warehouse.archive();
        repository.update(warehouse);

        List<Warehouse> result = repository.getAll();

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldThrowExceptionOnHardDelete() {

        Warehouse warehouse = new Warehouse();
        warehouse.businessUnitCode = "BU500";

        UnsupportedOperationException exception =
                assertThrows(UnsupportedOperationException.class,
                        () -> repository.remove(warehouse));

        assertEquals(
                "Hard delete not allowed. Use archive (soft delete).",
                exception.getMessage());
    }
}