package com.hostistock.repository;

import com.hostistock.model.MovimientoStock;
import com.hostistock.model.TipoMovimiento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MovimientoStockRepository extends JpaRepository<MovimientoStock, Long> {
    List<MovimientoStock> findByBarIdOrderByCreatedAtDesc(Long barId);
    List<MovimientoStock> findByBarIdAndIngredienteIdOrderByCreatedAtDesc(Long barId, Long ingredienteId);
    List<MovimientoStock> findByBarIdAndTipoOrderByCreatedAtAsc(Long barId, TipoMovimiento tipo);
}