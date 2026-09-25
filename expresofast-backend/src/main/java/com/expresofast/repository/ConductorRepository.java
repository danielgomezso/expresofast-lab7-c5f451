package com.expresofast.repository;

import com.expresofast.model.Conductor;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConductorRepository extends JpaRepository<Conductor, Integer> {
}