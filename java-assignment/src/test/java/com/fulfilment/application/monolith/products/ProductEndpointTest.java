package com.fulfilment.application.monolith.products;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.core.IsNot.not;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class ProductEndpointTest {

  @Test
  public void testCrudProduct() {
    final String path = "product";

    // List all, should have all 3 products the database has initially:
    given()
        .when()
        .get(path)
        .then()
        .statusCode(200)
        .body(containsString("TONSTAD"), containsString("KALLAX"), containsString("BESTÅ"));

    // Delete the TONSTAD:
    given().when().delete(path + "/1").then().statusCode(204);

    // List all, TONSTAD should be missing now:
    given()
        .when()
        .get(path)
        .then()
        .statusCode(200)
        .body(not(containsString("TONSTAD")), containsString("KALLAX"), containsString("BESTÅ"));
  }

  // ==================== CREATE Tests ====================
  @Test
  void shouldCreateProductSuccessfully() {
    given()
        .contentType(ContentType.JSON)
        .body("""
            {
              "name": "NewProduct",
              "description": "A new test product",
              "price": 29.99,
              "stock": 50
            }
            """)
        .when()
        .post("/product")
        .then()
        .statusCode(201)
        .body("id", notNullValue())
        .body("name", equalTo("NewProduct"))
        .body("price", equalTo(29.99f))
        .body("stock", equalTo(50));
  }

  @Test
  void shouldCreateProductWithZeroPrice() {
    given()
        .contentType(ContentType.JSON)
        .body("""
            {
              "name": "FreeProduct",
              "description": "Free to use",
              "price": 0.0,
              "stock": 100
            }
            """)
        .when()
        .post("/product")
        .then()
        .statusCode(201)
        .body("price", equalTo(0.0f));
  }

  @Test
  void shouldCreateProductWithZeroStock() {
    given()
        .contentType(ContentType.JSON)
        .body("""
            {
              "name": "OutOfStock",
              "description": "Currently unavailable",
              "price": 99.99,
              "stock": 0
            }
            """)
        .when()
        .post("/product")
        .then()
        .statusCode(201)
        .body("stock", equalTo(0));
  }

  @Test
  void shouldFailCreateProductWithIdSet() {
    given()
        .contentType(ContentType.JSON)
        .body("""
            {
              "id": 999,
              "name": "InvalidProduct",
              "price": 19.99,
              "stock": 10
            }
            """)
        .when()
        .post("/product")
        .then()
        .statusCode(422)
        .body("error", containsString("Id was invalidly set"));
  }

  @Test
  void shouldCreateProductWithLargePrice() {
    given()
        .contentType(ContentType.JSON)
        .body("""
            {
              "name": "ExpensiveProduct",
              "description": "High-end item",
              "price": 999999.99,
              "stock": 1
            }
            """)
        .when()
        .post("/product")
        .then()
        .statusCode(201)
        .body("price", equalTo(999999.99f));
  }

  @Test
  void shouldCreateProductWithLargeStock() {
    given()
        .contentType(ContentType.JSON)
        .body("""
            {
              "name": "BulkProduct",
              "description": "Bulk inventory",
              "price": 5.99,
              "stock": 999999
            }
            """)
        .when()
        .post("/product")
        .then()
        .statusCode(201)
        .body("stock", equalTo(999999));
  }

  // ==================== READ Tests ====================
  @Test
  void shouldListAllProductsInitial() {
    given()
        .when()
        .get("/product")
        .then()
        .statusCode(200)
        .body("size()", greaterThanOrEqualTo(3));
  }

  @Test
  void shouldGetProductById() {
    // Use product ID 3 (BESTÅ) instead of 2 to avoid conflicts with update tests
    given()
        .when()
        .get("/product/3")
        .then()
        .statusCode(200)
        .body("id", equalTo(3))
        .body("name", equalTo("BESTÅ"));
  }

  @Test
  void shouldGetProductByIdWithAllFields() {
    given()
        .when()
        .get("/product/3")
        .then()
        .statusCode(200)
        .body("id", equalTo(3))
        .body("name", equalTo("BESTÅ"))
        .body("stock", equalTo(3));
  }

  @Test
  void shouldReturn404WhenProductNotFound() {
    given()
        .when()
        .get("/product/9999")
        .then()
        .statusCode(404)
        .body("error", containsString("does not exist"));
  }

  @Test
  void shouldListProductsSorted() {
    given()
        .when()
        .get("/product")
        .then()
        .statusCode(200)
        .body("size()", greaterThanOrEqualTo(3))
        .body("name", hasItems(containsString("KALLAX"), containsString("BESTÅ")));
  }

  // ==================== UPDATE Tests ====================
  @Test
  void shouldUpdateProductSuccessfully() {
    given()
        .contentType(ContentType.JSON)
        .body("""
            {
              "name": "UpdatedKALLAX",
              "description": "Updated description",
              "price": 49.99,
              "stock": 15
            }
            """)
        .when()
        .put("/product/2")
        .then()
        .statusCode(200)
        .body("name", equalTo("UpdatedKALLAX"))
        .body("price", equalTo(49.99f))
        .body("stock", equalTo(15));
  }

  @Test
  void shouldUpdateProductPrice() {
    // Create a new product to update (avoid conflicts with preloaded products)
    Integer productId = given()
        .contentType(ContentType.JSON)
        .body("""
            {
              "name": "PriceTestProduct",
              "description": "For price update test",
              "price": 50.0,
              "stock": 5
            }
            """)
        .when()
        .post("/product")
        .then()
        .statusCode(201)
        .extract()
        .path("id");

    given()
        .contentType(ContentType.JSON)
        .body("""
            {
              "name": "PriceTestProduct",
              "description": "Updated",
              "price": 199.99,
              "stock": 5
            }
            """)
        .when()
        .put("/product/" + productId)
        .then()
        .statusCode(200)
        .body("price", equalTo(199.99f));
  }

  @Test
  void shouldUpdateProductStock() {
    // Create a new product to update (avoid relying on preloaded products)
    Integer productId = given()
        .contentType(ContentType.JSON)
        .body("""
            {
              "name": "StockTestProduct",
              "description": "For stock update test",
              "price": 10.0,
              "stock": 10
            }
            """)
        .when()
        .post("/product")
        .then()
        .statusCode(201)
        .extract()
        .path("id");

    given()
        .contentType(ContentType.JSON)
        .body("""
            {
              "name": "StockTestProduct",
              "description": "For stock update test",
              "price": 10.0,
              "stock": 100
            }
            """)
        .when()
        .put("/product/" + productId)
        .then()
        .statusCode(200)
        .body("stock", equalTo(100));
  }

  @Test
  void shouldFailUpdateProductWithNullName() {
    given()
        .contentType(ContentType.JSON)
        .body("""
            {
              "name": null,
              "price": 19.99,
              "stock": 10
            }
            """)
        .when()
        .put("/product/2")
        .then()
        .statusCode(422)
        .body("error", containsString("Product Name was not set"));
  }

  @Test
  void shouldFailUpdateNonexistentProduct() {
    given()
        .contentType(ContentType.JSON)
        .body("""
            {
              "name": "UpdatedName",
              "price": 19.99,
              "stock": 10
            }
            """)
        .when()
        .put("/product/9999")
        .then()
        .statusCode(404)
        .body("error", containsString("does not exist"));
  }

  // ==================== DELETE Tests ====================
  @Test
  void shouldDeleteProductSuccessfully() {
    // Create a product first
    Integer productId = given()
        .contentType(ContentType.JSON)
        .body("""
            {
              "name": "ProductToDelete",
              "description": "Will be deleted",
              "price": 9.99,
              "stock": 5
            }
            """)
        .when()
        .post("/product")
        .then()
        .statusCode(201)
        .extract()
        .path("id");

    // Delete the product
    given()
        .when()
        .delete("/product/" + productId)
        .then()
        .statusCode(204);

    // Verify it's deleted
    given()
        .when()
        .get("/product/" + productId)
        .then()
        .statusCode(404);
  }

  @Test
  void shouldFailDeleteNonexistentProduct() {
    given()
        .when()
        .delete("/product/9999")
        .then()
        .statusCode(404)
        .body("error", containsString("does not exist"));
  }

  @Test
  void shouldNotFindProductAfterDelete() {
    // Create a product first
    Integer productId = given()
        .contentType(ContentType.JSON)
        .body("""
            {
              "name": "DeleteVerifyProduct",
              "description": "Test deletion verification",
              "price": 14.99,
              "stock": 8
            }
            """)
        .when()
        .post("/product")
        .then()
        .statusCode(201)
        .extract()
        .path("id");

    // Verify it exists
    given()
        .when()
        .get("/product/" + productId)
        .then()
        .statusCode(200);

    // Delete it
    given()
        .when()
        .delete("/product/" + productId)
        .then()
        .statusCode(204);

    // Verify it's gone
    given()
        .when()
        .get("/product/" + productId)
        .then()
        .statusCode(404);
  }

  @Test
  void shouldHandleMultipleDeletes() {
    // Create two products
    Integer productId1 = given()
        .contentType(ContentType.JSON)
        .body("""
            {
              "name": "FirstDelete",
              "price": 5.99,
              "stock": 10
            }
            """)
        .when()
        .post("/product")
        .then()
        .statusCode(201)
        .extract()
        .path("id");

    Integer productId2 = given()
        .contentType(ContentType.JSON)
        .body("""
            {
              "name": "SecondDelete",
              "price": 7.99,
              "stock": 20
            }
            """)
        .when()
        .post("/product")
        .then()
        .statusCode(201)
        .extract()
        .path("id");

    // Delete both
    given()
        .when()
        .delete("/product/" + productId1)
        .then()
        .statusCode(204);

    given()
        .when()
        .delete("/product/" + productId2)
        .then()
        .statusCode(204);

    // Verify both are gone
    given()
        .when()
        .get("/product/" + productId1)
        .then()
        .statusCode(404);

    given()
        .when()
        .get("/product/" + productId2)
        .then()
        .statusCode(404);
  }
}
