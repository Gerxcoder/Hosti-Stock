package com.hostistock.repository;

import com.hostistock.model.Ingrediente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface IngredienteRepository extends JpaRepository<Ingrediente, Long> {
    List<Ingrediente> findByBarId(Long barId);
    Optional<Ingrediente> findByIdAndBarId(Long id, Long barId);
    boolean existsByNombreAndBarId(String nombre, Long barId);
}