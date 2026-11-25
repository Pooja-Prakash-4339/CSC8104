package org.acme.hotel;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.ws.rs.WebApplicationException;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@QuarkusTest
class TravelAgentTest {

    @InjectMock @RestClient TaxiClient taxi;
    @InjectMock @RestClient FlightClient flight;
    @InjectMock @RestClient HotelClient hotel;

    private static final String BODY =
            "{\"customer\":{\"name\":\"Ann\",\"email\":\"ta@example.com\",\"phoneNumber\":\"0123456789\"},"
            + "\"taxiId\":1,\"flightId\":1,\"hotelId\":1,\"bookingDate\":\"2030-01-15\"}";

    private RemoteBooking remote(long id) {
        RemoteBooking r = new RemoteBooking();
        r.id = id;
        return r;
    }

    @Test
    void allThreeServicesSucceed() {
        when(taxi.guestBooking(any())).thenReturn(remote(11));
        when(flight.guestBooking(any())).thenReturn(remote(22));
        when(hotel.guestBooking(any())).thenReturn(remote(33));

        given().contentType(ContentType.JSON).body(BODY)
                .post("/travel-agent/bookings")
                .then().statusCode(201);

        given().queryParam("email", "ta@example.com")
                .get("/travel-agent/bookings")
                .then().statusCode(200);
    }

    @Test
    void hotelFailureCancelsTaxiAndFlight() {
        when(taxi.guestBooking(any())).thenReturn(remote(11));
        when(flight.guestBooking(any())).thenReturn(remote(22));
        when(hotel.guestBooking(any())).thenThrow(new WebApplicationException(503));

        given().contentType(ContentType.JSON).body(BODY)
                .post("/travel-agent/bookings")
                .then().statusCode(502);

        verify(taxi).cancel(11L);
        verify(flight).cancel(22L);
    }

    @Test
    void firstServiceDownMeansNothingToCancel() {
        when(taxi.guestBooking(any())).thenThrow(new RuntimeException("connection refused"));

        given().contentType(ContentType.JSON).body(BODY)
                .post("/travel-agent/bookings")
                .then().statusCode(502);

        verifyNoInteractions(flight, hotel);
    }

    @Test
    void invalidRequestIsRejected() {
        given().contentType(ContentType.JSON).body("{\"taxiId\":1}")
                .post("/travel-agent/bookings")
                .then().statusCode(400);
    }
}