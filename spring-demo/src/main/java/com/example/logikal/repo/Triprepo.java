package com.example.logikal.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.logikal.Entity.Trip;

public interface Triprepo extends JpaRepository<Trip, Long>{

}
