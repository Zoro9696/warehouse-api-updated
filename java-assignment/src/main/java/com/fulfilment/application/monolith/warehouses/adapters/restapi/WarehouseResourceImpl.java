package com.fulfilment.application.monolith.warehouses.adapters.restapi;

import com.fulfilment.application.monolith.warehouses.adapters.database.WarehouseRepository;
import com.fulfilment.application.monolith.warehouses.domain.usecases.ArchiveWarehouseUseCase;
import com.fulfilment.application.monolith.warehouses.domain.usecases.CreateWarehouseUseCase;
import com.fulfilment.application.monolith.warehouses.domain.usecases.ReplaceWarehouseUseCase;
import com.warehouse.api.WarehouseResource;
import com.warehouse.api.beans.Warehouse;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.WebApplicationException;

import java.util.List;

@RequestScoped
public class WarehouseResourceImpl implements WarehouseResource {

    @Inject
    private WarehouseRepository warehouseRepository;
    @Inject
    CreateWarehouseUseCase createUseCase;
    @Inject
    ReplaceWarehouseUseCase replaceUseCase;
    @Inject
    ArchiveWarehouseUseCase archiveUseCase;

    @Override
    public List<Warehouse> listAllWarehousesUnits() {
        return warehouseRepository.getAll().stream().map(this::toWarehouseResponse).toList();
    }

    @Override
    @Transactional
    public Warehouse createANewWarehouseUnit(@NotNull Warehouse data) {
        var domain = toDomain(data);
        createUseCase.create(domain);
        return data;
    }
    @Override
    @Transactional
    public Warehouse getAWarehouseUnitByID(String id) {

        com.fulfilment.application.monolith.warehouses.domain.models.Warehouse warehouse;

        try {
            Long warehouseId = Long.parseLong(id);
            warehouse = warehouseRepository.findActiveById(warehouseId);
        } catch (NumberFormatException e) {
            throw new WebApplicationException("Warehouse not found", 404);
        }

        if (warehouse == null) {
            throw new WebApplicationException("Warehouse not found", 404);
        }

        return toWarehouseResponse(warehouse);
    }

    @Override
    @Transactional
    public void archiveAWarehouseUnitByID(String id) {

        com.fulfilment.application.monolith.warehouses.domain.models.Warehouse warehouse;

        try {
            Long warehouseId = Long.parseLong(id);
            warehouse = warehouseRepository.findActiveById(warehouseId);
        } catch (NumberFormatException e) {
            throw new WebApplicationException("Warehouse not found", 404);
        }

        if (warehouse == null) {
            throw new WebApplicationException("Warehouse not found", 404);
        }

        var domain = new com.fulfilment.application.monolith.warehouses.domain.models.Warehouse();
        domain.businessUnitCode = warehouse.businessUnitCode;

        archiveUseCase.archive(domain);
    }


    @Override
    public Warehouse replaceTheCurrentActiveWarehouse(
            String businessUnitCode, @NotNull Warehouse data) {
        var domain = toDomain(data);
        domain.businessUnitCode = businessUnitCode;

        replaceUseCase.replace(domain);
        return data;
    }

    private Warehouse toWarehouseResponse(
            com.fulfilment.application.monolith.warehouses.domain.models.Warehouse warehouse) {
        var response = new Warehouse();
        response.setId(String.valueOf(warehouse.id));
        response.setBusinessUnitCode(warehouse.businessUnitCode);
        response.setLocation(warehouse.location);
        response.setCapacity(warehouse.capacity);
        response.setStock(warehouse.stock);

        return response;
    }
    private com.fulfilment.application.monolith.warehouses.domain.models.Warehouse
    toDomain(Warehouse w) {

        var domain =
                new com.fulfilment.application.monolith.warehouses.domain.models.Warehouse();

        domain.businessUnitCode = w.getBusinessUnitCode();
        domain.location = w.getLocation();
        domain.capacity = w.getCapacity();
        domain.stock = w.getStock();

        return domain;
    }
}