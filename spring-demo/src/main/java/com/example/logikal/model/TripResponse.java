package com.example.logikal.model;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TripResponse {

	private Long id;
	
	   
    private String startLocation;

    private String destination;

    private Long vehicleId;
    
    private String VehicleNumber;
    
    private LocalDateTime startTime;

    private LocalDateTime expectedArrivalTime;
    
    private String drivername;


	
}
