package org.acme.hotel;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

@Entity
public class Booking {

    @Id
    @GeneratedValue
    public Long id;

    @ManyToOne(optional = false)
    public Customer customer;

    @ManyToOne(optional = false)
    public Hotel hotel;

    @NotNull
    public LocalDate bookingDate;
}