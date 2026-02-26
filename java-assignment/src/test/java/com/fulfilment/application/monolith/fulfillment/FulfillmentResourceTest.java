package com.fulfilment.application.monolith.fulfillment;

import static org.junit.jupiter.api.Assertions.*;

import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;

import org.junit.jupiter.api.Test;

import com.fulfilment.application.monolith.products.Product;
import com.fulfilment.application.monolith.products.ProductRepository;
import com.fulfilment.application.monolith.stores.Store;

import java.util.List;

@QuarkusTest
class FulfillmentResourceTest {

    @Inject
    FulfillmentResource resource;

    @Inject
    ProductRepository productRepository;

    @Inject
    FulfillmentRepository fulfillmentRepository;

    @Test
    void testCreateAssociation_InvalidProductId() {

        FulfillmentAssociationRequest request = new FulfillmentAssociationRequest();
        request.productId = -1L;
        request.storeId = 1L;
        request.warehouseBusinessUnitCode = "WH001";

        assertThrows(WebApplicationException.class, () ->
                resource.createAssociation(request));
    }

    @Test
    void testCreateAssociation_InvalidWarehouseCode() {

        FulfillmentAssociationRequest request = new FulfillmentAssociationRequest();
        request.productId = 1L;
        request.storeId = 1L;
        request.warehouseBusinessUnitCode = "";

        assertThrows(WebApplicationException.class, () ->
                resource.createAssociation(request));
    }

    @Test
    void testCreateAssociation_InvalidStoreId() {

        FulfillmentAssociationRequest request = new FulfillmentAssociationRequest();
        request.productId = 1L;
        request.storeId = -1L;
        request.warehouseBusinessUnitCode = "WH001";

        assertThrows(WebApplicationException.class, () ->
                resource.createAssociation(request));
    }

    @Test
    void testCreateAssociation_ProductNotFound() {

        FulfillmentAssociationRequest request = new FulfillmentAssociationRequest();
        request.productId = 9999L;
        request.storeId = 1L;
        request.warehouseBusinessUnitCode = "WH001";

        assertThrows(WebApplicationException.class, () ->
                resource.createAssociation(request));
    }

    @Test
    void testGetAssociations_InvalidParams() {

        assertThrows(WebApplicationException.class, () ->
                resource.getAssociations(null, null));
    }

    @Test
    void testGetAssociations_ProductNotFound() {

        assertThrows(WebApplicationException.class, () ->
                resource.getAssociations(9999L, 1L));
    }

    @Test
    void testGetAssociations_Valid() {

        List<ProductWarehouseStoreAssociation> result =
                resource.getAssociations(1L, 1L);

        assertNotNull(result);
    }

    @Test
    void testGetStoreAssociations_NotFound() {

        assertThrows(WebApplicationException.class, () ->
                resource.getStoreAssociations(9999L));
    }

    @Test
    void testGetStoreAssociations_Valid() {

        List<ProductWarehouseStoreAssociation> result =
                resource.getStoreAssociations(1L);

        assertNotNull(result);
    }

    @Test
    void testGetWarehouseAssociations() {

        List<ProductWarehouseStoreAssociation> result =
                resource.getWarehouseAssociations("MWH.001");

        assertNotNull(result);
    }

    @Test
    @Transactional
    void testDeleteAssociation_NotFound() {

        assertThrows(WebApplicationException.class, () ->
                resource.deleteAssociation(9999L));
    }

    @Test
    @TestTransaction
    void testDeleteAssociation_Valid() {

        Product product = productRepository.findById(1L);
        Store store = Store.findById(1L);

        ProductWarehouseStoreAssociation association =
                new ProductWarehouseStoreAssociation();

        association.product = product;
        association.store = store;
        association.warehouseBusinessUnitCode = "DELETE_TEST";

        fulfillmentRepository.persist(association);

        resource.deleteAssociation(association.id);

        ProductWarehouseStoreAssociation deleted =
                fulfillmentRepository.findById(association.id);

        assertNull(deleted);
    }

    @Test
    void testGetConstraints() {

        FulfillmentConstraints constraints =
                resource.getConstraints();

        assertNotNull(constraints);
    }
}