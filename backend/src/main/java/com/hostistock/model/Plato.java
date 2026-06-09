package com.hostistock.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "plato",
       uniqueConstraints = @UniqueConstraint(
           name = "uk_plato_bar_nombre",
           columnNames = {"bar_id", "nombre"}
       ))
public class Plato {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bar_id", nullable = false)
    private Bar bar;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(length = 50)
    private String categoria;

    @OneToMany(mappedBy = "plato", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PlatoIngrediente> receta = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Bar getBar() { return bar; }
    public void setBar(Bar bar) { this.bar = bar; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public List<PlatoIngrediente> getReceta() { return receta; }
    public void setReceta(List<PlatoIngrediente> receta) { this.receta = receta; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}