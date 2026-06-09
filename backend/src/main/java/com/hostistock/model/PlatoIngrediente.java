package com.hostistock.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "plato_ingrediente",
       uniqueConstraints = @UniqueConstraint(
           name = "uk_plato_ingrediente",
           columnNames = {"plato_id", "ingrediente_id"}
       ))
public class PlatoIngrediente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plato_id", nullable = false)
    private Plato plato;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ingrediente_id", nullable = false)
    private Ingrediente ingrediente;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal cantidad;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Plato getPlato() { return plato; }
    public void setPlato(Plato plato) { this.plato = plato; }

    public Ingrediente getIngrediente() { return ingrediente; }
    public void setIngrediente(Ingrediente ingrediente) { this.ingrediente = ingrediente; }

    public BigDecimal getCantidad() { return cantidad; }
    public void setCantidad(BigDecimal cantidad) { this.cantidad = cantidad; }
}