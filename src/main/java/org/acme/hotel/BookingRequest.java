package org.acme.hotel;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public class BookingRequest {

    @NotNull(message = "customerId is required")
    public Long customerId;

    @NotNull(message = "hotelId is required")
    public Long hotelId;

    @NotNull(message = "bookingDate is required")
    @Future(message = "bookingDate must be in the future")
    public LocalDate bookingDate;
}