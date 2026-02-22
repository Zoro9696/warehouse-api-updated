package com.fulfilment.application.monolith.stores;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
public class StoreResourceTest {

    @BeforeEach
    @Transactional
    void cleanup() {
        Store.deleteAll();
    }

    @Transactional
    void persistStore(Store store) {
        store.persist();
    }

    @Test
    void shouldCreateStore() {
        given()
                .contentType(ContentType.JSON)
                .body("""
            {
              "name": "PuneStore",
              "quantityProductsInStock": 10
            }
            """)
                .when()
                .post("/store")
                .then()
                .statusCode(201)
                .body("id", notNullValue())
                .body("name", equalTo("PuneStore"))
                .body("quantityProductsInStock", equalTo(10));
    }

    @Test
    void shouldCreateStoreWithZeroQuantity() {
        given()
                .contentType(ContentType.JSON)
                .body("""
            {
              "name": "EmptyStore",
              "quantityProductsInStock": 0
            }
            """)
                .when()
                .post("/store")
                .then()
                .statusCode(201)
                .body("name", equalTo("EmptyStore"))
                .body("quantityProductsInStock", equalTo(0));
    }

    @Test
    void shouldCreateStoreWithLargeQuantity() {
        given()
                .contentType(ContentType.JSON)
                .body("""
            {
              "name": "LargeStore",
              "quantityProductsInStock": 999999
            }
            """)
                .when()
                .post("/store")
                .then()
                .statusCode(201)
                .body("name", equalTo("LargeStore"))
                .body("quantityProductsInStock", equalTo(999999));
    }

    @Test
    void shouldFailCreateStoreWithIdAlreadySet() {
        given()
                .contentType(ContentType.JSON)
                .body("""
            {
              "id": 999,
              "name": "InvalidStore",
              "quantityProductsInStock": 10
            }
            """)
                .when()
                .post("/store")
                .then()
                .statusCode(422)
                .body("error", containsString("Id was invalidly set"));
    }

    @Test
    void shouldGetAllStores() {
        Store store = new Store("StoreA");
        persistStore(store);
        Store store1 = new Store("StoreB");
        persistStore(store1);


        given()
                .when()
                .get("/store")
                .then()
                .statusCode(200)
                .body("$.size()", is(2));
    }

    @Test
    void shouldGetAllStoresEmpty() {
        given()
                .when()
                .get("/store")
                .then()
                .statusCode(200)
                .body("$.size()", is(0));
    }


    @Test
    void shouldGetSingleStore() {

        Store store = new Store("SingleStore");
        persistStore(store);
        given()
                .when()
                .get("/store/" + store.id)
                .then()
                .statusCode(200)
                .body("name", equalTo("SingleStore"));
    }

    @Test
    void shouldGetSingleStoreWithCorrectAttributes() {
        Store store = new Store("TestStore");
        store.quantityProductsInStock = 42;
        persistStore(store);

        given()
                .when()
                .get("/store/" + store.id)
                .then()
                .statusCode(200)
                .body("id", equalTo(store.id.intValue()))
                .body("name", equalTo("TestStore"))
                .body("quantityProductsInStock", equalTo(42));
    }

    @Test
    void shouldReturn404WhenStoreNotFound() {

        given()
                .when()
                .get("/store/9999")
                .then()
                .statusCode(404);
    }

    @Test
    void shouldUpdateStore() {

        Store store = new Store("OldName");
        store.quantityProductsInStock = 5;
        persistStore(store);
        given()
                .contentType(ContentType.JSON)
                .body("""
            {
              "name": "UpdatedStore",
              "quantityProductsInStock": 20
            }
            """)
                .when()
                .put("/store/" + store.id)
                .then()
                .statusCode(200)
                .body("name", equalTo("UpdatedStore"))
                .body("quantityProductsInStock", equalTo(20));
    }

    @Test
    void shouldFailUpdateStoreWithNullName() {
        Store store = new Store("ValidStore");
        persistStore(store);

        given()
                .contentType(ContentType.JSON)
                .body("""
            {
              "name": null,
              "quantityProductsInStock": 20
            }
            """)
                .when()
                .put("/store/" + store.id)
                .then()
                .statusCode(422)
                .body("error", containsString("Store Name was not set"));
    }

    @Test
    void shouldFailUpdateNonexistentStore() {
        given()
                .contentType(ContentType.JSON)
                .body("""
            {
              "name": "UpdatedName",
              "quantityProductsInStock": 10
            }
            """)
                .when()
                .put("/store/9999")
                .then()
                .statusCode(404)
                .body("error", containsString("does not exist"));
    }

    @Test
    void shouldUpdateStoreQuantity() {
        Store store = new Store("QuantityStore");
        store.quantityProductsInStock = 10;
        persistStore(store);

        given()
                .contentType(ContentType.JSON)
                .body("""
            {
              "name": "QuantityStore",
              "quantityProductsInStock": 50
            }
            """)
                .when()
                .put("/store/" + store.id)
                .then()
                .statusCode(200)
                .body("quantityProductsInStock", equalTo(50));
    }

    @Test
    void shouldPatchStore() {

        Store store = new Store("PatchStore");
        store.quantityProductsInStock = 15;
        persistStore(store);
        given()
                .contentType(ContentType.JSON)
                .body("""
            {
              "name": "PatchedName",
              "quantityProductsInStock": 0
            }
            """)
                .when()
                .patch("/store/" + store.id)
                .then()
                .statusCode(200)
                .body("name", equalTo("PatchedName"))
                .body("quantityProductsInStock", equalTo(0));
    }

    @Test
    void shouldFailPatchStoreWithNullName() {
        Store store = new Store("ValidStore");
        persistStore(store);

        given()
                .contentType(ContentType.JSON)
                .body("""
            {
              "name": null,
              "quantityProductsInStock": 20
            }
            """)
                .when()
                .patch("/store/" + store.id)
                .then()
                .statusCode(422)
                .body("error", containsString("Store Name was not set"));
    }

    @Test
    void shouldFailPatchNonexistentStore() {
        given()
                .contentType(ContentType.JSON)
                .body("""
            {
              "name": "PatchedName",
              "quantityProductsInStock": 10
            }
            """)
                .when()
                .patch("/store/9999")
                .then()
                .statusCode(404)
                .body("error", containsString("does not exist"));
    }

    @Test
    void shouldDeleteStore() {

        Store store = new Store("DeleteStore");
        persistStore(store);
        given()
                .when()
                .delete("/store/" + store.id)
                .then()
                .statusCode(204);

        given()
                .when()
                .get("/store/" + store.id)
                .then()
                .statusCode(404);
    }

    @Test
    void shouldDeleteStoreAndNotGetIt() {
        Store store = new Store("StoreToDelete");
        store.quantityProductsInStock = 30;
        persistStore(store);
        long storeId = store.id;

        given()
                .when()
                .delete("/store/" + storeId)
                .then()
                .statusCode(204);

        given()
                .when()
                .get("/store/" + storeId)
                .then()
                .statusCode(404);
    }

    @Test
    void shouldDeleteMultipleStores() {
        Store store1 = new Store("Store1");
        Store store2 = new Store("Store2");
        persistStore(store1);
        persistStore(store2);

        given()
                .when()
                .delete("/store/" + store1.id)
                .then()
                .statusCode(204);

        given()
                .when()
                .delete("/store/" + store2.id)
                .then()
                .statusCode(204);

        given()
                .when()
                .get("/store")
                .then()
                .statusCode(200)
                .body("$.size()", is(0));
    }

    @Test
    void shouldFailDeleteNonexistentStore() {
        given()
                .when()
                .delete("/store/9999")
                .then()
                .statusCode(404)
                .body("error", containsString("does not exist"));
    }
}
