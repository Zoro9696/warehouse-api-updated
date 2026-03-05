package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import jakarta.ws.rs.WebApplicationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ReplaceWarehouseUseCaseTest {

    private WarehouseStore warehouseStore;
    private ReplaceWarehouseUseCase useCase;

    @BeforeEach
    void setUp() {
        warehouseStore = mock(WarehouseStore.class);
        useCase = new ReplaceWarehouseUseCase(warehouseStore);
    }

    @Test
    void shouldArchiveExistingAndCreateNewWarehouse() {

        Warehouse existing =
                new Warehouse("BU1", "LOC1", 100, 10,1L);

        Warehouse newWarehouse =
                new Warehouse("BU1", "LOC2", 150, 10,1L);

        when(warehouseStore.findByBusinessUnitCode("BU1"))
                .thenReturn(existing);

        useCase.replace(newWarehouse);

        // verify existing archived
        verify(warehouseStore).update(existing);

        // verify new created
        verify(warehouseStore).create(newWarehouse);

        assertNotNull(existing.archivedAt);
    }

    @Test
    void shouldThrowIfWarehouseNotFound() {

        Warehouse newWarehouse =
                new Warehouse("BU1", "LOC2", 150, 10,1L);

        when(warehouseStore.findByBusinessUnitCode("BU1"))
                .thenReturn(null);

        assertThrows(WebApplicationException.class,
                () -> useCase.replace(newWarehouse));
    }

    @Test
    void shouldThrowIfStockMismatch() {

        Warehouse existing =
                new Warehouse("BU1", "LOC1", 100, 20 , 1L);

        Warehouse newWarehouse =
                new Warehouse("BU1", "LOC2", 150, 10 ,1L);

        when(warehouseStore.findByBusinessUnitCode("BU1"))
                .thenReturn(existing);

        assertThrows(WebApplicationException.class,
                () -> useCase.replace(newWarehouse));
    }

    @Test
    void shouldThrowIfCapacityLessThanStock() {

        Warehouse existing =
                new Warehouse("BU1", "LOC1", 100, 50,1L);

        // capacity is valid for its own stock
        // but less than existing stock
        Warehouse newWarehouse =
                new Warehouse("BU1", "LOC2", 40, 40,1L);

        when(warehouseStore.findByBusinessUnitCode("BU1"))
                .thenReturn(existing);

        assertThrows(WebApplicationException.class,
                () -> useCase.replace(newWarehouse));
    }
}