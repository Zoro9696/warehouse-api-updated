package com.fulfilment.application.monolith.fulfillment;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import java.util.List;
import com.fulfilment.application.monolith.products.Product;
import com.fulfilment.application.monolith.stores.Store;

/**
 * Service for managing fulfillment associations between Products, Warehouses, and Stores.
 * Enforces business constraints:
 * - Each Product can be fulfilled by max 2 Warehouses per Store
 * - Each Store can be fulfilled by max 3 Warehouses
 * - Each Warehouse can store max 5 different Product types
 */
@ApplicationScoped
public class FulfillmentService {

  private static final int MAX_WAREHOUSES_PER_PRODUCT_PER_STORE = 2;
  private static final int MAX_WAREHOUSES_PER_STORE = 3;
  private static final int MAX_PRODUCT_TYPES_PER_WAREHOUSE = 5;

  @Inject
  FulfillmentRepository fulfillmentRepository;

  public ProductWarehouseStoreAssociation createAssociation(
          Product product,
          String warehouseBusinessUnitCode,
          Store store
  ) {

    if (product == null)
      throw new WebApplicationException("Product cannot be null", 400);

    if (warehouseBusinessUnitCode == null || warehouseBusinessUnitCode.isBlank())
      throw new WebApplicationException("Warehouse business unit code cannot be null or empty", 400);

    if (store == null)
      throw new WebApplicationException("Store cannot be null", 400);

    if (product.id == null) {
      product.persist();
    }

    if (store.id == null) {
      store.persist();
    }

    // Duplicate check
    if (fulfillmentRepository.exists(product, warehouseBusinessUnitCode, store)) {
      throw new WebApplicationException(
              "Association already exists",
              409
      );
    }

    // Constraint 1
    long warehouseCountForProduct =
            fulfillmentRepository.countWarehousesForProductInStore(product, store);

    if (warehouseCountForProduct >= MAX_WAREHOUSES_PER_PRODUCT_PER_STORE) {
      throw new WebApplicationException("Max warehouses per product/store reached", 422);
    }

    // Constraint 2
    long warehouseCountForStore =
            fulfillmentRepository.countWarehousesForStore(store);

    if (warehouseCountForStore >= MAX_WAREHOUSES_PER_STORE) {
      throw new WebApplicationException("Max warehouses per store reached", 422);
    }

    // Constraint 3
    long productCountInWarehouse =
            fulfillmentRepository.countProductTypesInWarehouse(warehouseBusinessUnitCode);

    if (productCountInWarehouse >= MAX_PRODUCT_TYPES_PER_WAREHOUSE) {
      throw new WebApplicationException("Max product types per warehouse reached", 422);
    }

    ProductWarehouseStoreAssociation association =
            new ProductWarehouseStoreAssociation(product, warehouseBusinessUnitCode, store);

    fulfillmentRepository.persist(association);

    return association;
  }

  /**
   * Remove a fulfillment association.
   */
  public void removeAssociation(ProductWarehouseStoreAssociation association) {
    if (association == null) {
      throw new WebApplicationException("Association cannot be null", 400);
    }
    fulfillmentRepository.deleteById(association.id);
  }

  /**
   * Get all warehouses fulfilling a product in a specific store.
   */
  public List<ProductWarehouseStoreAssociation> getWarehousesForProductInStore(Product product, Store store) {
    return fulfillmentRepository.findByProductAndStore(product, store);
  }

  /**
   * Get all fulfillment associations for a store.
   */
  public List<ProductWarehouseStoreAssociation> getAssociationsForStore(Store store) {
    return fulfillmentRepository.findByStore(store);
  }

  /**
   * Get all fulfillment associations for a warehouse.
   */
  public List<ProductWarehouseStoreAssociation> getAssociationsForWarehouse(String warehouseBusinessUnitCode) {
    return fulfillmentRepository.findByWarehouse(warehouseBusinessUnitCode);
  }

  /**
   * Get constraint limits.
   */
  public FulfillmentConstraints getConstraints() {
    return new FulfillmentConstraints(
        MAX_WAREHOUSES_PER_PRODUCT_PER_STORE,
        MAX_WAREHOUSES_PER_STORE,
        MAX_PRODUCT_TYPES_PER_WAREHOUSE
    );
  }
}

