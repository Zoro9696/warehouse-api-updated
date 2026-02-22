package com.fulfilment.application.monolith.fulfillment;

/**
 * DTO for creating a fulfillment association request.
 */
public class FulfillmentAssociationRequest {

  public Long productId;
  public String warehouseBusinessUnitCode;
  public Long storeId;

  public FulfillmentAssociationRequest() {}

  public FulfillmentAssociationRequest(Long productId, String warehouseBusinessUnitCode, Long storeId) {
    this.productId = productId;
    this.warehouseBusinessUnitCode = warehouseBusinessUnitCode;
    this.storeId = storeId;
  }
}

