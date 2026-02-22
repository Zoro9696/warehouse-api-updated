package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CreateWarehouseUseCaseTest {

    private WarehouseStore warehouseStore;
    private LocationResolver locationResolver;
    private CreateWarehouseUseCase useCase;

    @BeforeEach
    void setup() {
        warehouseStore = mock(WarehouseStore.class);
        locationResolver = mock(LocationResolver.class);
        useCase = new CreateWarehouseUseCase(warehouseStore, locationResolver);
    }

    @Test
    void shouldCreateWarehouseSuccessfully() {
        Warehouse warehouse = new Warehouse();
        warehouse.businessUnitCode = "WH-1";
        warehouse.location = "AMSTERDAM-001";
        warehouse.capacity = 50;
        warehouse.stock = 10;

        Location location = new Location("AMSTERDAM-001", 5, 100);

        when(locationResolver.resolveByIdentifier("AMSTERDAM-001"))
                .thenReturn(location);
        when(warehouseStore.findByBusinessUnitCode("WH-1"))
                .thenReturn(null);
        when(warehouseStore.getAll())
                .thenReturn(new ArrayList<>());

        useCase.create(warehouse);

        verify(warehouseStore).create(warehouse);
        assertNotNull(warehouse.createdAt);
    }

    @Test
    void shouldFailWhenLocationInvalid() {
        Warehouse w = new Warehouse();
        w.businessUnitCode = "WH-1";
        w.location = "INVALID";

        when(locationResolver.resolveByIdentifier("INVALID"))
                .thenThrow(new IllegalArgumentException("Location not found for identifier: INVALID"));

        assertThrows(IllegalArgumentException.class,
                () -> useCase.create(w));
    }

    @Test
    void shouldFailWhenWarehouseNull() {
        assertThrows(NullPointerException.class,
                () -> useCase.create(null));
    }

    @Test
    void shouldFailWhenBusinessUnitMissing() {
        Warehouse w = new Warehouse();
        w.location = "AMSTERDAM-001";

        assertThrows(NullPointerException.class,
                () -> useCase.create(w));
    }

    @Test
    void shouldFailWhenBusinessUnitCodeAlreadyExists() {
        Warehouse warehouse = new Warehouse();
        warehouse.businessUnitCode = "WH-EXISTING";
        warehouse.location = "AMSTERDAM-001";
        warehouse.capacity = 50;
        warehouse.stock = 10;

        when(warehouseStore.findByBusinessUnitCode("WH-EXISTING"))
                .thenReturn(new Warehouse()); // Already exists

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> useCase.create(warehouse)
        );
        assertEquals("Business unit already exists", exception.getMessage());
    }

    @Test
    void shouldFailWhenMaxWarehousesPerLocationExceeded() {
        Warehouse warehouse = new Warehouse();
        warehouse.businessUnitCode = "WH-NEW";
        warehouse.location = "AMSTERDAM-001";
        warehouse.capacity = 50;
        warehouse.stock = 10;

        Location location = new Location("AMSTERDAM-001", 2, 100); // Max 2 warehouses

        Warehouse existing1 = new Warehouse();
        existing1.location = "AMSTERDAM-001";
        Warehouse existing2 = new Warehouse();
        existing2.location = "AMSTERDAM-001";

        when(locationResolver.resolveByIdentifier("AMSTERDAM-001"))
                .thenReturn(location);
        when(warehouseStore.findByBusinessUnitCode("WH-NEW"))
                .thenReturn(null);
        when(warehouseStore.getAll())
                .thenReturn(List.of(existing1, existing2));

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> useCase.create(warehouse)
        );
        assertEquals("Maximum warehouses reached for location", exception.getMessage());
    }

    @Test
    void shouldFailWhenCapacityExceedsLocationLimit() {
        Warehouse warehouse = new Warehouse();
        warehouse.businessUnitCode = "WH-1";
        warehouse.location = "AMSTERDAM-001";
        warehouse.capacity = 150; // Exceeds max capacity of 100
        warehouse.stock = 10;

        Location location = new Location("AMSTERDAM-001", 5, 100);

        when(locationResolver.resolveByIdentifier("AMSTERDAM-001"))
                .thenReturn(location);
        when(warehouseStore.findByBusinessUnitCode("WH-1"))
                .thenReturn(null);
        when(warehouseStore.getAll())
                .thenReturn(new ArrayList<>());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> useCase.create(warehouse)
        );
        assertEquals("Capacity exceeds location limit", exception.getMessage());
    }

    @Test
    void shouldFailWhenStockExceedsCapacity() {
        Warehouse warehouse = new Warehouse();
        warehouse.businessUnitCode = "WH-1";
        warehouse.location = "AMSTERDAM-001";
        warehouse.capacity = 50;
        warehouse.stock = 100; // Exceeds capacity

        Location location = new Location("AMSTERDAM-001", 5, 100);

        when(locationResolver.resolveByIdentifier("AMSTERDAM-001"))
                .thenReturn(location);
        when(warehouseStore.findByBusinessUnitCode("WH-1"))
                .thenReturn(null);
        when(warehouseStore.getAll())
                .thenReturn(new ArrayList<>());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> useCase.create(warehouse)
        );
        assertEquals("Stock exceeds capacity", exception.getMessage());
    }

    @Test
    void shouldFailWhenLocationReturnsNull() {
        Warehouse warehouse = new Warehouse();
        warehouse.businessUnitCode = "WH-1";
        warehouse.location = "UNKNOWN";
        warehouse.capacity = 50;
        warehouse.stock = 10;

        when(locationResolver.resolveByIdentifier("UNKNOWN"))
                .thenReturn(null);
        when(warehouseStore.findByBusinessUnitCode("WH-1"))
                .thenReturn(null);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> useCase.create(warehouse)
        );
        assertEquals("Invalid location", exception.getMessage());
    }

    @Test
    void shouldCreateWarehouseWithZeroStock() {
        Warehouse warehouse = new Warehouse();
        warehouse.businessUnitCode = "WH-ZERO";
        warehouse.location = "AMSTERDAM-001";
        warehouse.capacity = 50;
        warehouse.stock = 0;

        Location location = new Location("AMSTERDAM-001", 5, 100);

        when(locationResolver.resolveByIdentifier("AMSTERDAM-001"))
                .thenReturn(location);
        when(warehouseStore.findByBusinessUnitCode("WH-ZERO"))
                .thenReturn(null);
        when(warehouseStore.getAll())
                .thenReturn(new ArrayList<>());

        useCase.create(warehouse);

        verify(warehouseStore).create(warehouse);
    }

    @Test
    void shouldCreateWarehouseAtCapacityLimit() {
        Warehouse warehouse = new Warehouse();
        warehouse.businessUnitCode = "WH-MAX";
        warehouse.location = "AMSTERDAM-001";
        warehouse.capacity = 100;
        warehouse.stock = 100;

        Location location = new Location("AMSTERDAM-001", 5, 100);

        when(locationResolver.resolveByIdentifier("AMSTERDAM-001"))
                .thenReturn(location);
        when(warehouseStore.findByBusinessUnitCode("WH-MAX"))
                .thenReturn(null);
        when(warehouseStore.getAll())
                .thenReturn(new ArrayList<>());

        useCase.create(warehouse);

        verify(warehouseStore).create(warehouse);
    }
}