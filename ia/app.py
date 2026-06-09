#!/usr/bin/env python3
# -*- coding: utf-8 -*-

import json
import os
import tempfile
from typing import Optional

from fastapi import FastAPI, HTTPException
from pydantic import BaseModel

# Reutilizar la lógica existente de los scripts
from entrenar import cargar_y_preparar_datos, entrenar_modelo
from predecir import cargar_ultimos_consumos, predecir_ingrediente

import joblib
import pandas as pd
from datetime import datetime, timedelta

app = FastAPI(title="Hosti-Stock IA", version="1.0.0")

DIRECTORIO_MODELOS = os.path.join(os.path.dirname(__file__), "modelos")
DIRECTORIO_DATA = os.path.join(os.path.dirname(__file__), "data")


# Modelos de request/response

class TrainRequest(BaseModel):
    bar_id: int
    csv_content: str  # Contenido CSV completo enviado por el backend


class PredictRequest(BaseModel):
    bar_id: int
    dias: Optional[int] = 7


# Endpoints

@app.get("/health")
def health():
    return {"status": "ok", "servicio": "hosti-stock-ia"}


@app.post("/train")
def train(req: TrainRequest):
    try:
        # Guardar CSV en disco (necesario para el flujo de entrenamiento)
        os.makedirs(DIRECTORIO_DATA, exist_ok=True)
        csv_path = os.path.join(DIRECTORIO_DATA, f"consumos_bar_{req.bar_id}.csv")
        with open(csv_path, "w", encoding="utf-8") as f:
            f.write(req.csv_content)

        df = cargar_y_preparar_datos(csv_path)

        if df.empty:
            return {
                "mensaje": "No hay datos de consumo para entrenar",
                "metricas": {},
            }

        metricas = entrenar_modelo(df, DIRECTORIO_MODELOS, req.bar_id)

        return {
            "mensaje": f"Entrenamiento completado para bar {req.bar_id}",
            "ingredientes_procesados": len(metricas),
            "metricas": metricas,
        }

    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


@app.post("/predict")
def predict(req: PredictRequest):
    ruta_modelos = os.path.join(DIRECTORIO_MODELOS, f"bar_{req.bar_id}")

    if not os.path.exists(ruta_modelos):
        raise HTTPException(
            status_code=404,
            detail=f"No hay modelos entrenados para el bar {req.bar_id}. "
                   "Ejecuta primero el entrenamiento.",
        )

    try:
        df_consumos = cargar_ultimos_consumos(req.bar_id)
        fecha_inicio = datetime.now().date() + timedelta(days=1)

        resultados = []

        for archivo in sorted(os.listdir(ruta_modelos)):
            if not archivo.endswith(".joblib"):
                continue

            ing_id = int(archivo.replace("ingrediente_", "").replace(".joblib", ""))
            modelo = joblib.load(os.path.join(ruta_modelos, archivo))

            if not df_consumos.empty:
                consumos_ing = df_consumos[df_consumos["ingrediente_id"] == ing_id]
                ultimos = consumos_ing.sort_values("fecha")["cantidad"]
            else:
                ultimos = pd.Series([0.0] * 7)

            predicciones = predecir_ingrediente(modelo, ultimos, req.dias, fecha_inicio)

            consumo_acumulado = 0
            for fecha, consumo in predicciones:
                consumo_acumulado += consumo
                resultados.append({
                    "ingrediente_id": ing_id,
                    "fecha": fecha,
                    "consumo_previsto": consumo,
                    "stock_estimado": round(-consumo_acumulado, 2),
                    "recomendacion_compra": 0,
                })

        return resultados

    except HTTPException:
        raise
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))
