package com.hostistock.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "prediccion")
public class Prediccion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bar_id", nullable = false)
    private Bar bar;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ingrediente_id", nullable = false)
    private Ingrediente ingrediente;

    @Column(name = "fecha_prediccion", nullable = false)
    private LocalDate fechaPrediccion;

    @Column(name = "consumo_previsto", nullable = false, precision = 12, scale = 2)
    private BigDecimal consumoPrevisto;

    @Column(name = "stock_estimado", precision = 12, scale = 2)
    private BigDecimal stockEstimado;

    @Column(name = "recomendacion_compra", nullable = false, precision = 12, scale = 2)
    private BigDecimal recomendacionCompra = BigDecimal.ZERO;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Bar getBar() { return bar; }
    public void setBar(Bar bar) { this.bar = bar; }

    public Ingrediente getIngrediente() { return ingrediente; }
    public void setIngrediente(Ingrediente ingrediente) { this.ingrediente = ingrediente; }

    public LocalDate getFechaPrediccion() { return fechaPrediccion; }
    public void setFechaPrediccion(LocalDate fechaPrediccion) { this.fechaPrediccion = fechaPrediccion; }

    public BigDecimal getConsumoPrevisto() { return consumoPrevisto; }
    public void setConsumoPrevisto(BigDecimal consumoPrevisto) { this.consumoPrevisto = consumoPrevisto; }

    public BigDecimal getStockEstimado() { return stockEstimado; }
    public void setStockEstimado(BigDecimal stockEstimado) { this.stockEstimado = stockEstimado; }

    public BigDecimal getRecomendacionCompra() { return recomendacionCompra; }
    public void setRecomendacionCompra(BigDecimal recomendacionCompra) { this.recomendacionCompra = recomendacionCompra; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}