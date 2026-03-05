package com.fulfilment.application.monolith.fulfilment;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import com.fulfilment.application.monolith.products.Product;
import com.fulfilment.application.monolith.stores.Store;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

/**
 * Association entity linking Products, Warehouses, and Stores.
 * Represents that a specific warehouse can fulfill a product for a specific store.
 */


@Entity
@Table(
        name = "product_warehouse_store_association",
        uniqueConstraints = {
                @UniqueConstraint(
                        columnNames = {"product_id", "warehouse_id", "store_id"},
                        name = "uk_product_warehouse_store"
                )
        }
)
public class ProductWarehouseStoreAssociation extends PanacheEntity {

//  @ManyToOne(optional = false)
//  public Product product;

    @ManyToOne(optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    public Product product;

//  @Column(nullable = false)
//  public String warehouseBusinessUnitCode;

    @Column(name = "warehouse_id", nullable = false)
    public Long warehouseId;

//  @ManyToOne(optional = false)
//  public Store store;

    @ManyToOne(optional = false)
    @JoinColumn(name = "store_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    public Store store;

    @Column(nullable = false)
    public LocalDateTime createdAt;

    public LocalDateTime updatedAt;

    public ProductWarehouseStoreAssociation() {}

    public ProductWarehouseStoreAssociation(
            Product product,
            Long warehouseId,
            Store store) {
        this.product = product;
        this.warehouseId = warehouseId;
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

