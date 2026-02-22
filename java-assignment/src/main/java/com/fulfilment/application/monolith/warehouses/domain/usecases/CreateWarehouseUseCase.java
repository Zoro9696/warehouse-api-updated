package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.CreateWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.LocalDateTime;
import java.util.Objects;

@ApplicationScoped
public class CreateWarehouseUseCase implements CreateWarehouseOperation {

  private final WarehouseStore warehouseStore;
  private final LocationResolver locationResolver;

  public CreateWarehouseUseCase(
          WarehouseStore warehouseStore,
          LocationResolver locationResolver) {
    this.warehouseStore = warehouseStore;
    this.locationResolver = locationResolver;
  }

  @Override
  public void create(Warehouse warehouse) {
    Objects.requireNonNull(warehouse.businessUnitCode,
            "Business Unit Code is required");

    //  uniqueness
    if (warehouseStore.findByBusinessUnitCode(
            warehouse.businessUnitCode) != null) {
      throw new IllegalArgumentException(
              "Business unit already exists");
    }

    //  location validation
    Location location =
            locationResolver.resolveByIdentifier(warehouse.location);

    if (location == null) {
      throw new IllegalArgumentException("Invalid location");
    }

    //  max warehouses validation
    long count =
            warehouseStore.getAll().stream()
                    .filter(w -> w.location.equals(location.identification))
                    .count();

    if (count >= location.maxNumberOfWarehouses) {
      throw new IllegalStateException(
              "Maximum warehouses reached for location");
    }

    //  capacity validation
    if (warehouse.capacity > location.maxCapacity) {
      throw new IllegalArgumentException(
              "Capacity exceeds location limit");
    }

    //  stock validation
    if (warehouse.stock > warehouse.capacity) {
      throw new IllegalArgumentException(
              "Stock exceeds capacity");
    }

    warehouse.createdAt = LocalDateTime.now();
    warehouseStore.create(warehouse);
  }
}
