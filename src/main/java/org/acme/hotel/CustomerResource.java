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

@Path("/customers")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CustomerResource {

    @Inject
    EntityManager em;

    @GET
    public List<Customer> list() {
        return em.createQuery("select c from Customer c order by c.id", Customer.class).getResultList();
    }

    @GET
    @Path("/{id}")
    public Customer get(@PathParam("id") Long id) {
        Customer c = em.find(Customer.class, id);
        if (c == null) {
            throw new NotFoundException("Customer " + id + " not found");
        }
        return c;
    }

    @POST
    @Transactional
    public Response create(@Valid Customer customer) {
        customer.id = null;
        em.persist(customer);
        return Response.status(Response.Status.CREATED).entity(customer).build();
    }

    // Part 2
    @DELETE
    @Path("/{id}")
    @Transactional
    public Response delete(@PathParam("id") Long id) {
        Customer c = em.find(Customer.class, id);
        if (c == null) {
            throw new NotFoundException("Customer " + id + " not found");
        }
        em.remove(c); // cascades to bookings
        return Response.noContent().build();
    }
}