package org.acme.hotel;

import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;

@Path("/bookings")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class BookingResource {

    @Inject
    EntityManager em;

    @Inject
    BookingService bookingService;

    @GET
    public List<Booking> list(@QueryParam("customerId") Long customerId) {
        if (customerId == null) {
            return em.createQuery("select b from Booking b order by b.id", Booking.class).getResultList();
        }
        return em.createQuery("select b from Booking b where b.customer.id = :id order by b.id", Booking.class)
                .setParameter("id", customerId)
                .getResultList();
    }

    @GET
    @Path("/{id}")
    public Booking get(@PathParam("id") Long id) {
        Booking b = em.find(Booking.class, id);
        if (b == null) {
            throw new NotFoundException("Booking " + id + " not found");
        }
        return b;
    }

    @POST
    @Transactional
    public Response create(@Valid BookingRequest request) {
        Customer customer = em.find(Customer.class, request.customerId);
        if (customer == null) {
            throw new NotFoundException("Customer " + request.customerId + " not found");
        }
        Booking booking = bookingService.create(customer, request.hotelId, request.bookingDate);
        return Response.status(Response.Status.CREATED).entity(booking).build();
    }

    // Cancel a booking
    @DELETE
    @Path("/{id}")
    @Transactional
    public Response cancel(@PathParam("id") Long id) {
        Booking b = em.find(Booking.class, id);
        if (b == null) {
            throw new NotFoundException("Booking " + id + " not found");
        }
        em.remove(b);
        return Response.noContent().build();
    }
}