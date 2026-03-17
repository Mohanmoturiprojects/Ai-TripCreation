package com.example.logikal.Entity;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
@Data
@Entity

public class Vehicle {
	  @Id
	    @GeneratedValue(strategy = GenerationType.IDENTITY)
	    @Column(name = "vehicleid")
	    private Long id;

	    
	    private String vehicleNumber;

	    @Column(name = "driver_name")
	    private String driverName;

	    
	    private String licenseNumber;

	   
	    private LocalDate licenseExpiryDate;

	  
	    private String status;
	    private double averageSpeed;


}
