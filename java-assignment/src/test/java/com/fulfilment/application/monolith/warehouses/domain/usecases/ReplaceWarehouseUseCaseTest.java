package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import jakarta.ws.rs.WebApplicationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ReplaceWarehouseUseCaseTest {

    private WarehouseStore store;
    private ReplaceWarehouseUseCase useCase;

    @BeforeEach
    void setup() {
        store = mock(WarehouseStore.class);
        useCase = new ReplaceWarehouseUseCase(store);
    }

    @Test
    void shouldReplaceWarehouseSuccessfully() {
        Warehouse existing = new Warehouse();
        existing.businessUnitCode = "WH-1";
        existing.stock = 10;
        existing.capacity = 50;

        Warehouse newWarehouse = new Warehouse();
        newWarehouse.businessUnitCode = "WH-1";
        newWarehouse.stock = 10;
        newWarehouse.capacity = 50;

        when(store.findByBusinessUnitCode("WH-1"))
                .thenReturn(existing);

        useCase.replace(newWarehouse);

        verify(store).update(newWarehouse);
    }

    @Test
    void shouldHandleNullWarehouse() {
        assertThrows(
                NullPointerException.class,
                () -> useCase.replace(null)
        );
    }

    @Test
    void shouldFailWhenBusinessUnitCodeNull() {
        Warehouse newWarehouse = new Warehouse();
        newWarehouse.businessUnitCode = null;

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> useCase.replace(newWarehouse)
        );
        assertEquals("Business unit code required", exception.getMessage());
    }

    @Test
    void shouldFailWhenWarehouseNotFound() {
        Warehouse newWarehouse = new Warehouse();
        newWarehouse.businessUnitCode = "UNKNOWN";
        newWarehouse.stock = 10;

        when(store.findByBusinessUnitCode("UNKNOWN"))
                .thenReturn(null);

        WebApplicationException exception = assertThrows(
                WebApplicationException.class,
                () -> useCase.replace(newWarehouse)
        );
        assertEquals(404, exception.getResponse().getStatus());
        assertTrue(exception.getMessage().contains("Warehouse not found"));
    }

    @Test
    void shouldFailWhenStockMismatch() {
        Warehouse existing = new Warehouse();
        existing.businessUnitCode = "WH-1";
        existing.stock = 10;
        existing.capacity = 50;

        Warehouse newWarehouse = new Warehouse();
        newWarehouse.businessUnitCode = "WH-1";
        newWarehouse.stock = 20; // Different stock
        newWarehouse.capacity = 50;

        when(store.findByBusinessUnitCode("WH-1"))
                .thenReturn(existing);

        WebApplicationException exception = assertThrows(
                WebApplicationException.class,
                () -> useCase.replace(newWarehouse)
        );
        assertEquals(422, exception.getResponse().getStatus());
        assertTrue(exception.getMessage().contains("Stock must match"));
    }

    @Test
    void shouldFailWhenCapacityTooSmallForStock() {
        Warehouse existing = new Warehouse();
        existing.businessUnitCode = "WH-1";
        existing.stock = 50;
        existing.capacity = 50;

        Warehouse newWarehouse = new Warehouse();
        newWarehouse.businessUnitCode = "WH-1";
        newWarehouse.stock = 50;
        newWarehouse.capacity = 40; // Cannot accommodate existing stock

        when(store.findByBusinessUnitCode("WH-1"))
                .thenReturn(existing);

        WebApplicationException exception = assertThrows(
                WebApplicationException.class,
                () -> useCase.replace(newWarehouse)
        );
        assertEquals(422, exception.getResponse().getStatus());
        assertTrue(exception.getMessage().contains("Capacity cannot accommodate stock"));
    }

    @Test
    void shouldReplaceWithNullStockWhenExistingStockNull() {
        Warehouse existing = new Warehouse();
        existing.businessUnitCode = "WH-NULL-STOCK";
        existing.stock = null;
        existing.capacity = 50;

        Warehouse newWarehouse = new Warehouse();
        newWarehouse.businessUnitCode = "WH-NULL-STOCK";
        newWarehouse.stock = null;
        newWarehouse.capacity = 50;

        when(store.findByBusinessUnitCode("WH-NULL-STOCK"))
                .thenReturn(existing);

        useCase.replace(newWarehouse);

        verify(store).update(newWarehouse);
    }

    @Test
    void shouldReplaceWithNullCapacityWhenExistingStockNull() {
        Warehouse existing = new Warehouse();
        existing.businessUnitCode = "WH-1";
        existing.stock = null;
        existing.capacity = 50;

        Warehouse newWarehouse = new Warehouse();
        newWarehouse.businessUnitCode = "WH-1";
        newWarehouse.stock = null;
        newWarehouse.capacity = null;

        when(store.findByBusinessUnitCode("WH-1"))
                .thenReturn(existing);

        useCase.replace(newWarehouse);

        verify(store).update(newWarehouse);
    }

    @Test
    void shouldReplaceWithDifferentLocationAndCapacity() {
        Warehouse existing = new Warehouse();
        existing.businessUnitCode = "WH-1";
        existing.stock = 10;
        existing.location = "OLD-LOCATION";
        existing.capacity = 50;

        Warehouse newWarehouse = new Warehouse();
        newWarehouse.businessUnitCode = "WH-1";
        newWarehouse.stock = 10;
        newWarehouse.location = "NEW-LOCATION";
        newWarehouse.capacity = 100;

        when(store.findByBusinessUnitCode("WH-1"))
                .thenReturn(existing);

        useCase.replace(newWarehouse);

        verify(store).update(newWarehouse);
    }

    @Test
    void shouldReplaceWithZeroStock() {
        Warehouse existing = new Warehouse();
        existing.businessUnitCode = "WH-1";
        existing.stock = 0;
        existing.capacity = 50;

        Warehouse newWarehouse = new Warehouse();
        newWarehouse.businessUnitCode = "WH-1";
        newWarehouse.stock = 0;
        newWarehouse.capacity = 50;

        when(store.findByBusinessUnitCode("WH-1"))
                .thenReturn(existing);

        useCase.replace(newWarehouse);

        verify(store).update(newWarehouse);
    }
}