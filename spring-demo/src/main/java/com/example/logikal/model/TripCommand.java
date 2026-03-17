package com.example.logikal.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class TripCommand {
	
	 private String startlocation;

	    private String destination;

	    private Long vehicleId;

	    private String startTime;

	

}
