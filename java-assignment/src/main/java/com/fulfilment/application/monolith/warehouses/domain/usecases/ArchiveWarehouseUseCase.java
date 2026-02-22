package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.ArchiveWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.WebApplicationException;

@ApplicationScoped
public class ArchiveWarehouseUseCase implements ArchiveWarehouseOperation {

  private final WarehouseStore warehouseStore;

  public ArchiveWarehouseUseCase(WarehouseStore warehouseStore) {
    this.warehouseStore = warehouseStore;
  }

  @Override
  public void archive(Warehouse warehouse) {
    Warehouse existing =
            warehouseStore.findByBusinessUnitCode(
                    warehouse.businessUnitCode);

    if (warehouse.businessUnitCode == null) {
      throw new NullPointerException("Business unit code required");
    }

    if (existing == null) {
      throw new WebApplicationException(
              "Warehouse not found", 404);
    }

    warehouseStore.remove(existing);
  }
}
