package org.acme.hotel;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public class TravelAgentRequest {

    @NotNull
    @Valid
    public Customer customer;

    @NotNull
    public Long taxiId;

    @NotNull
    public Long flightId;

    @NotNull
    public Long hotelId;

    @NotNull
    @Future
    public LocalDate bookingDate;
}