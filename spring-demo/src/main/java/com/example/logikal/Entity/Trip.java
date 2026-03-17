package com.example.logikal.Entity;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

@Entity
@Data
public class Trip {
	  @Id
	    @GeneratedValue(strategy = GenerationType.IDENTITY)
	    private Long id;

	    private String startLocation;

	    private String destination;

	    private LocalDateTime startTime;

	    private LocalDateTime expectedArrival;

	    private double distance;

	    private Long vehicleId;


}
