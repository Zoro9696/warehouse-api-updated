package com.fulfilment.application.monolith.warehouses.adapters.restapi;

import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import com.warehouse.api.beans.Warehouse;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@QuarkusTest
class WarehouseResourceTest {

    @InjectMock
    LocationResolver locationResolver;

    private String testWarehouseCode;
    private String testLocation;
    private String warehouseId;

    @BeforeEach
    void setup() {

        String uuid = UUID.randomUUID().toString().substring(0,8);

        testWarehouseCode = "WH-" + uuid;
        testLocation = "LOC-" + uuid;

        when(locationResolver.resolveByIdentifier(anyString()))
                .thenReturn(new Location(testLocation,100,1000));
    }

    void createWarehouse() {

        Warehouse warehouse = new Warehouse();
        warehouse.setBusinessUnitCode(testWarehouseCode);
        warehouse.setLocation(testLocation);
        warehouse.setCapacity(50);
        warehouse.setStock(10);

        given()
                .contentType(ContentType.JSON)
                .body(warehouse)
                .when()
                .post("/warehouse")
                .then()
                .statusCode(anyOf(is(200),is(201)));

        List<Warehouse> warehouses =
                given()
                        .when()
                        .get("/warehouse")
                        .then()
                        .statusCode(200)
                        .extract()
                        .body()
                        .jsonPath()
                        .getList(".", Warehouse.class);

        warehouseId = warehouses.stream()
                .filter(w -> testWarehouseCode.equals(w.getBusinessUnitCode()))
                .findFirst()
                .orElseThrow()
                .getId();
    }

    @Test
    void shouldCreateWarehouse() {
        createWarehouse();
    }

    @Test
    void shouldListWarehouses() {

        createWarehouse();

        given()
                .when()
                .get("/warehouse")
                .then()
                .statusCode(200)
                .body("size()", greaterThanOrEqualTo(0));
    }

    @Test
    void shouldGetWarehouseById() {

        createWarehouse();

        given()
                .when()
                .get("/warehouse/" + warehouseId)
                .then()
                .statusCode(200)
                .body("businessUnitCode", equalTo(testWarehouseCode));
    }

    @Test
    void shouldGetWarehouseByIdWithValidFields() {

        createWarehouse();

        given()
                .when()
                .get("/warehouse/" + warehouseId)
                .then()
                .statusCode(200)
                .body("businessUnitCode", equalTo(testWarehouseCode))
                .body("location", equalTo(testLocation))
                .body("capacity", equalTo(50))
                .body("stock", equalTo(10));
    }

    @Test
    void shouldReturn404WhenWarehouseMissing() {

        given()
                .when()
                .get("/warehouse/999999")
                .then()
                .statusCode(404);
    }

    @Test
    void shouldArchiveWarehouse() {

        createWarehouse();

        given()
                .when()
                .delete("/warehouse/" + warehouseId)
                .then()
                .statusCode(anyOf(is(200),is(204)));
    }

    @Test
    void shouldArchiveWarehouseAndNotFindAfter() {

        createWarehouse();

        given()
                .when()
                .delete("/warehouse/" + warehouseId)
                .then()
                .statusCode(anyOf(is(200),is(204)));

        given()
                .when()
                .get("/warehouse/" + warehouseId)
                .then()
                .statusCode(404);
    }

    @Test
    void shouldReplaceWarehouse() {

        createWarehouse();

        Warehouse updated = new Warehouse();
        updated.setLocation(testLocation);
        updated.setCapacity(75);
        updated.setStock(10);

        given()
                .contentType(ContentType.JSON)
                .body(updated)
                .when()
                .post("/warehouse/" + testWarehouseCode + "/replacement")
                .then()
                .statusCode(anyOf(is(200),is(204)));
    }

    @Test
    void shouldFailReplaceWarehouseWithDifferentStock() {

        createWarehouse();

        Warehouse updated = new Warehouse();
        updated.setLocation(testLocation);
        updated.setCapacity(50);
        updated.setStock(20);

        given()
                .contentType(ContentType.JSON)
                .body(updated)
                .when()
                .post("/warehouse/" + testWarehouseCode + "/replacement")
                .then()
                .statusCode(422);
    }

    @Test
    void shouldFailReplaceWarehouseWithInsufficientCapacity() {

        createWarehouse();

        Warehouse updated = new Warehouse();
        updated.setLocation(testLocation);
        updated.setCapacity(5);
        updated.setStock(10);

        given()
                .contentType(ContentType.JSON)
                .body(updated)
                .when()
                .post("/warehouse/" + testWarehouseCode + "/replacement")
                .then()
                .statusCode(422);
    }

    @Test
    void shouldFailReplaceNonexistentWarehouse() {

        Warehouse updated = new Warehouse();
        updated.setLocation(testLocation);
        updated.setCapacity(50);
        updated.setStock(10);

        given()
                .contentType(ContentType.JSON)
                .body(updated)
                .when()
                .post("/warehouse/UNKNOWN/replacement")
                .then()
                .statusCode(404);
    }
}