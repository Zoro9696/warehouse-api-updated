package com.fulfilment.application.monolith.fulfillment;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * Repository for querying fulfillment statistics and reports.
 */
@ApplicationScoped
public class FulfillmentAnalyticsRepository implements PanacheRepository<ProductWarehouseStoreAssociation> {

  /**
   * Get fulfillment statistics for a store.
   */
  public long countAssociationsForStore(Long storeId) {
    return count("store.id = ?1", storeId);
  }

  /**
   * Get fulfillment statistics for a product.
   */
  public long countAssociationsForProduct(Long productId) {
    return count("product.id = ?1", productId);
  }

  /**
   * Get average number of warehouses per product.
   */
  public double getAverageWarehousesPerProduct() {
    Long totalAssociations = count();
    Long totalProducts = getEntityManager()
        .createQuery("SELECT COUNT(DISTINCT p.id) FROM ProductWarehouseStoreAssociation a JOIN a.product p", Long.class)
        .getSingleResult();

    return totalProducts > 0 ? (double) totalAssociations / totalProducts : 0.0;
  }
}

