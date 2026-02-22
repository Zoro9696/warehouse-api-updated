package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.ReplaceWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.WebApplicationException;

@ApplicationScoped
public class ReplaceWarehouseUseCase implements ReplaceWarehouseOperation {

  private final WarehouseStore warehouseStore;

  public ReplaceWarehouseUseCase(WarehouseStore warehouseStore) {
    this.warehouseStore = warehouseStore;
  }

  @Override
  public void replace(Warehouse newWarehouse) {

    if (newWarehouse == null) {
      throw new NullPointerException("Warehouse cannot be null");
    }

    if (newWarehouse.businessUnitCode == null) {
      throw new IllegalArgumentException("Business unit code required");
    }

    Warehouse existing =
            warehouseStore.findByBusinessUnitCode(
                    newWarehouse.businessUnitCode);

    if (existing == null) {
      throw new WebApplicationException(
              "Warehouse not found", 404);
    }

//    if (!existing.stock.equals(newWarehouse.stock)) {
//      throw new WebApplicationException(
//              "Stock must match existing warehouse", 422);
//    }

      if (existing.stock != null
              && newWarehouse.stock != null
              && !existing.stock.equals(newWarehouse.stock)) {

          throw new WebApplicationException(
                  "Stock must match existing warehouse", 422);
      }

//    if (newWarehouse.capacity < existing.stock) {
//      throw new WebApplicationException(
//              "Capacity cannot accommodate stock", 422);
//    }

      if (newWarehouse.capacity != null
              && existing.stock != null
              && newWarehouse.capacity < existing.stock) {

          throw new WebApplicationException(
                  "Capacity cannot accommodate stock", 422);
      }

    warehouseStore.update(newWarehouse);
  }
}
