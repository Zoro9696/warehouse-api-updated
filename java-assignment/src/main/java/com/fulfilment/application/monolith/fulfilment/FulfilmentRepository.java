package com.fulfilment.application.monolith.fulfilment;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import com.fulfilment.application.monolith.products.Product;
import com.fulfilment.application.monolith.stores.Store;

/**
 * Repository for managing ProductWarehouseStoreAssociation entities.
 */

@ApplicationScoped
public class FulfilmentRepository implements PanacheRepository<ProductWarehouseStoreAssociation> {

    public List<ProductWarehouseStoreAssociation> findByProductAndStore(Product product, Store store) {
        return list("product = ?1 and store = ?2", product, store);
    }

    public List<ProductWarehouseStoreAssociation> findByStore(Store store) {
        return list("store = ?1", store);
    }

    public List<ProductWarehouseStoreAssociation> findByWarehouse(Long warehouseId) {
        return list("warehouseId = ?1", warehouseId);
    }

    public long countWarehousesForProductInStore(Product product, Store store) {
        return getEntityManager().createQuery(
                        "SELECT COUNT(DISTINCT a.warehouseId) " +
                                "FROM ProductWarehouseStoreAssociation a " +
                                "WHERE a.product = ?1 AND a.store = ?2",
                        Long.class
                )
                .setParameter(1, product)
                .setParameter(2, store)
                .getSingleResult();
    }

    public long countWarehousesForStore(Store store) {
        return getEntityManager().createQuery(
                        "SELECT COUNT(DISTINCT a.warehouseId) " +
                                "FROM ProductWarehouseStoreAssociation a " +
                                "WHERE a.store = ?1",
                        Long.class
                )
                .setParameter(1, store)
                .getSingleResult();
    }

    public long countProductTypesInWarehouse(Long warehouseId) {
        return getEntityManager().createQuery(
                        "SELECT COUNT(DISTINCT a.product) " +
                                "FROM ProductWarehouseStoreAssociation a " +
                                "WHERE a.warehouseId = ?1",
                        Long.class
                )
                .setParameter(1, warehouseId)
                .getSingleResult();
    }

    public boolean exists(Product product, Long warehouseId, Store store) {
        return count(
                "product = ?1 and warehouseId = ?2 and store = ?3",
                product,
                warehouseId,
                store
        ) > 0;
    }
}
