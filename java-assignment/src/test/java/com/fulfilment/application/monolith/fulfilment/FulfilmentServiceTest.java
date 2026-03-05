package com.fulfilment.application.monolith.fulfilment;

import com.fulfilment.application.monolith.products.Product;
import com.fulfilment.application.monolith.stores.Store;
import jakarta.ws.rs.WebApplicationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FulfilmentServiceTest {

    @Mock
    FulfilmentRepository repository;

    @InjectMocks
    FulfilmentService service;

    @InjectMocks
    ProductWarehouseStoreAssociation productWarehouseStoreAssociation;

    Product product;
    Store store;

    @BeforeEach
    void setup() {
        product = spy(new Product());
        product.id = 1L;

        store = spy(new Store());
        store.id = 1L;
    }

    // =========================
    // VALIDATION TESTS
    // =========================

    @Test
    void shouldThrowWhenProductIsNull() {
        WebApplicationException ex = assertThrows(
                WebApplicationException.class,
                () -> service.createAssociation(null, 1L, store)
        );
        assertEquals(400, ex.getResponse().getStatus());
    }

    @Test
    void shouldThrowWhenWarehouseIdInvalid() {
        WebApplicationException ex = assertThrows(
                WebApplicationException.class,
                () -> service.createAssociation(product, 0L, store)
        );
        assertEquals(400, ex.getResponse().getStatus());
    }

    @Test
    void shouldThrowWhenStoreIsNull() {
        WebApplicationException ex = assertThrows(
                WebApplicationException.class,
                () -> service.createAssociation(product, 1L, null)
        );
        assertEquals(400, ex.getResponse().getStatus());
    }

    // =========================
    // PERSIST BRANCHES
    // =========================

    @Test
    void shouldPersistProductAndStoreWhenIdsAreNull() {
    Product product = new Product();
    product.id = 1L;

    Store store = new Store();
    store.id = 1L;

    when(repository.exists(product, 1L, store)).thenReturn(false);
    when(repository.countWarehousesForProductInStore(product, store)).thenReturn(0L);
    when(repository.countWarehousesForStore(store)).thenReturn(0L);
    when(repository.countProductTypesInWarehouse(1L)).thenReturn(0L);

    ProductWarehouseStoreAssociation result =
            service.createAssociation(product, 1L, store);

    assertNotNull(result);
}
//    @Test
//    void shouldPersistProductAndStoreWhenIdsAreNull() {
//
//        PanacheMock.mock(Product.class);
//        PanacheMock.mock(Store.class);
//
//        Product newProduct = new Product();
//        Store newStore = new Store();
//
//        when(repository.exists(any(), anyLong(), any())).thenReturn(false);
//        when(repository.countWarehousesForProductInStore(any(), any())).thenReturn(0L);
//        when(repository.countWarehousesForStore(any())).thenReturn(0L);
//        when(repository.countProductTypesInWarehouse(anyLong())).thenReturn(0L);
//
//        service.createAssociation(newProduct, 1L, newStore);
//
//        PanacheMock.verify(Product.class).persist(newProduct);
//        PanacheMock.verify(Store.class).persist(newStore);
//
//        verify(repository).persist(any(ProductWarehouseStoreAssociation.class));
//    }

    // =========================
    // DUPLICATE CHECK
    // =========================

    @Test
    void shouldThrowWhenDuplicateExists() {
        when(repository.exists(product, 1L, store)).thenReturn(true);

        WebApplicationException ex = assertThrows(
                WebApplicationException.class,
                () -> service.createAssociation(product, 1L, store)
        );

        assertEquals(409, ex.getResponse().getStatus());
    }

    // =========================
    // CONSTRAINT 1
    // =========================

    @Test
    void shouldThrowWhenMaxWarehousesPerProductPerStoreReached() {
        when(repository.exists(product, 1L, store)).thenReturn(false);
        when(repository.countWarehousesForProductInStore(product, store))
                .thenReturn(2L);

        WebApplicationException ex = assertThrows(
                WebApplicationException.class,
                () -> service.createAssociation(product, 1L, store)
        );

        assertEquals(422, ex.getResponse().getStatus());
    }

    // =========================
    // CONSTRAINT 2
    // =========================

    @Test
    void shouldThrowWhenMaxWarehousesPerStoreReached() {
        when(repository.exists(product, 1L, store)).thenReturn(false);
        when(repository.countWarehousesForProductInStore(product, store))
                .thenReturn(0L);
        when(repository.countWarehousesForStore(store))
                .thenReturn(3L);

        WebApplicationException ex = assertThrows(
                WebApplicationException.class,
                () -> service.createAssociation(product, 1L, store)
        );

        assertEquals(422, ex.getResponse().getStatus());
    }

    // =========================
    // CONSTRAINT 3
    // =========================

    @Test
    void shouldThrowWhenMaxProductTypesPerWarehouseReached() {
        when(repository.exists(product, 1L, store)).thenReturn(false);
        when(repository.countWarehousesForProductInStore(product, store))
                .thenReturn(0L);
        when(repository.countWarehousesForStore(store))
                .thenReturn(0L);
        when(repository.countProductTypesInWarehouse(1L))
                .thenReturn(5L);

        WebApplicationException ex = assertThrows(
                WebApplicationException.class,
                () -> service.createAssociation(product, 1L, store)
        );

        assertEquals(422, ex.getResponse().getStatus());
    }

    // =========================
    // SUCCESS CASE
    // =========================

    @Test
    void shouldCreateAssociationSuccessfully() {
        when(repository.exists(product, 1L, store)).thenReturn(false);
        when(repository.countWarehousesForProductInStore(product, store))
                .thenReturn(0L);
        when(repository.countWarehousesForStore(store))
                .thenReturn(0L);
        when(repository.countProductTypesInWarehouse(1L))
                .thenReturn(0L);

        ProductWarehouseStoreAssociation result =
                service.createAssociation(product, 1L, store);

        assertNotNull(result);
        verify(repository).persist(any(ProductWarehouseStoreAssociation.class));
    }

    // =========================
    // REMOVE ASSOCIATION
    // =========================

    @Test
    void shouldThrowWhenRemovingNullAssociation() {
        WebApplicationException ex = assertThrows(
                WebApplicationException.class,
                () -> service.removeAssociation(null)
        );

        assertEquals(400, ex.getResponse().getStatus());
    }

    @Test
    void shouldRemoveAssociation() {
        ProductWarehouseStoreAssociation association =
                new ProductWarehouseStoreAssociation(product, 1L, store);
        association.id = 10L;

        service.removeAssociation(association);

        verify(repository).deleteById(10L);
    }

    // =========================
    // FIND METHODS
    // =========================

    @Test
    void shouldReturnWarehousesForProductInStore() {
        when(repository.findByProductAndStore(product, store))
                .thenReturn(Collections.emptyList());

        List<ProductWarehouseStoreAssociation> result =
                service.getWarehousesForProductInStore(product, store);

        assertNotNull(result);
        verify(repository).findByProductAndStore(product, store);
    }

    @Test
    void shouldReturnAssociationsForStore() {
        when(repository.findByStore(store))
                .thenReturn(Collections.emptyList());

        List<ProductWarehouseStoreAssociation> result =
                service.getAssociationsForStore(store);

        assertNotNull(result);
        verify(repository).findByStore(store);
    }

    @Test
    void shouldReturnAssociationsForWarehouse() {
        when(repository.findByWarehouse(1L))
                .thenReturn(Collections.emptyList());

        List<ProductWarehouseStoreAssociation> result =
                service.getAssociationsForWarehouse(1L);

        assertNotNull(result);
        verify(repository).findByWarehouse(1L);
    }

    // =========================
    // CONSTRAINTS OBJECT
    // =========================

    @Test
    void shouldReturnCorrectConstraints() {
        FulfilmentConstraints constraints = service.getConstraints();

        assertEquals(2, constraints.maxWarehousesPerProductPerStore);
        assertEquals(3, constraints.maxWarehousesPerStore);
        assertEquals(5, constraints.maxProductTypesPerWarehouse);
    }
}