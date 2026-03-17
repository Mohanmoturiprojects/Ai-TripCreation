package com.example.logikal.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.logikal.Entity.Vehicle;

public interface Vehiclerepo extends JpaRepository<Vehicle, Long>{


	List<Vehicle> findByStatus(String string);

}
