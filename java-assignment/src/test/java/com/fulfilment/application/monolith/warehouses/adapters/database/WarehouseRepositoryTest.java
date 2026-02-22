package com.fulfilment.application.monolith.warehouses.adapters.database;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
public class WarehouseRepositoryTest {

    @Inject
    WarehouseRepository repository;

    private String testWarehouseCode;
    private String testLocation;

    @BeforeEach
    @Transactional
    void setup() {
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        testWarehouseCode = "WH-" + uuid;
        testLocation = "LOC-" + uuid;

        // Clean up any existing test data
        repository.deleteAll();
    }

    // ==================== GET ALL Tests ====================
    @Test
    @Transactional
    void testGetAllEmpty() {
        List<Warehouse> result = repository.getAll();

        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    @Transactional
    void testGetAllWithData() {
        Warehouse warehouse = new Warehouse();
        warehouse.businessUnitCode = testWarehouseCode;
        warehouse.location = testLocation;
        warehouse.capacity = 100;
        warehouse.stock = 50;

        repository.create(warehouse);

        List<Warehouse> result = repository.getAll();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testWarehouseCode, result.get(0).businessUnitCode);
    }

    @Test
    @Transactional
    void testGetAllMultipleWarehouses() {
        for (int i = 0; i < 3; i++) {
            Warehouse warehouse = new Warehouse();
            warehouse.businessUnitCode = testWarehouseCode + "-" + i;
            warehouse.location = testLocation;
            warehouse.capacity = 100 + i;
            warehouse.stock = 50 + i;
            repository.create(warehouse);
        }

        List<Warehouse> result = repository.getAll();

        assertNotNull(result);
        assertEquals(3, result.size());
    }

    // ==================== CREATE Tests ====================
    @Test
    @Transactional
    void testCreateSuccess() {
        Warehouse warehouse = new Warehouse();
        warehouse.businessUnitCode = testWarehouseCode;
        warehouse.location = testLocation;
        warehouse.capacity = 100;
        warehouse.stock = 50;

        assertDoesNotThrow(() -> repository.create(warehouse));

        List<Warehouse> result = repository.getAll();
        assertEquals(1, result.size());
    }

    @Test
    @Transactional
    void testCreateWithZeroCapacity() {
        Warehouse warehouse = new Warehouse();
        warehouse.businessUnitCode = testWarehouseCode;
        warehouse.location = testLocation;
        warehouse.capacity = 0;
        warehouse.stock = 0;

        assertDoesNotThrow(() -> repository.create(warehouse));
    }

    @Test
    @Transactional
    void testCreateWithLargeCapacity() {
        Warehouse warehouse = new Warehouse();
        warehouse.businessUnitCode = testWarehouseCode;
        warehouse.location = testLocation;
        warehouse.capacity = 999999;
        warehouse.stock = 500000;

        assertDoesNotThrow(() -> repository.create(warehouse));
    }

    @Test
    @Transactional
    void testCreateMultipleWarehouses() {
        for (int i = 0; i < 5; i++) {
            Warehouse warehouse = new Warehouse();
            warehouse.businessUnitCode = testWarehouseCode + "-" + i;
            warehouse.location = testLocation + "-" + i;
            warehouse.capacity = 100 + i;
            warehouse.stock = 50 + i;
            repository.create(warehouse);
        }

        List<Warehouse> result = repository.getAll();
        assertEquals(5, result.size());
    }

    // ==================== UPDATE Tests ====================
    @Test
    @Transactional
    void testUpdateSuccess() {
        Warehouse warehouse = new Warehouse();
        warehouse.businessUnitCode = testWarehouseCode;
        warehouse.location = testLocation;
        warehouse.capacity = 100;
        warehouse.stock = 50;

        repository.create(warehouse);

        warehouse.capacity = 200;
        warehouse.stock = 100;
        repository.update(warehouse);

        List<Warehouse> result = repository.getAll();
        assertEquals(200, result.get(0).capacity);
        assertEquals(100, result.get(0).stock);
    }

    @Test
    @Transactional
    void testUpdateNonexistentWarehouse() {
        Warehouse warehouse = new Warehouse();
        warehouse.businessUnitCode = "NONEXISTENT";
        warehouse.location = testLocation;
        warehouse.capacity = 100;
        warehouse.stock = 50;

        assertThrows(IllegalArgumentException.class, () -> repository.update(warehouse));
    }

    @Test
    @Transactional
    void testUpdateWithArchiveDate() {
        Warehouse warehouse = new Warehouse();
        warehouse.businessUnitCode = testWarehouseCode;
        warehouse.location = testLocation;
        warehouse.capacity = 100;
        warehouse.stock = 50;

        repository.create(warehouse);

        warehouse.archivedAt = LocalDateTime.now();
        repository.update(warehouse);

        List<Warehouse> result = repository.getAll();
        assertNotNull(result.get(0).archivedAt);
    }

    // ==================== FIND BY CODE Tests ====================
    @Test
    @Transactional
    void testFindByBusinessUnitCodeSuccess() {
        Warehouse warehouse = new Warehouse();
        warehouse.businessUnitCode = testWarehouseCode;
        warehouse.location = testLocation;
        warehouse.capacity = 100;
        warehouse.stock = 50;

        repository.create(warehouse);

        Warehouse result = repository.findByBusinessUnitCode(testWarehouseCode);

        assertNotNull(result);
        assertEquals(testWarehouseCode, result.businessUnitCode);
        assertEquals(testLocation, result.location);
    }

    @Test
    @Transactional
    void testFindByBusinessUnitCodeNotFound() {
        Warehouse result = repository.findByBusinessUnitCode("NONEXISTENT");

        assertNull(result);
    }

    @Test
    @Transactional
    void testFindByBusinessUnitCodeWithSpecialCharacters() {
        String specialCode = "WH-@#$%-TEST";
        Warehouse warehouse = new Warehouse();
        warehouse.businessUnitCode = specialCode;
        warehouse.location = testLocation;
        warehouse.capacity = 100;
        warehouse.stock = 50;

        repository.create(warehouse);

        Warehouse result = repository.findByBusinessUnitCode(specialCode);

        assertNotNull(result);
        assertEquals(specialCode, result.businessUnitCode);
    }

    // ==================== ARCHIVE Tests ====================
//    @Test
//    @Transactional
//    void testArchiveSuccess() {
//        Warehouse warehouse = new Warehouse();
//        warehouse.businessUnitCode = testWarehouseCode;
//        warehouse.location = testLocation;
//        warehouse.capacity = 100;
//        warehouse.stock = 50;
//
//        repository.create(warehouse);
//
//        repository.archive(testWarehouseCode);
//
//        Warehouse result = repository.findByBusinessUnitCode(testWarehouseCode);
//        assertNotNull(result.archivedAt);
//    }
//
//    @Test
//    @Transactional
//    void testArchiveNonexistentWarehouse() {
//        assertThrows(IllegalArgumentException.class, () ->
//            repository.archive("NONEXISTENT")
//        );
//    }
//
//    @Test
//    @Transactional
//    void testArchiveMultipleWarehouses() {
//        for (int i = 0; i < 3; i++) {
//            Warehouse warehouse = new Warehouse();
//            warehouse.businessUnitCode = testWarehouseCode + "-" + i;
//            warehouse.location = testLocation;
//            warehouse.capacity = 100 + i;
//            warehouse.stock = 50 + i;
//            repository.create(warehouse);
//        }
//
//        repository.archive(testWarehouseCode + "-0");
//        repository.archive(testWarehouseCode + "-1");
//
//        Warehouse result0 = repository.findByBusinessUnitCode(testWarehouseCode + "-0");
//        Warehouse result1 = repository.findByBusinessUnitCode(testWarehouseCode + "-1");
//        Warehouse result2 = repository.findByBusinessUnitCode(testWarehouseCode + "-2");
//
//        assertNotNull(result0.archivedAt);
//        assertNotNull(result1.archivedAt);
//        assertNull(result2.archivedAt);
//    }

    // ==================== INTEGRATION Tests ====================
    @Test
    @Transactional
    void testCreateReadUpdateFlow() {
        // Create
        Warehouse warehouse = new Warehouse();
        warehouse.businessUnitCode = testWarehouseCode;
        warehouse.location = testLocation;
        warehouse.capacity = 100;
        warehouse.stock = 50;

        repository.create(warehouse);

        // Read
        Warehouse created = repository.findByBusinessUnitCode(testWarehouseCode);
        assertNotNull(created);
        assertEquals(100, created.capacity);

        // Update
        created.capacity = 200;
        repository.update(created);

        // Verify update
        Warehouse updated = repository.findByBusinessUnitCode(testWarehouseCode);
        assertEquals(200, updated.capacity);
    }

//    @Test
//    @Transactional
//    void testCreateReadArchiveFlow() {
//        // Create
//        Warehouse warehouse = new Warehouse();
//        warehouse.businessUnitCode = testWarehouseCode;
//        warehouse.location = testLocation;
//        warehouse.capacity = 100;
//        warehouse.stock = 50;
//
//        repository.create(warehouse);
//
//        // Read
//        Warehouse created = repository.findByBusinessUnitCode(testWarehouseCode);
//        assertNotNull(created);
//
//        // Archive
//        repository.archive(testWarehouseCode);
//
//        // Verify archive
//        Warehouse archived = repository.findByBusinessUnitCode(testWarehouseCode);
//        assertNotNull(archived.archivedAt);
//    }

    @Test
    @Transactional
    void testRepositoryIsApplicationScoped() {
        assertNotNull(repository);
    }

    @Test
    @Transactional
    void testDbWarehouseMapping() {
        Warehouse warehouse = new Warehouse();
        warehouse.businessUnitCode = testWarehouseCode;
        warehouse.location = testLocation;
        warehouse.capacity = 150;
        warehouse.stock = 75;
        warehouse.createdAt = LocalDateTime.now();

        repository.create(warehouse);

        Warehouse result = repository.findByBusinessUnitCode(testWarehouseCode);

        assertNotNull(result);
        assertEquals(warehouse.businessUnitCode, result.businessUnitCode);
        assertEquals(warehouse.location, result.location);
        assertEquals(warehouse.capacity, result.capacity);
        assertEquals(warehouse.stock, result.stock);
    }
}

