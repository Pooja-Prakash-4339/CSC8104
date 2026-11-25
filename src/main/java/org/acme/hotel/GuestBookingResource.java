package org.acme.hotel;

import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Status;
import jakarta.transaction.UserTransaction;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/guest-bookings")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class GuestBookingResource {

    @Inject
    UserTransaction utx;

    @Inject
    EntityManager em;

    @Inject
    BookingService bookingService;

    @POST
    public Response create(@Valid GuestBookingRequest request) {
        try {
            utx.begin();                                   // 1. start JTA transaction

            Customer customer = request.customer;          // 2. create Customer
            customer.id = null;
            em.persist(customer);
            em.flush();

            Booking booking = bookingService.create(       // 3. create Booking
                    customer, request.hotelId, request.bookingDate);
            em.flush();

            utx.commit();                                  // 4. both succeeded -> COMMIT
            return Response.status(Response.Status.CREATED).entity(booking).build();

        } catch (WebApplicationException e) {
            rollback();                                    // known failure (e.g. hotel not found)
            throw e;
        } catch (Exception e) {
            rollback();                                    // any other failure -> ROLLBACK
            throw new WebApplicationException("GuestBooking failed and was rolled back: " + e.getMessage(), 500);
        }
    }

    private void rollback() {
        try {
            if (utx.getStatus() != Status.STATUS_NO_TRANSACTION) {
                utx.rollback();
            }
        } catch (Exception ignored) {
            // nothing more we can do
        }
    }
}