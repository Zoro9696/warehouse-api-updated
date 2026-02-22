package com.fulfilment.application.monolith.fulfillment;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.Query;
import java.util.List;
import com.fulfilment.application.monolith.products.Product;
import com.fulfilment.application.monolith.stores.Store;

/**
 * Repository for managing ProductWarehouseStoreAssociation entities.
 */
@ApplicationScoped
public class FulfillmentRepository implements PanacheRepository<ProductWarehouseStoreAssociation> {

  /**
   * Find all associations for a given product and store.
   */
  public List<ProductWarehouseStoreAssociation> findByProductAndStore(Product product, Store store) {
    return list(
        "product = ?1 and store = ?2",
        product,
        store
    );
  }

  /**
   * Find all associations for a given store.
   */
  public List<ProductWarehouseStoreAssociation> findByStore(Store store) {
    return list("store = ?1", store);
  }

  /**
   * Find all associations for a given warehouse.
   */
  public List<ProductWarehouseStoreAssociation> findByWarehouse(String warehouseBusinessUnitCode) {
    return list("warehouseBusinessUnitCode = ?1", warehouseBusinessUnitCode);
  }

  /**
   * Count the number of warehouses fulfilling a product in a specific store.
   */
  public long countWarehousesForProductInStore(Product product, Store store) {
    Query query = getEntityManager().createQuery(
        "SELECT COUNT(DISTINCT a.warehouseBusinessUnitCode) FROM ProductWarehouseStoreAssociation a " +
        "WHERE a.product = ?1 AND a.store = ?2"
    );
    query.setParameter(1, product);
    query.setParameter(2, store);
    return (Long) query.getSingleResult();
  }

  /**
   * Count the number of warehouses fulfilling any product in a specific store.
   */
  public long countWarehousesForStore(Store store) {
    Query query = getEntityManager().createQuery(
        "SELECT COUNT(DISTINCT a.warehouseBusinessUnitCode) FROM ProductWarehouseStoreAssociation a " +
        "WHERE a.store = ?1"
    );
    query.setParameter(1, store);
    return (Long) query.getSingleResult();
  }

  /**
   * Count the number of product types in a warehouse.
   */
  public long countProductTypesInWarehouse(String warehouseBusinessUnitCode) {
    Query query = getEntityManager().createQuery(
        "SELECT COUNT(DISTINCT a.product) FROM ProductWarehouseStoreAssociation a " +
        "WHERE a.warehouseBusinessUnitCode = ?1"
    );
    query.setParameter(1, warehouseBusinessUnitCode);
    return (Long) query.getSingleResult();
  }

  /**
   * Check if an association already exists.
   */
  public boolean exists(Product product, String warehouseBusinessUnitCode, Store store) {
    long count = count(
        "product = ?1 and warehouseBusinessUnitCode = ?2 and store = ?3",
        product,
        warehouseBusinessUnitCode,
        store
    );
    return count > 0;
  }
}

