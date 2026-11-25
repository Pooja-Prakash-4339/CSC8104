package org.acme.hotel;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.WebApplicationException;

import java.time.LocalDate;

@ApplicationScoped
public class BookingService {

    @Inject
    EntityManager em;

    public Booking create(Customer customer, Long hotelId, LocalDate date) {
        Hotel hotel = em.find(Hotel.class, hotelId);
        if (hotel == null) {
            throw new NotFoundException("Hotel " + hotelId + " not found");
        }

        long duplicates = em.createQuery(
                "select count(b) from Booking b where b.customer = :c and b.hotel = :h and b.bookingDate = :d",
                Long.class)
                .setParameter("c", customer)
                .setParameter("h", hotel)
                .setParameter("d", date)
                .getSingleResult();
        if (duplicates > 0) {
            throw new WebApplicationException("Booking already exists for this customer, hotel and date", 409);
        }

        Booking booking = new Booking();
        booking.customer = customer;
        booking.hotel = hotel;
        booking.bookingDate = date;
        em.persist(booking);
        return booking;
    }
}