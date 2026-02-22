package com.fulfilment.application.monolith.location;

import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.smallrye.common.constraint.Assert.assertNotNull;
import static io.smallrye.common.constraint.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class LocationGatewayTest {

  private LocationGateway gateway;

  @BeforeEach
  void setup() {
    gateway = new LocationGateway();
  }

  @Test
  void testResolveByIdentifierPositive() {
    Location loc = gateway.resolveByIdentifier("ZWOLLE-001");
    assertNotNull(loc);
    assertEquals("ZWOLLE-001", loc.identification);
  }

  @Test
  void testResolveByIdentifierNotFound() {
    Exception exception = assertThrows(IllegalArgumentException.class, () ->
            gateway.resolveByIdentifier("UNKNOWN-001"));
    assertTrue(exception.getMessage().contains("Location not found"));
  }

  @Test
  void testResolveByIdentifierNull() {
    Exception exception = assertThrows(IllegalArgumentException.class, () ->
            gateway.resolveByIdentifier(null));
    assertTrue(exception.getMessage().contains("must not be null"));
  }

  @Test
  void testResolveByIdentifierEmpty() {
    Exception exception = assertThrows(IllegalArgumentException.class, () ->
            gateway.resolveByIdentifier(""));
    assertTrue(exception.getMessage().contains("must not be null"));
  }
}


