package com.hostistock.repository;

import com.hostistock.model.Prediccion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface PrediccionRepository extends JpaRepository<Prediccion, Long> { 
    List<Prediccion> findByBarIdAndFechaPrediccionBetweenOrderByFechaPrediccionAsc(
        Long barId, LocalDate desde, LocalDate hasta);

    void deleteByBarId(Long barId);
}
