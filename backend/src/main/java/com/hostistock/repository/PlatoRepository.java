package com.hostistock.repository;

import com.hostistock.model.Plato;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface PlatoRepository extends JpaRepository<Plato, Long> {
    List<Plato> findByBarId(Long barId);
    Optional<Plato> findByIdAndBarId(Long id, Long barId);
    boolean existsByNombreAndBarId(String nombre, Long barId);
}