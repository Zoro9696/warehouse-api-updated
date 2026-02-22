package com.fulfilment.application.monolith.fulfillment;

/**
 * DTO representing fulfillment constraints.
 */
public class FulfillmentConstraints {

  public int maxWarehousesPerProductPerStore;
  public int maxWarehousesPerStore;
  public int maxProductTypesPerWarehouse;

  public FulfillmentConstraints(
      int maxWarehousesPerProductPerStore,
      int maxWarehousesPerStore,
      int maxProductTypesPerWarehouse
  ) {
    this.maxWarehousesPerProductPerStore = maxWarehousesPerProductPerStore;
    this.maxWarehousesPerStore = maxWarehousesPerStore;
    this.maxProductTypesPerWarehouse = maxProductTypesPerWarehouse;
  }
}

