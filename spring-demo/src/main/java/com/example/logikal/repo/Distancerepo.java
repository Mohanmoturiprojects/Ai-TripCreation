package com.example.logikal.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.logikal.Entity.WhDistance;

@Repository
public interface Distancerepo extends JpaRepository<WhDistance, Long>{

	

	WhDistance findByStartLocationAndDestination(String startLocation, String destination);

}
	

