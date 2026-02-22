package com.fulfilment.application.monolith.fulfillment;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.WebApplicationException;
import java.util.List;
import com.fulfilment.application.monolith.products.Product;
import com.fulfilment.application.monolith.products.ProductRepository;
import com.fulfilment.application.monolith.stores.Store;

/**
 * REST API for managing fulfillment associations between Products, Warehouses, and Stores.
 */
@Path("fulfillment")
@ApplicationScoped
@Produces("application/json")
@Consumes("application/json")
public class FulfillmentResource {

  @Inject
  FulfillmentService fulfillmentService;

  @Inject
  FulfillmentRepository fulfillmentRepository;

  @Inject
  ProductRepository productRepository;

  /**
   * Create a new fulfillment association.
   * POST /fulfillment
   * Body: {
   *   "productId": 1,
   *   "warehouseBusinessUnitCode": "WH001",
   *   "storeId": 2
   * }
   */
  @POST
  @Transactional
  public ProductWarehouseStoreAssociation createAssociation(FulfillmentAssociationRequest request) {
    if (request.productId == null || request.productId <= 0) {
      throw new WebApplicationException("Invalid product ID", 400);
    }
    if (request.warehouseBusinessUnitCode == null || request.warehouseBusinessUnitCode.isBlank()) {
      throw new WebApplicationException("Warehouse business unit code cannot be null or empty", 400);
    }
    if (request.storeId == null || request.storeId <= 0) {
      throw new WebApplicationException("Invalid store ID", 400);
    }

    Product product = productRepository.findById(request.productId);
    if (product == null) {
      throw new WebApplicationException("Product not found with ID: " + request.productId, 404);
    }

    Store store = Store.findById(request.storeId);
    if (store == null) {
      throw new WebApplicationException("Store not found with ID: " + request.storeId, 404);
    }

    return fulfillmentService.createAssociation(product, request.warehouseBusinessUnitCode, store);
  }

  /**
   * Get all fulfillment associations for a product in a store.
   * GET /fulfillment?productId=1&storeId=2
   */
  @GET
  public List<ProductWarehouseStoreAssociation> getAssociations(
      @QueryParam("productId") Long productId,
      @QueryParam("storeId") Long storeId
  ) {
    if (productId == null || storeId == null) {
      throw new WebApplicationException("Both productId and storeId query parameters are required", 400);
    }

    Product product = productRepository.findById(productId);
    if (product == null) {
      throw new WebApplicationException("Product not found with ID: " + productId, 404);
    }

    Store store = Store.findById(storeId);
    if (store == null) {
      throw new WebApplicationException("Store not found with ID: " + storeId, 404);
    }

    return fulfillmentService.getWarehousesForProductInStore(product, store);
  }

  /**
   * Get all fulfillment associations for a store.
   * GET /fulfillment/store/{storeId}
   */
  @GET
  @Path("store/{storeId}")
  public List<ProductWarehouseStoreAssociation> getStoreAssociations(@PathParam("storeId") Long storeId) {
    Store store = Store.findById(storeId);
    if (store == null) {
      throw new WebApplicationException("Store not found with ID: " + storeId, 404);
    }
    return fulfillmentService.getAssociationsForStore(store);
  }

  /**
   * Get all fulfillment associations for a warehouse.
   * GET /fulfillment/warehouse/{warehouseCode}
   */
  @GET
  @Path("warehouse/{warehouseCode}")
  public List<ProductWarehouseStoreAssociation> getWarehouseAssociations(
      @PathParam("warehouseCode") String warehouseCode
  ) {
    return fulfillmentService.getAssociationsForWarehouse(warehouseCode);
  }

  /**
   * Delete a fulfillment association.
   * DELETE /fulfillment/{associationId}
   */
  @DELETE
  @Path("{associationId}")
  @Transactional
  public void deleteAssociation(@PathParam("associationId") Long associationId) {
    ProductWarehouseStoreAssociation association = fulfillmentRepository.findById(associationId);
    if (association == null) {
      throw new WebApplicationException("Association not found with ID: " + associationId, 404);
    }
    fulfillmentService.removeAssociation(association);
  }

  /**
   * Get fulfillment constraints.
   * GET /fulfillment/constraints
   */
  @GET
  @Path("constraints")
  public FulfillmentConstraints getConstraints() {
    return fulfillmentService.getConstraints();
  }
}

