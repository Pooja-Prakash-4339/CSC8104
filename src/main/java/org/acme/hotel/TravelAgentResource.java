package org.acme.hotel;

import io.quarkus.narayana.jta.QuarkusTransaction;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Path("/travel-agent/bookings")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TravelAgentResource {

    private static final Logger LOG = Logger.getLogger(TravelAgentResource.class);

    @Inject @RestClient TaxiClient taxi;
    @Inject @RestClient FlightClient flight;
    @Inject @RestClient HotelClient hotel;
    @Inject EntityManager em;

    @POST
    public Response bookAll(@Valid TravelAgentRequest req) {
        Map<String, Object> customer = Map.of(
                "name", req.customer.name,
                "email", req.customer.email,
                "phoneNumber", req.customer.phoneNumber == null ? "" : req.customer.phoneNumber);
        String date = req.bookingDate.toString();

        // compensating actions for everything that has succeeded so far
        List<Runnable> undo = new ArrayList<>();
        try {
            RemoteBooking t = taxi.guestBooking(
                    Map.of("customer", customer, "taxiId", req.taxiId, "bookingDate", date));
            undo.add(() -> taxi.cancel(t.id));

            RemoteBooking f = flight.guestBooking(
                    Map.of("customer", customer, "flightId", req.flightId, "bookingDate", date));
            undo.add(() -> flight.cancel(f.id));

            RemoteBooking h = hotel.guestBooking(
                    Map.of("customer", customer, "hotelId", req.hotelId, "bookingDate", date));
            undo.add(() -> hotel.cancel(h.id));

            TravelAgentBooking saved = new TravelAgentBooking();
            saved.customerEmail = req.customer.email;
            saved.taxiBookingId = t.id;
            saved.flightBookingId = f.id;
            saved.hotelBookingId = h.id;
            QuarkusTransaction.requiringNew().run(() -> em.persist(saved));

            return Response.status(Response.Status.CREATED).entity(saved).build();

        } catch (Exception e) {
            // a service failed -> cancel everything that succeeded (newest first)
            Collections.reverse(undo);
            int cancelled = 0;
            for (Runnable r : undo) {
                try {
                    r.run();
                    cancelled++;
                } catch (Exception ce) {
                    LOG.error("Compensating cancellation failed: " + ce.getMessage());
                }
            }
            boolean badRequest = e instanceof WebApplicationException w && w.getResponse().getStatus() < 500;
            int status = badRequest ? 400 : 502;
            return Response.status(status)
                    .entity(Map.of("error", "Combined booking failed: " + e.getMessage(),
                                   "cancelledBookings", cancelled))
                    .build();
        }
    }

    @GET
    public List<TravelAgentBooking> listByCustomer(@QueryParam("email") String email) {
        return em.createQuery(
                "select b from TravelAgentBooking b where b.customerEmail = :e order by b.id",
                TravelAgentBooking.class)
                .setParameter("e", email)
                .getResultList();
    }
}