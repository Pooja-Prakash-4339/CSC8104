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
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;

@Path("/hotels")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class HotelResource {

    @Inject
    EntityManager em;

    @GET
    public List<Hotel> list() {
        return em.createQuery("select h from Hotel h order by h.id", Hotel.class).getResultList();
    }

    @GET
    @Path("/{id}")
    public Hotel get(@PathParam("id") Long id) {
        Hotel h = em.find(Hotel.class, id);
        if (h == null) {
            throw new NotFoundException("Hotel " + id + " not found");
        }
        return h;
    }

    @POST
    @Transactional
    public Response create(@Valid Hotel hotel) {
        hotel.id = null;
        em.persist(hotel);
        return Response.status(Response.Status.CREATED).entity(hotel).build();
    }

    // Part 2
    @DELETE
    @Path("/{id}")
    @Transactional
    public Response delete(@PathParam("id") Long id) {
        Hotel h = em.find(Hotel.class, id);
        if (h == null) {
            throw new NotFoundException("Hotel " + id + " not found");
        }
        em.remove(h); // cascades to bookings
        return Response.noContent().build();
    }
}