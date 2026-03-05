package com.fulfilment.application.monolith.warehouses.domain.models;

import java.time.LocalDateTime;

public class Warehouse {

  public String businessUnitCode;
  public String location;
  public Integer capacity;
  public Integer stock;

  public LocalDateTime createdAt;
  public LocalDateTime archivedAt;
  public Long id;

    public Warehouse() {
    // required for mapping
  }

  public Warehouse(String businessUnitCode,
                   String location,
                   Integer capacity,
                   Integer stock,Long id) {

    validateBusinessUnitCode(businessUnitCode);
    validateCapacity(capacity);
    validateStock(stock, capacity);

    this.businessUnitCode = businessUnitCode;
    this.location = location;
    this.capacity = capacity;
    this.stock = stock;
    this.createdAt = LocalDateTime.now();
    this.id = id;
  }

  public void archive() {
    if (this.archivedAt != null) {
      throw new IllegalStateException("Warehouse already archived");
    }
    this.archivedAt = LocalDateTime.now();
  }

  private void validateBusinessUnitCode(String bu) {
    if (bu == null || bu.isBlank()) {
      throw new IllegalArgumentException("Business Unit Code cannot be null or blank");
    }
  }

  private void validateCapacity(Integer capacity) {
    if (capacity == null || capacity <= 0) {
      throw new IllegalArgumentException("Capacity must be greater than 0");
    }
  }

  private void validateStock(Integer stock, Integer capacity) {
    if (stock == null || stock < 0) {
      throw new IllegalArgumentException("Stock cannot be negative");
    }
    if (capacity != null && stock > capacity) {
      throw new IllegalArgumentException("Stock cannot exceed capacity");
    }
  }
}