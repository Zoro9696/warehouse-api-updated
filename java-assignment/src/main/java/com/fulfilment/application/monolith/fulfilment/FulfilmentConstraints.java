package com.fulfilment.application.monolith.fulfilment;

/**
 * DTO representing fulfillment constraints.
 */
public class FulfilmentConstraints {

    public int maxWarehousesPerProductPerStore;
    public int maxWarehousesPerStore;
    public int maxProductTypesPerWarehouse;

    public FulfilmentConstraints(
            int maxWarehousesPerProductPerStore,
            int maxWarehousesPerStore,
            int maxProductTypesPerWarehouse
    ) {
        this.maxWarehousesPerProductPerStore = maxWarehousesPerProductPerStore;
        this.maxWarehousesPerStore = maxWarehousesPerStore;
        this.maxProductTypesPerWarehouse = maxProductTypesPerWarehouse;
    }
}

