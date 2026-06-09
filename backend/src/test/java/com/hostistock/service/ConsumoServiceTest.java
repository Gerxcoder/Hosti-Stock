package com.hostistock.service;

import com.hostistock.dto.*;
import com.hostistock.model.TipoUnidad;
import com.hostistock.exception.StockInsuficienteException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ConsumoServiceTest {

    @Autowired
    private AutenticacionService autenticacionService;

    @Autowired
    private IngredienteService ingredienteService;

    @Autowired
    private PlatoService platoService;

    @Autowired
    private ConsumoService consumoService;

    private Long barId;

    @BeforeEach
    void setUp() {
        // Crear bar
        LoginResponse login = autenticacionService.registrar(
            new RegistroBarRequest("Bar Consumo Test", "consumo@test.com", "clave123"));
        this.barId = login.barId();

        // Crear ingredientes
        ingredienteService.crear(barId, new IngredienteRequest(
            "Patata", TipoUnidad.GRAMOS, new BigDecimal("1000"), new BigDecimal("200")));
        ingredienteService.crear(barId, new IngredienteRequest(
            "Huevo", TipoUnidad.UNIDADES, new BigDecimal("12"), new BigDecimal("4")));

        // Crear plato con receta: Tortilla = 200g patata + 3 huevos
        PlatoRequest platoReq = new PlatoRequest(
            "Tortilla", "tapa",
            List.of(
                new RecetaItemRequest(1L, new BigDecimal("200")),
                new RecetaItemRequest(2L, new BigDecimal("3"))
            ));
        platoService.crear(barId, platoReq);
    }

    @Test
    void registrarConsumo_conStockSuficiente_debeDescontarStock() {
        // Registrar consumo de 2 tortillas
        ConsumoRequest request = new ConsumoRequest(1L, 2);
        List<MovimientoStockResponse> movimientos =
            consumoService.registrarConsumo(barId, request);

        // Debe generar 2 movimientos (uno por ingrediente)
        assertEquals(2, movimientos.size());

        // Verificar stock actualizado
        IngredienteResponse patata = ingredienteService.obtenerPorId(barId, 1L);
        IngredienteResponse huevo = ingredienteService.obtenerPorId(barId, 2L);

        // Patata: 1000 - (200 * 2) = 600
        assertEquals(0, new BigDecimal("600.00").compareTo(patata.stockActual()));
        // Huevo: 12 - (3 * 2) = 6
        assertEquals(0, new BigDecimal("6.00").compareTo(huevo.stockActual()));
    }

    @Test
    void registrarConsumo_sinStockSuficiente_debeLanzarExcepcion() {
        // Intentar consumir 5 tortillas (necesita 15 huevos, solo hay 12)
        ConsumoRequest request = new ConsumoRequest(1L, 5);

        assertThrows(StockInsuficienteException.class, () ->
            consumoService.registrarConsumo(barId, request));

        // Verificar que el stock NO cambió (rollback)
        IngredienteResponse patata = ingredienteService.obtenerPorId(barId, 1L);
        assertEquals(0, new BigDecimal("1000.00").compareTo(patata.stockActual()));
    }

    @Test
    void exportarCsv_conConsumos_debeGenerarCsvConDatos() {
        // Registrar un consumo
        consumoService.registrarConsumo(barId, new ConsumoRequest(1L, 1));

        // Exportar CSV
        String csv = consumoService.exportarCsv(barId);

        // Verificar formato
        assertTrue(csv.startsWith("fecha,ingrediente_id,nombre_ingrediente,cantidad"));
        assertTrue(csv.contains("Patata"));
        assertTrue(csv.contains("Huevo"));
    }
}
