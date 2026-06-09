#!/usr/bin/env python3
# -*- coding: utf-8 -*-

import argparse
import json
import os
import sys
import warnings
from datetime import datetime, timedelta

import joblib
import pandas as pd

warnings.filterwarnings("ignore")


def cargar_ultimos_consumos(bar_id):
    ruta_csv = os.path.join(
        os.path.dirname(__file__), "data", f"consumos_bar_{bar_id}.csv"
    )

    if not os.path.exists(ruta_csv):
        return pd.DataFrame()

    df = pd.read_csv(ruta_csv, parse_dates=["fecha"])

    if df.empty:
        return df

    df_agg = (
        df.groupby(["fecha", "ingrediente_id", "nombre_ingrediente"])["cantidad"]
        .sum()
        .reset_index()
    )

    return df_agg


def predecir_ingrediente(modelo, ultimos_consumos, dias, fecha_inicio):
    ventana = list(ultimos_consumos.tail(7).values)

    while len(ventana) < 7:
        media = sum(ventana) / len(ventana) if ventana else 0
        ventana.insert(0, media)

    predicciones = []

    for i in range(dias):
        fecha = fecha_inicio + timedelta(days=i)

        features = {"dia_semana": fecha.weekday(), "mes": fecha.month}
        for j in range(1, 8):
            features[f"lag_{j}"] = ventana[-j]

        X = pd.DataFrame([features])
        consumo_previsto = max(0, modelo.predict(X)[0])

        predicciones.append((fecha.strftime("%Y-%m-%d"), round(consumo_previsto, 2)))
        ventana.append(consumo_previsto)

    return predicciones


def main():
    parser = argparse.ArgumentParser(
        description="Genera predicciones de consumo para Hosti-Stock"
    )
    parser.add_argument(
        "--bar_id", type=int, required=True, help="ID del bar"
    )
    parser.add_argument(
        "--dias", type=int, default=7, help="Número de días a predecir (default: 7)"
    )
    args = parser.parse_args()

    ruta_modelos = os.path.join(
        os.path.dirname(__file__), "modelos", f"bar_{args.bar_id}"
    )

    if not os.path.exists(ruta_modelos):
        print(
            json.dumps(
                [{"error": f"No hay modelos entrenados para el bar {args.bar_id}. "
                           "Ejecuta primero el entrenamiento."}]
            )
        )
        sys.exit(1)

    df_consumos = cargar_ultimos_consumos(args.bar_id)
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

        predicciones = predecir_ingrediente(modelo, ultimos, args.dias, fecha_inicio)

        # stock_estimado negativo acumulado — el backend lo ajusta con el stock real
        consumo_acumulado = 0
        for fecha, consumo in predicciones:
            consumo_acumulado += consumo
            resultados.append(
                {
                    "ingrediente_id": ing_id,
                    "fecha": fecha,
                    "consumo_previsto": consumo,
                    "stock_estimado": round(-consumo_acumulado, 2),
                    "recomendacion_compra": 0,
                }
            )

    print(json.dumps(resultados, ensure_ascii=False))


if __name__ == "__main__":
    main()
