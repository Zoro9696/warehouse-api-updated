//package com.fulfilment.application.monolith.warehouses.adapters.restapi;
//
//import com.fulfilment.application.monolith.warehouses.adapters.database.WarehouseRepository;
//import com.fulfilment.application.monolith.warehouses.domain.usecases.CreateWarehouseUseCase;
//import com.warehouse.api.beans.Warehouse;
//import io.quarkus.test.InjectMock;
//import io.quarkus.test.junit.QuarkusTest;
//import io.restassured.http.ContentType;
//import jakarta.inject.Inject;
//import org.junit.jupiter.api.Test;
//
//import static io.restassured.RestAssured.given;
//import static org.hamcrest.Matchers.*;
//
//@QuarkusTest
//class WarehouseResourceTest {
//
//    @InjectMock
//    CreateWarehouseUseCase createUseCase;
//
//    @Inject
//    WarehouseRepository warehouseRepository;
//
//    @Test
//    void shouldCreateWarehouse() {
//
//        Warehouse warehouse = new Warehouse();
//        warehouse.setBusinessUnitCode("WH-1");
//        warehouse.setLocation("AMSTERDAM-001");
//        warehouse.setCapacity(50);
//        warehouse.setStock(10);
//
//        given()
//                .contentType(ContentType.JSON)
//                .body(warehouse)
//                .when()
//                .post("/warehouse")
//                .then()
//                .statusCode(anyOf(is(200), is(201)));
//    }
//
//    @Test
//    void shouldListWarehouses() {
//
//        given()
//                .when()
//                .get("/warehouse")
//                .then()
//                .statusCode(200);
//    }
//
//    @Test
//    void shouldGetWarehouseById() {
//
//        // create first
//        shouldCreateWarehouse();
//
//        given()
//                .when()
//                .get("/warehouse/WH-1")
//                .then()
//                .statusCode(200)
//                .body("businessUnitCode", equalTo("WH-1"));
//    }
//
//    @Test
//    void shouldReturn404WhenWarehouseMissing() {
//
//        given()
//                .when()
//                .get("/warehouse/UNKNOWN")
//                .then()
//                .statusCode(404);
//    }
//
//    @Test
//    void shouldArchiveWarehouse() {
//
//        shouldCreateWarehouse();
//
//        given()
//                .when()
//                .delete("/warehouse/WH-1")
//                .then()
//                .statusCode(anyOf(is(200), is(204)));
//    }
//
//    @Test
//    void shouldReplaceWarehouse() {
//
//        shouldCreateWarehouse();
//        Warehouse warehouse = new Warehouse();
//        warehouse.setLocation("UPDATED");
//        warehouse.setCapacity(100);
//        warehouse.setStock(20);
//
//        given()
//                .contentType(ContentType.JSON)
//                .body(warehouse)
//                .when()
//                .post("/warehouse/WH-1/replacement")
//                .then()
//                .statusCode(anyOf(is(200), is(204)));
//    }
//}

package com.fulfilment.application.monolith.warehouses.adapters.restapi;

import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@QuarkusTest
class WarehouseResourceTest {

    /**
     * Mock LocationResolver to always return valid location
     */
    @InjectMock
    LocationResolver locationResolver;

    private String testWarehouseCode;
    private String testLocation;

    @BeforeEach
    void setup() {
        // Generate unique warehouse code and location per test
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        testWarehouseCode = "WH-" + uuid;
        testLocation = "LOC-" + uuid; // Unique location per test to avoid warehouse limit

        // Mock LocationResolver to return location with high warehouse capacity
        when(locationResolver.resolveByIdentifier(anyString()))
                .thenReturn(new Location(testLocation, 100, 1000)); // Max 100 warehouses, 1000 capacity
    }

    /**
     * Helper to create warehouse with the test's unique business unit code
     */
    void createWarehouse() {
        com.warehouse.api.beans.Warehouse warehouse = new com.warehouse.api.beans.Warehouse();
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
                .statusCode(anyOf(is(200), is(201)));
    }

    @Test
    void shouldCreateWarehouse() {
        createWarehouse();
    }

    @Test
    void shouldCreateWarehouseWithMinimalFields() {
        com.warehouse.api.beans.Warehouse warehouse = new com.warehouse.api.beans.Warehouse();
        warehouse.setBusinessUnitCode("WH-MIN-" + UUID.randomUUID().toString().substring(0, 4));
        warehouse.setLocation(testLocation);
        warehouse.setCapacity(1);
        warehouse.setStock(0);

        given()
                .contentType(ContentType.JSON)
                .body(warehouse)
                .when()
                .post("/warehouse")
                .then()
                .statusCode(anyOf(is(200), is(201)));
    }

    @Test
    void shouldCreateWarehouseAtCapacityLimit() {
        com.warehouse.api.beans.Warehouse warehouse = new com.warehouse.api.beans.Warehouse();
        warehouse.setBusinessUnitCode("WH-CAP-" + UUID.randomUUID().toString().substring(0, 4));
        warehouse.setLocation(testLocation);
        warehouse.setCapacity(100);
        warehouse.setStock(100);

        given()
                .contentType(ContentType.JSON)
                .body(warehouse)
                .when()
                .post("/warehouse")
                .then()
                .statusCode(anyOf(is(200), is(201)));
    }

    @Test
    void shouldFailCreateWarehouseWithDuplicateBusinessUnitCode() {
        // Create first warehouse
        createWarehouse();

        // Try to create another with same code
        com.warehouse.api.beans.Warehouse duplicate = new com.warehouse.api.beans.Warehouse();
        duplicate.setBusinessUnitCode(testWarehouseCode);
        duplicate.setLocation(testLocation);
        duplicate.setCapacity(50);
        duplicate.setStock(10);

        given()
                .contentType(ContentType.JSON)
                .body(duplicate)
                .when()
                .post("/warehouse")
                .then()
                .statusCode(500);
    }

    @Test
    void shouldFailCreateWarehouseWithStockExceedingCapacity() {
        com.warehouse.api.beans.Warehouse warehouse = new com.warehouse.api.beans.Warehouse();
        warehouse.setBusinessUnitCode("WH-INVALID-STOCK");
        warehouse.setLocation(testLocation);
        warehouse.setCapacity(50);
        warehouse.setStock(100); // Exceeds capacity

        given()
                .contentType(ContentType.JSON)
                .body(warehouse)
                .when()
                .post("/warehouse")
                .then()
                .statusCode(500);
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
    void shouldListWarehousesEmpty() {
        given()
                .when()
                .get("/warehouse")
                .then()
                .statusCode(200);
    }

    @Test
    void shouldGetWarehouseById() {
        createWarehouse();

        given()
                .when()
                .get("/warehouse/" + testWarehouseCode)
                .then()
                .statusCode(200)
                .body("businessUnitCode", equalTo(testWarehouseCode));
    }

    @Test
    void shouldGetWarehouseByIdWithValidFields() {
        createWarehouse();

        given()
                .when()
                .get("/warehouse/" + testWarehouseCode)
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
                .get("/warehouse/NONEXISTENT-WH")
                .then()
                .statusCode(404);
    }


    @Test
    void shouldArchiveWarehouse() {
        createWarehouse();

        given()
                .when()
                .delete("/warehouse/" + testWarehouseCode)
                .then()
                .statusCode(anyOf(is(200), is(204)));
    }

    @Test
    void shouldArchiveWarehouseAndNotFindAfter() {
        createWarehouse();

        given()
                .when()
                .delete("/warehouse/" + testWarehouseCode)
                .then()
                .statusCode(anyOf(is(200), is(204)));

        // Verify it's gone
        given()
                .when()
                .get("/warehouse/" + testWarehouseCode)
                .then()
                .statusCode(404);
    }

    @Test
    void shouldFailArchiveNonexistentWarehouse() {
        given()
                .when()
                .delete("/warehouse/UNKNOWN-WH")
                .then()
                .statusCode(404); // Warehouse not found throws 404 WebApplicationException
    }

    @Test
    void shouldReplaceWarehouse() {
        createWarehouse();

        com.warehouse.api.beans.Warehouse updated = new com.warehouse.api.beans.Warehouse();
        updated.setLocation(testLocation);
        updated.setCapacity(75);
        updated.setStock(10);

        given()
                .contentType(ContentType.JSON)
                .body(updated)
                .when()
                .post("/warehouse/" + testWarehouseCode + "/replacement")
                .then()
                .statusCode(anyOf(is(200), is(204)));
    }

    @Test
    void shouldReplaceWarehouseWithUpdatedCapacity() {
        createWarehouse();

        com.warehouse.api.beans.Warehouse updated = new com.warehouse.api.beans.Warehouse();
        updated.setLocation(testLocation);
        updated.setCapacity(200);
        updated.setStock(10);

        given()
                .contentType(ContentType.JSON)
                .body(updated)
                .when()
                .post("/warehouse/" + testWarehouseCode + "/replacement")
                .then()
                .statusCode(anyOf(is(200), is(204)));
    }

    @Test
    void shouldFailReplaceWarehouseWithDifferentStock() {
        createWarehouse();

        com.warehouse.api.beans.Warehouse updated = new com.warehouse.api.beans.Warehouse();
        updated.setLocation(testLocation);
        updated.setCapacity(50);
        updated.setStock(20); // Different stock than original (10)

        given()
                .contentType(ContentType.JSON)
                .body(updated)
                .when()
                .post("/warehouse/" + testWarehouseCode + "/replacement")
                .then()
                .statusCode(422); // Stock mismatch validation failure (Unprocessable Entity)
    }

    @Test
    void shouldFailReplaceWarehouseWithInsufficientCapacity() {
        createWarehouse();

        com.warehouse.api.beans.Warehouse updated = new com.warehouse.api.beans.Warehouse();
        updated.setLocation(testLocation);
        updated.setCapacity(5); // Less than stock (10)
        updated.setStock(10);

        given()
                .contentType(ContentType.JSON)
                .body(updated)
                .when()
                .post("/warehouse/" + testWarehouseCode + "/replacement")
                .then()
                .statusCode(422); // Capacity too small validation failure (Unprocessable Entity)
    }

    @Test
    void shouldFailReplaceNonexistentWarehouse() {
        com.warehouse.api.beans.Warehouse updated = new com.warehouse.api.beans.Warehouse();
        updated.setLocation(testLocation);
        updated.setCapacity(50);
        updated.setStock(10);

        given()
                .contentType(ContentType.JSON)
                .body(updated)
                .when()
                .post("/warehouse/UNKNOWN-WH/replacement")
                .then()
                .statusCode(404); // Warehouse not found throws 404 WebApplicationException
    }

    @Test
    void shouldReplaceMultipleWarehouses() {
        // Create first warehouse
        String code1 = "WH-1-" + UUID.randomUUID().toString().substring(0, 4);
        com.warehouse.api.beans.Warehouse wh1 = new com.warehouse.api.beans.Warehouse();
        wh1.setBusinessUnitCode(code1);
        wh1.setLocation(testLocation);
        wh1.setCapacity(50);
        wh1.setStock(10);

        given()
                .contentType(ContentType.JSON)
                .body(wh1)
                .when()
                .post("/warehouse")
                .then()
                .statusCode(anyOf(is(200), is(201)));

        // Create second warehouse with different location to avoid warehouse limit
        String loc2 = "LOC2-" + UUID.randomUUID().toString().substring(0, 4);
        String code2 = "WH-2-" + UUID.randomUUID().toString().substring(0, 4);
        com.warehouse.api.beans.Warehouse wh2 = new com.warehouse.api.beans.Warehouse();
        wh2.setBusinessUnitCode(code2);
        wh2.setLocation(loc2);
        wh2.setCapacity(60);
        wh2.setStock(15);

        given()
                .contentType(ContentType.JSON)
                .body(wh2)
                .when()
                .post("/warehouse")
                .then()
                .statusCode(anyOf(is(200), is(201)));

        // Replace first
        com.warehouse.api.beans.Warehouse updated1 = new com.warehouse.api.beans.Warehouse();
        updated1.setLocation(testLocation);
        updated1.setCapacity(70);
        updated1.setStock(10);

        given()
                .contentType(ContentType.JSON)
                .body(updated1)
                .when()
                .post("/warehouse/" + code1 + "/replacement")
                .then()
                .statusCode(anyOf(is(200), is(204)));

        // Verify first still exists with new capacity
        given()
                .when()
                .get("/warehouse/" + code1)
                .then()
                .statusCode(200)
                .body("capacity", equalTo(70));

        // Verify second still exists unchanged
        given()
                .when()
                .get("/warehouse/" + code2)
                .then()
                .statusCode(200)
                .body("capacity", equalTo(60));
    }
}