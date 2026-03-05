package com.fulfilment.application.monolith.warehouses.adapters.database;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@ApplicationScoped
public class WarehouseRepository implements WarehouseStore, PanacheRepository<DbWarehouse> {


  @Override
  public List<Warehouse> getAll() {
    return find("archivedAt is null")
            .list()
            .stream()
            .map(DbWarehouse::toWarehouse)
            .toList();
  }


  @Override
  @Transactional
  public void create(Warehouse warehouse) {
    DbWarehouse entity = new DbWarehouse();
    entity.businessUnitCode = warehouse.businessUnitCode;
    entity.location = warehouse.location;
    entity.capacity = warehouse.capacity;
    entity.stock = warehouse.stock;
    entity.createdAt = LocalDateTime.now();
    entity.archivedAt = null;

    persist(entity);
  }

  @Override
  @Transactional
  public void update(Warehouse warehouse) {
    DbWarehouse entity =
            find("businessUnitCode", warehouse.businessUnitCode)
                    .firstResult();

    if (entity == null) {
      throw new IllegalArgumentException(
              "Warehouse not found: " + warehouse.businessUnitCode);
    }

    entity.location = warehouse.location;
    entity.capacity = warehouse.capacity;
    entity.stock = warehouse.stock;
    entity.archivedAt = warehouse.archivedAt;
  }

  /**
   * Archive use case must perform soft delete.
   */
  @Override
  @Transactional
  public void remove(Warehouse warehouse) {
    throw new UnsupportedOperationException(
            "Hard delete not allowed. Use archive (soft delete).");
  }

  @Override
  public Warehouse findByBusinessUnitCode(String buCode) {
    DbWarehouse entity =
            find("businessUnitCode = ?1 and archivedAt is null", buCode)
                    .firstResult();

    return entity == null ? null : entity.toWarehouse();
  }


    public Warehouse findActiveById(Long id) {

        DbWarehouse entity =
                find("id = ?1 and archivedAt is null", id)
                        .firstResult();

        return entity == null ? null : entity.toWarehouse();
    }

}