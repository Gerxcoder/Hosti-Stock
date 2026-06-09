#!/usr/bin/env python3
# -*- coding: utf-8 -*-

import argparse
import json
import os
import sys
import warnings

import joblib
import pandas as pd
from sklearn.ensemble import RandomForestRegressor
from sklearn.metrics import mean_absolute_error, mean_squared_error

warnings.filterwarnings("ignore")


def cargar_y_preparar_datos(ruta_csv):
    df = pd.read_csv(ruta_csv, parse_dates=["fecha"])

    if df.empty:
        return df

    df_agg = (
        df.groupby(["fecha", "ingrediente_id", "nombre_ingrediente"])["cantidad"]
        .sum()
        .reset_index()
    )

    # Rellenar días sin consumo con 0
    fecha_min = df_agg["fecha"].min()
    fecha_max = df_agg["fecha"].max()
    fechas = pd.date_range(fecha_min, fecha_max, freq="D")

    ingredientes = df_agg[["ingrediente_id", "nombre_ingrediente"]].drop_duplicates()
    filas = []
    for _, ing in ingredientes.iterrows():
        for fecha in fechas:
            cantidad = df_agg[
                (df_agg["fecha"] == fecha)
                & (df_agg["ingrediente_id"] == ing["ingrediente_id"])
            ]["cantidad"].sum()
            filas.append(
                {
                    "fecha": fecha,
                    "ingrediente_id": ing["ingrediente_id"],
                    "nombre_ingrediente": ing["nombre_ingrediente"],
                    "cantidad": cantidad,
                }
            )

    return pd.DataFrame(filas)


def crear_features(df):
    df = df.copy()
    df["dia_semana"] = df["fecha"].dt.dayofweek
    df["mes"] = df["fecha"].dt.month

    for i in range(1, 8):
        df[f"lag_{i}"] = df["cantidad"].shift(i)

    df = df.dropna()

    return df


def entrenar_modelo(df, directorio_modelos, bar_id):
    ruta_modelos = os.path.join(directorio_modelos, f"bar_{bar_id}")
    os.makedirs(ruta_modelos, exist_ok=True)

    features_cols = ["dia_semana", "mes"] + [f"lag_{i}" for i in range(1, 8)]
    metricas = {}

    ingredientes = df["ingrediente_id"].unique()

    for ing_id in ingredientes:
        df_ing = df[df["ingrediente_id"] == ing_id].sort_values("fecha")
        nombre = df_ing["nombre_ingrediente"].iloc[0]

        df_feat = crear_features(df_ing)

        if len(df_feat) < 10:
            metricas[str(int(ing_id))] = {
                "nombre": nombre,
                "estado": "insuficientes_datos",
                "filas": len(df_feat),
            }
            continue

        X = df_feat[features_cols]
        y = df_feat["cantidad"]

        # Split temporal, no aleatorio — hay que respetar el orden cronológico
        punto_corte = int(len(X) * 0.8)
        X_train, X_test = X.iloc[:punto_corte], X.iloc[punto_corte:]
        y_train, y_test = y.iloc[:punto_corte], y.iloc[punto_corte:]

        modelo = RandomForestRegressor(
            n_estimators=100,
            max_depth=10,
            random_state=42,
            n_jobs=-1,
        )
        modelo.fit(X_train, y_train)

        y_pred = modelo.predict(X_test)
        mae = mean_absolute_error(y_test, y_pred)
        mse = mean_squared_error(y_test, y_pred)

        ruta_modelo = os.path.join(ruta_modelos, f"ingrediente_{int(ing_id)}.joblib")
        joblib.dump(modelo, ruta_modelo)

        metricas[str(int(ing_id))] = {
            "nombre": nombre,
            "estado": "entrenado",
            "mae": round(mae, 2),
            "mse": round(mse, 2),
            "filas_entrenamiento": len(X_train),
            "filas_test": len(X_test),
        }

    return metricas


def main():
    parser = argparse.ArgumentParser(
        description="Entrena modelos de predicción de consumo para Hosti-Stock"
    )
    parser.add_argument(
        "--bar_id", type=int, required=True, help="ID del bar para el que entrenar"
    )
    parser.add_argument(
        "--csv", type=str, required=True, help="Ruta al CSV de consumos"
    )
    args = parser.parse_args()

    if not os.path.exists(args.csv):
        print(
            json.dumps({"error": f"No se encontró el fichero CSV: {args.csv}"}),
            file=sys.stderr,
        )
        sys.exit(1)

    df = cargar_y_preparar_datos(args.csv)

    if df.empty:
        print(
            json.dumps(
                {
                    "mensaje": "No hay datos de consumo para entrenar",
                    "metricas": {},
                }
            )
        )
        sys.exit(0)

    directorio_modelos = os.path.join(os.path.dirname(__file__), "modelos")
    metricas = entrenar_modelo(df, directorio_modelos, args.bar_id)

    resultado = {
        "mensaje": f"Entrenamiento completado para bar {args.bar_id}",
        "ingredientes_procesados": len(metricas),
        "metricas": metricas,
    }
    print(json.dumps(resultado, ensure_ascii=False))


if __name__ == "__main__":
    main()
