package com.fulfilment.application.monolith.warehouses.adapters.restapi;

import com.fulfilment.application.monolith.warehouses.adapters.database.WarehouseRepository;
import com.fulfilment.application.monolith.warehouses.domain.usecases.ArchiveWarehouseUseCase;
import com.fulfilment.application.monolith.warehouses.domain.usecases.CreateWarehouseUseCase;
import com.fulfilment.application.monolith.warehouses.domain.usecases.ReplaceWarehouseUseCase;
import jakarta.ws.rs.WebApplicationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class WarehouseResourceImplTest {

    @Mock
    WarehouseRepository warehouseRepository;

    @Mock
    CreateWarehouseUseCase createUseCase;

    @Mock
    ReplaceWarehouseUseCase replaceUseCase;

    @Mock
    ArchiveWarehouseUseCase archiveUseCase;

    @InjectMocks
    WarehouseResourceImpl resource;

    private com.fulfilment.application.monolith.warehouses.domain.models.Warehouse domainWarehouse;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        domainWarehouse = new com.fulfilment.application.monolith.warehouses.domain.models.Warehouse();
        domainWarehouse.id = 1L;
        domainWarehouse.businessUnitCode = "WH001";
        domainWarehouse.location = "PUNE";
        domainWarehouse.capacity = 100;
        domainWarehouse.stock = 50;
    }

    @Test
    void shouldListAllWarehousesUnits() {

        when(warehouseRepository.getAll()).thenReturn(List.of(domainWarehouse));

        var result = resource.listAllWarehousesUnits();

        assertEquals(1, result.size());
        assertEquals("WH001", result.get(0).getBusinessUnitCode());
        verify(warehouseRepository).getAll();
    }

    @Test
    void shouldCreateWarehouse() {

        var request = new com.warehouse.api.beans.Warehouse();
        request.setBusinessUnitCode("WH001");
        request.setLocation("PUNE");
        request.setCapacity(100);
        request.setStock(50);

        var result = resource.createANewWarehouseUnit(request);

        assertEquals("WH001", result.getBusinessUnitCode());
        verify(createUseCase).create(any());
    }

    @Test
    void shouldGetWarehouseById() {

        when(warehouseRepository.findActiveById(1L)).thenReturn(domainWarehouse);

        var result = resource.getAWarehouseUnitByID("1");

        assertEquals("WH001", result.getBusinessUnitCode());
        verify(warehouseRepository).findActiveById(1L);
    }

    @Test
    void shouldThrowExceptionWhenWarehouseNotFound() {

        when(warehouseRepository.findActiveById(1L)).thenReturn(null);

        assertThrows(WebApplicationException.class,
                () -> resource.getAWarehouseUnitByID("1"));
    }

    @Test
    void shouldThrowExceptionWhenIdInvalid() {

        assertThrows(WebApplicationException.class,
                () -> resource.getAWarehouseUnitByID("abc"));
    }

    @Test
    void shouldArchiveWarehouse() {

        when(warehouseRepository.findActiveById(1L)).thenReturn(domainWarehouse);

        resource.archiveAWarehouseUnitByID("1");

        verify(archiveUseCase).archive(any());
    }

    @Test
    void shouldThrowExceptionWhenArchiveWarehouseNotFound() {

        when(warehouseRepository.findActiveById(1L)).thenReturn(null);

        assertThrows(WebApplicationException.class,
                () -> resource.archiveAWarehouseUnitByID("1"));
    }

    @Test
    void shouldThrowExceptionWhenArchiveIdInvalid() {

        assertThrows(WebApplicationException.class,
                () -> resource.archiveAWarehouseUnitByID("abc"));
    }

    @Test
    void shouldReplaceWarehouse() {

        var request = new com.warehouse.api.beans.Warehouse();
        request.setLocation("MUMBAI");
        request.setCapacity(200);
        request.setStock(100);

        var result = resource.replaceTheCurrentActiveWarehouse("WH001", request);

        assertEquals("MUMBAI", result.getLocation());
        verify(replaceUseCase).replace(any());
    }
}