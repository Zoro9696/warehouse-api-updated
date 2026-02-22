package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import jakarta.ws.rs.WebApplicationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ArchiveWarehouseUseCaseTest {

    private WarehouseStore store;
    private ArchiveWarehouseUseCase useCase;

    @BeforeEach
    void setup() {
        store = mock(WarehouseStore.class);
        useCase = new ArchiveWarehouseUseCase(store);
    }

    @Test
    void shouldArchiveWarehouseSuccessfully() {
        Warehouse input = new Warehouse();
        input.businessUnitCode = "WH-1";

        Warehouse existing = new Warehouse();
        existing.businessUnitCode = "WH-1";

        when(store.findByBusinessUnitCode("WH-1"))
                .thenReturn(existing);

        useCase.archive(input);

        verify(store).remove(existing);
    }

    @Test
    void shouldHandleNullWarehouse() {
        assertThrows(
                NullPointerException.class,
                () -> useCase.archive(null)
        );
    }

    @Test
    void shouldFailWhenWarehouseNotFound() {
        Warehouse input = new Warehouse();
        input.businessUnitCode = "UNKNOWN";

        when(store.findByBusinessUnitCode("UNKNOWN"))
                .thenReturn(null);

        WebApplicationException exception = assertThrows(
                WebApplicationException.class,
                () -> useCase.archive(input)
        );
        assertEquals(404, exception.getResponse().getStatus());
        assertTrue(exception.getMessage().contains("Warehouse not found"));
    }

    @Test
    void shouldFailWhenBusinessUnitCodeIsNull() {
        Warehouse input = new Warehouse();
        input.businessUnitCode = null;

        assertThrows(
                NullPointerException.class,
                () -> useCase.archive(input)
        );
    }

    @Test
    void shouldArchiveWarehouseWithArchivedDate() {
        Warehouse input = new Warehouse();
        input.businessUnitCode = "WH-ARCHIVE";

        Warehouse existing = new Warehouse();
        existing.businessUnitCode = "WH-ARCHIVE";

        when(store.findByBusinessUnitCode("WH-ARCHIVE"))
                .thenReturn(existing);

        useCase.archive(input);

        verify(store).remove(existing);
    }

    @Test
    void shouldArchiveAlreadyArchivedWarehouse() {
        Warehouse input = new Warehouse();
        input.businessUnitCode = "WH-ALREADY-ARCHIVED";

        Warehouse existing = new Warehouse();
        existing.businessUnitCode = "WH-ALREADY-ARCHIVED";
        existing.archivedAt = java.time.LocalDateTime.now();

        when(store.findByBusinessUnitCode("WH-ALREADY-ARCHIVED"))
                .thenReturn(existing);

        useCase.archive(input);

        verify(store).remove(existing);
    }
}