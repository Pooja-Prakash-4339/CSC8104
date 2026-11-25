package org.acme.hotel;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

@Entity
public class TravelAgentBooking {

    @Id
    @GeneratedValue
    public Long id;

    public String customerEmail;
    public Long taxiBookingId;
    public Long flightBookingId;
    public Long hotelBookingId;
}