package org.acme.hotel;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public class GuestBookingRequest {

    @NotNull(message = "customer is required")
    @Valid
    public Customer customer;

    @NotNull(message = "hotelId is required")
    public Long hotelId;

    @NotNull(message = "bookingDate is required")
    @Future(message = "bookingDate must be in the future")
    public LocalDate bookingDate;
}