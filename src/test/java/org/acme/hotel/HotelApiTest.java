package org.acme.hotel;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.get;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;

import org.junit.jupiter.api.Disabled;

@QuarkusTest
class HotelApiTest {

    private static final String DATE = "2030-01-15";

    // ---------- helpers ----------
    private long createCustomer(String email) {
        return given().contentType(ContentType.JSON)
                .body("{\"name\":\"Ann Lee\",\"email\":\"" + email + "\",\"phoneNumber\":\"0123456789\"}")
                .post("/customers")
                .then().statusCode(201)
                .extract().jsonPath().getLong("id");
    }

    private long createHotel() {
        return given().contentType(ContentType.JSON)
                .body("{\"name\":\"Grand\",\"city\":\"Newcastle\",\"pricePerNight\":80}")
                .post("/hotels")
                .then().statusCode(201)
                .extract().jsonPath().getLong("id");
    }

    private long createBooking(long customerId, long hotelId) {
        return given().contentType(ContentType.JSON)
                .body("{\"customerId\":" + customerId + ",\"hotelId\":" + hotelId + ",\"bookingDate\":\"" + DATE + "\"}")
                .post("/bookings")
                .then().statusCode(201)
                .extract().jsonPath().getLong("id");
    }

    // ---------- Part 1 ----------
    @Disabled
    @Test
    void createAndListCustomer() {
        createCustomer("list@example.com");
        get("/customers").then().statusCode(200).body("email", hasItem("list@example.com"));
    }

    @Disabled
    @Test
    void invalidCustomerIsRejected() {
        given().contentType(ContentType.JSON)
                .body("{\"name\":\"\",\"email\":\"not-an-email\",\"phoneNumber\":\"abc\"}")
                .post("/customers")
                .then().statusCode(400);
    }

    @Test
    void createAndListHotel() {
        createHotel();
        get("/hotels").then().statusCode(200).body("name", hasItem("Grand"));
    }

    @Test
    void invalidHotelIsRejected() {
        given().contentType(ContentType.JSON)
                .body("{\"name\":\"\",\"city\":\"\",\"pricePerNight\":0}")
                .post("/hotels")
                .then().statusCode(400);
    }

    @Test
    void createListAndCancelBooking() {
        long c = createCustomer("book@example.com");
        long h = createHotel();
        long b = createBooking(c, h);

        get("/bookings/" + b).then().statusCode(200);
        get("/bookings?customerId=" + c).then().statusCode(200).body("id", hasItem((int) b));

        given().delete("/bookings/" + b).then().statusCode(204);
        get("/bookings/" + b).then().statusCode(404);
    }

    @Test
    void bookingWithUnknownHotelReturns404() {
        long c = createCustomer("nohotel@example.com");
        given().contentType(ContentType.JSON)
                .body("{\"customerId\":" + c + ",\"hotelId\":999999,\"bookingDate\":\"" + DATE + "\"}")
                .post("/bookings")
                .then().statusCode(404);
    }

    @Test
    void bookingInThePastIsRejected() {
        long c = createCustomer("past@example.com");
        long h = createHotel();
        given().contentType(ContentType.JSON)
                .body("{\"customerId\":" + c + ",\"hotelId\":" + h + ",\"bookingDate\":\"2000-01-01\"}")
                .post("/bookings")
                .then().statusCode(400);
    }

    // ---------- Part 2 (add these after you finish Part 2) ----------
    @Test
    void deletingCustomerDeletesTheirBookings() {
        long c = createCustomer("cascade1@example.com");
        long h = createHotel();
        long b = createBooking(c, h);

        given().delete("/customers/" + c).then().statusCode(204);
        get("/bookings/" + b).then().statusCode(404);
    }

    @Test
    void deletingHotelDeletesItsBookings() {
        long c = createCustomer("cascade2@example.com");
        long h = createHotel();
        long b = createBooking(c, h);

        given().delete("/hotels/" + h).then().statusCode(204);
        get("/bookings/" + b).then().statusCode(404);
    }

    @Test
    void guestBookingCommitsCustomerAndBooking() {
        long h = createHotel();
        given().contentType(ContentType.JSON)
                .body("{\"customer\":{\"name\":\"Guest\",\"email\":\"guest-ok@example.com\",\"phoneNumber\":\"0123456789\"},"
                        + "\"hotelId\":" + h + ",\"bookingDate\":\"" + DATE + "\"}")
                .post("/guest-bookings")
                .then().statusCode(201);

        get("/customers").then().body("email", hasItem("guest-ok@example.com"));
    }

    @Test
    void guestBookingRollsBackCustomerWhenBookingFails() {
        given().contentType(ContentType.JSON)
                .body("{\"customer\":{\"name\":\"Ghost\",\"email\":\"ghost@example.com\",\"phoneNumber\":\"0123456789\"},"
                        + "\"hotelId\":999999,\"bookingDate\":\"" + DATE + "\"}")
                .post("/guest-bookings")
                .then().statusCode(404);

        // the customer must NOT have been saved
        get("/customers").then().body("email", not(hasItem("ghost@example.com")));
    }
}