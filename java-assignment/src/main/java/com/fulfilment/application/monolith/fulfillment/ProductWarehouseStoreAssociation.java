package com.fulfilment.application.monolith.fulfillment;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import com.fulfilment.application.monolith.products.Product;
import com.fulfilment.application.monolith.stores.Store;

/**
 * Association entity linking Products, Warehouses, and Stores.
 * Represents that a specific warehouse can fulfill a product for a specific store.
 */

@Entity
@Table(
        name = "product_warehouse_store_association",
        uniqueConstraints = {
                @UniqueConstraint(
                        columnNames = {"product_id", "warehouseBusinessUnitCode", "store_id"},
                        name = "uk_product_warehouse_store"
                )
        }
)
public class ProductWarehouseStoreAssociation extends PanacheEntity {

  @ManyToOne(optional = false)
  public Product product;

  @Column(nullable = false)
  public String warehouseBusinessUnitCode;

  @ManyToOne(optional = false)
  public Store store;

  @Column(nullable = false)
  public LocalDateTime createdAt;

  public LocalDateTime updatedAt;

  public ProductWarehouseStoreAssociation() {}

  public ProductWarehouseStoreAssociation(
          Product product,
          String warehouseBusinessUnitCode,
          Store store) {
    this.product = product;
    this.warehouseBusinessUnitCode = warehouseBusinessUnitCode;
    this.store = store;
  }

  @PrePersist
  void onCreate() {
    createdAt = LocalDateTime.now();
  }

  @PreUpdate
  void onUpdate() {
    updatedAt = LocalDateTime.now();
  }
}

