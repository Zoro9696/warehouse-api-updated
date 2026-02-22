package com.fulfilment.application.monolith.warehouses.adapters.restapi;

import com.fulfilment.application.monolith.warehouses.adapters.database.WarehouseRepository;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.usecases.ArchiveWarehouseUseCase;
import com.fulfilment.application.monolith.warehouses.domain.usecases.CreateWarehouseUseCase;
import com.fulfilment.application.monolith.warehouses.domain.usecases.ReplaceWarehouseUseCase;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@QuarkusTest
class WarehouseResourceImplTest {

    @Inject
    private WarehouseResourceImpl warehouseResource;

    @InjectMock
    private WarehouseRepository warehouseRepository;

    @InjectMock
    private CreateWarehouseUseCase createUseCase;

    @InjectMock
    private ReplaceWarehouseUseCase replaceUseCase;

    @InjectMock
    private ArchiveWarehouseUseCase archiveUseCase;

    private Warehouse testWarehouse;

    @BeforeEach
    void setUp() {
        testWarehouse = new Warehouse();
        testWarehouse.businessUnitCode = "WH001";
        testWarehouse.location = "LOC001";
        testWarehouse.capacity = 1000;
        testWarehouse.stock = 500;
        testWarehouse.createdAt = LocalDateTime.now();
    }

    // LIST WAREHOUSE TESTS
    @Test
    void shouldListAllWarehousesSuccessfully() {
        List<Warehouse> warehouses = new ArrayList<>();
        warehouses.add(testWarehouse);

        when(warehouseRepository.getAll()).thenReturn(warehouses);

        List<com.warehouse.api.beans.Warehouse> result = warehouseResource.listAllWarehousesUnits();

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(warehouseRepository, times(1)).getAll();
    }

    @Test
    void shouldListEmptyWarehousesList() {
        when(warehouseRepository.getAll()).thenReturn(new ArrayList<>());

        List<com.warehouse.api.beans.Warehouse> result = warehouseResource.listAllWarehousesUnits();

        assertNotNull(result);
        assertEquals(0, result.size());
        verify(warehouseRepository, times(1)).getAll();
    }

    @Test
    void shouldListMultipleWarehouses() {
        List<Warehouse> warehouses = new ArrayList<>();

        Warehouse wh1 = new Warehouse();
        wh1.businessUnitCode = "WH001";
        wh1.location = "LOC001";
        wh1.capacity = 1000;
        wh1.stock = 500;
        warehouses.add(wh1);

        Warehouse wh2 = new Warehouse();
        wh2.businessUnitCode = "WH002";
        wh2.location = "LOC002";
        wh2.capacity = 2000;
        wh2.stock = 1000;
        warehouses.add(wh2);

        Warehouse wh3 = new Warehouse();
        wh3.businessUnitCode = "WH003";
        wh3.location = "LOC003";
        wh3.capacity = 3000;
        wh3.stock = 1500;
        warehouses.add(wh3);

        when(warehouseRepository.getAll()).thenReturn(warehouses);

        List<com.warehouse.api.beans.Warehouse> result = warehouseResource.listAllWarehousesUnits();

        assertNotNull(result);
        assertEquals(3, result.size());
    }

    // CREATE WAREHOUSE TESTS
    @Test
    void shouldCreateWarehouseSuccessfully() {
        com.warehouse.api.beans.Warehouse request = new com.warehouse.api.beans.Warehouse();
        request.setBusinessUnitCode("WH001");
        request.setLocation("LOC001");
        request.setCapacity(1000);
        request.setStock(500);

        doNothing().when(createUseCase).create(any(Warehouse.class));

        com.warehouse.api.beans.Warehouse result = warehouseResource.createANewWarehouseUnit(request);

        assertNotNull(result);
        assertEquals("WH001", result.getBusinessUnitCode());
        verify(createUseCase, times(1)).create(any(Warehouse.class));
    }

    @Test
    void shouldCreateWarehouseWithSpecialCharacters() {
        com.warehouse.api.beans.Warehouse request = new com.warehouse.api.beans.Warehouse();
        request.setBusinessUnitCode("WH@#$001");
        request.setLocation("LOC@#001");
        request.setCapacity(500);
        request.setStock(250);

        doNothing().when(createUseCase).create(any(Warehouse.class));

        com.warehouse.api.beans.Warehouse result = warehouseResource.createANewWarehouseUnit(request);

        assertNotNull(result);
        assertEquals("WH@#$001", result.getBusinessUnitCode());
    }

    // GET WAREHOUSE TESTS
    @Test
    void shouldGetWarehouseByCodeSuccessfully() {
        when(warehouseRepository.findByBusinessUnitCode("WH001")).thenReturn(testWarehouse);

        com.warehouse.api.beans.Warehouse result = warehouseResource.getAWarehouseUnitByID("WH001");

        assertNotNull(result);
        assertEquals("WH001", result.getBusinessUnitCode());
        verify(warehouseRepository, times(1)).findByBusinessUnitCode("WH001");
    }

    @Test
    void shouldThrow404WhenWarehouseNotFound() {
        when(warehouseRepository.findByBusinessUnitCode("NONEXISTENT")).thenReturn(null);

        assertThrows(WebApplicationException.class, () ->
                warehouseResource.getAWarehouseUnitByID("NONEXISTENT")
        );
    }

    // ARCHIVE WAREHOUSE TESTS
    @Test
    void shouldArchiveWarehouseSuccessfully() {
        Warehouse archivedWh = new Warehouse();
        archivedWh.businessUnitCode = "WH001";
        archivedWh.archivedAt = LocalDateTime.now();

        doNothing().when(archiveUseCase).archive(any(Warehouse.class));

        assertDoesNotThrow(() -> warehouseResource.archiveAWarehouseUnitByID("WH001"));

        verify(archiveUseCase, times(1)).archive(any(Warehouse.class));
    }

    @Test
    void shouldArchiveMultipleWarehouses() {
        doNothing().when(archiveUseCase).archive(any(Warehouse.class));

        assertDoesNotThrow(() -> warehouseResource.archiveAWarehouseUnitByID("WH001"));
        assertDoesNotThrow(() -> warehouseResource.archiveAWarehouseUnitByID("WH002"));

        verify(archiveUseCase, times(2)).archive(any(Warehouse.class));
    }

    // REPLACE WAREHOUSE TESTS
    @Test
    void shouldReplaceWarehouseSuccessfully() {
        com.warehouse.api.beans.Warehouse updateRequest = new com.warehouse.api.beans.Warehouse();
        updateRequest.setBusinessUnitCode("WH001");
        updateRequest.setLocation("LOC001");
        updateRequest.setCapacity(2000);
        updateRequest.setStock(1000);

        doNothing().when(replaceUseCase).replace(any(Warehouse.class));

        com.warehouse.api.beans.Warehouse result = warehouseResource.replaceTheCurrentActiveWarehouse("WH001", updateRequest);

        assertNotNull(result);
        assertEquals("WH001", result.getBusinessUnitCode());
        assertEquals(2000, result.getCapacity());
        verify(replaceUseCase, times(1)).replace(any(Warehouse.class));
    }

    @Test
    void shouldReplaceWarehouseWithDifferentCode() {
        com.warehouse.api.beans.Warehouse updateRequest = new com.warehouse.api.beans.Warehouse();
        updateRequest.setBusinessUnitCode("WH001");
        updateRequest.setLocation("LOC001");
        updateRequest.setCapacity(1500);
        updateRequest.setStock(750);

        doNothing().when(replaceUseCase).replace(any(Warehouse.class));

        com.warehouse.api.beans.Warehouse result = warehouseResource.replaceTheCurrentActiveWarehouse("NEW_CODE", updateRequest);

        assertNotNull(result);
        verify(replaceUseCase, times(1)).replace(any(Warehouse.class));
    }

    // EDGE CASES
    @Test
    void shouldHandleNullLocation() {
        com.warehouse.api.beans.Warehouse request = new com.warehouse.api.beans.Warehouse();
        request.setBusinessUnitCode("NULL_LOC");
        request.setLocation(null);
        request.setCapacity(500);
        request.setStock(250);

        doNothing().when(createUseCase).create(any(Warehouse.class));

        com.warehouse.api.beans.Warehouse result = warehouseResource.createANewWarehouseUnit(request);

        assertNull(result.getLocation());
    }

    @Test
    void shouldHandleZeroCapacity() {
        com.warehouse.api.beans.Warehouse request = new com.warehouse.api.beans.Warehouse();
        request.setBusinessUnitCode("ZERO");
        request.setLocation("LOC");
        request.setCapacity(0);
        request.setStock(0);

        doNothing().when(createUseCase).create(any(Warehouse.class));

        com.warehouse.api.beans.Warehouse result = warehouseResource.createANewWarehouseUnit(request);

        assertEquals(0, result.getCapacity());
    }

    @Test
    void shouldHandleNegativeCapacity() {
        com.warehouse.api.beans.Warehouse request = new com.warehouse.api.beans.Warehouse();
        request.setBusinessUnitCode("NEGATIVE");
        request.setLocation("LOC");
        request.setCapacity(-100);
        request.setStock(-50);

        doNothing().when(createUseCase).create(any(Warehouse.class));

        com.warehouse.api.beans.Warehouse result = warehouseResource.createANewWarehouseUnit(request);

        assertEquals(-100, result.getCapacity());
    }

    @Test
    void shouldHandleUnicodeCharacters() {
        com.warehouse.api.beans.Warehouse request = new com.warehouse.api.beans.Warehouse();
        request.setBusinessUnitCode("UNICODE");
        request.setLocation("Almacén №1 中文 العربية");
        request.setCapacity(500);
        request.setStock(250);

        doNothing().when(createUseCase).create(any(Warehouse.class));

        com.warehouse.api.beans.Warehouse result = warehouseResource.createANewWarehouseUnit(request);

        assertEquals("Almacén №1 中文 العربية", result.getLocation());
    }

    @Test
    void shouldHandleLongWarehouseCode() {
        String longCode = "WH" + "0".repeat(100);
        com.warehouse.api.beans.Warehouse request = new com.warehouse.api.beans.Warehouse();
        request.setBusinessUnitCode(longCode);
        request.setLocation("LOC");
        request.setCapacity(500);
        request.setStock(250);

        doNothing().when(createUseCase).create(any(Warehouse.class));

        com.warehouse.api.beans.Warehouse result = warehouseResource.createANewWarehouseUnit(request);

        assertEquals(longCode, result.getBusinessUnitCode());
    }

    @Test
    void shouldHandleMaxIntegerCapacity() {
        com.warehouse.api.beans.Warehouse request = new com.warehouse.api.beans.Warehouse();
        request.setBusinessUnitCode("MAX");
        request.setLocation("LOC");
        request.setCapacity(Integer.MAX_VALUE);
        request.setStock(Integer.MAX_VALUE / 2);

        doNothing().when(createUseCase).create(any(Warehouse.class));

        com.warehouse.api.beans.Warehouse result = warehouseResource.createANewWarehouseUnit(request);

        assertEquals(Integer.MAX_VALUE, result.getCapacity());
    }

    @Test
    void shouldHandleWarehouseWithNullCode() {
        when(warehouseRepository.findByBusinessUnitCode(null)).thenReturn(null);

        assertThrows(WebApplicationException.class, () ->
                warehouseResource.getAWarehouseUnitByID(null)
        );
    }

    @Test
    void shouldCreateWarehouseAndVerifyAllFields() {
        com.warehouse.api.beans.Warehouse request = new com.warehouse.api.beans.Warehouse();
        request.setBusinessUnitCode("FULL_TEST");
        request.setLocation("TEST_LOC");
        request.setCapacity(5000);
        request.setStock(2500);

        doNothing().when(createUseCase).create(any(Warehouse.class));

        com.warehouse.api.beans.Warehouse result = warehouseResource.createANewWarehouseUnit(request);

        assertNotNull(result);
        assertEquals("FULL_TEST", result.getBusinessUnitCode());
        assertEquals("TEST_LOC", result.getLocation());
        assertEquals(5000, result.getCapacity());
        assertEquals(2500, result.getStock());
    }


    @Test
    void shouldHandleArchiveNullCode() {
        doNothing().when(archiveUseCase).archive(any(Warehouse.class));

        assertDoesNotThrow(() -> warehouseResource.archiveAWarehouseUnitByID(null));
    }

    @Test
    void shouldHandleReplaceWithLargeStock() {
        com.warehouse.api.beans.Warehouse updateRequest = new com.warehouse.api.beans.Warehouse();
        updateRequest.setBusinessUnitCode("WH001");
        updateRequest.setLocation("LOC001");
        updateRequest.setCapacity(Integer.MAX_VALUE);
        updateRequest.setStock(Integer.MAX_VALUE / 2);

        doNothing().when(replaceUseCase).replace(any(Warehouse.class));

        com.warehouse.api.beans.Warehouse result = warehouseResource.replaceTheCurrentActiveWarehouse("WH001", updateRequest);

        assertNotNull(result);
        assertEquals(Integer.MAX_VALUE, result.getCapacity());
    }
}

