package com.fulfilment.application.monolith.fulfilment;

/**
 * DTO for creating a fulfillment association request.
 */
public class FulfilmentAssociationRequest {

    public Long productId;
    public Long warehouseId;
    public Long storeId;

    public FulfilmentAssociationRequest() {}

    public FulfilmentAssociationRequest(Long productId, Long warehouseId, Long storeId) {
        this.productId = productId;
        this.warehouseId = warehouseId;
        this.storeId = storeId;
    }
}

