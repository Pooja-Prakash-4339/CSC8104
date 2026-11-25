package org.acme.hotel;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

import java.util.ArrayList;
import java.util.List;

@Entity
public class Hotel {

    @Id
    @GeneratedValue
    public Long id;

    @NotBlank(message = "Hotel name is required")
    public String name;

    @NotBlank(message = "City is required")
    public String city;

    @Min(value = 1, message = "Price per night must be at least 1")
    public int pricePerNight;

    // Part 2: deleting a hotel also deletes its bookings
    @JsonIgnore
    @OneToMany(mappedBy = "hotel", cascade = CascadeType.REMOVE, orphanRemoval = true)
    public List<Booking> bookings = new ArrayList<>();
}