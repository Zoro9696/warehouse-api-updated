package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import jakarta.ws.rs.WebApplicationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ArchiveWarehouseUseCaseTest {

    @Mock
    WarehouseStore warehouseStore;

    ArchiveWarehouseUseCase useCase;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        useCase = new ArchiveWarehouseUseCase(warehouseStore);
    }

    @Test
    void shouldArchiveWarehouseSuccessfully() {

        Warehouse existing =
                new Warehouse("BU1", "Pune", 100, 50 ,1l);

        when(warehouseStore.findByBusinessUnitCode("BU1"))
                .thenReturn(existing);

        useCase.archive(existing);

        assertNotNull(existing.archivedAt);
        verify(warehouseStore).update(existing);
    }

    @Test
    void shouldThrow404IfWarehouseNotFound() {

        when(warehouseStore.findByBusinessUnitCode("BU1"))
                .thenReturn(null);

        Warehouse request =
                new Warehouse("BU1", "Pune", 100, 50,1l);

        WebApplicationException ex =
                assertThrows(WebApplicationException.class,
                        () -> useCase.archive(request));

        assertEquals(404, ex.getResponse().getStatus());
    }

    @Test
    void shouldThrowIllegalStateIfAlreadyArchived() {

        Warehouse existing =
                new Warehouse("BU1", "Pune", 100, 50,1l);

        existing.archive();  // already archived

        when(warehouseStore.findByBusinessUnitCode("BU1"))
                .thenReturn(existing);

        assertThrows(IllegalStateException.class,
                () -> useCase.archive(existing));
    }

    @Test
    void shouldThrow404IfBusinessUnitCodeIsNull() {

        Warehouse request = new Warehouse();
        request.businessUnitCode = null;

        when(warehouseStore.findByBusinessUnitCode(null))
                .thenReturn(null);

        WebApplicationException ex =
                assertThrows(WebApplicationException.class,
                        () -> useCase.archive(request));

        assertEquals(404, ex.getResponse().getStatus());
    }
}