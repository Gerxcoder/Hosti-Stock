# Hosti-Stock

Sistema de gestión de stock para bares. Controla ingredientes, registra consumos y genera predicciones de demanda mediante Machine Learning.

## Stack

| Capa | Tecnología |
|---|---|
| Backend API | Spring Boot 3.5 · Java 21 · MySQL 8.4 · Flyway |
| IA / ML | Python 3.12 · FastAPI · scikit-learn (RandomForest) |
| Cliente | JavaFX 21 |
| Infraestructura | Docker Compose |

## Requisitos

- [Docker](https://docs.docker.com/get-docker/) y Docker Compose (recomendado)
- O bien: Java 21, Python 3.12 y MySQL 8.4 instalados localmente

## Arrancar con Docker

```bash
cp .env.example .env
# Editar .env con tus contraseñas y secreto JWT
docker compose up --build
```

Servicios disponibles tras el arranque:

| Servicio | URL |
|---|---|
| Backend API | http://localhost:8080 |
| IA (FastAPI) | http://localhost:8000 |
| MySQL | localhost:3306 |

## Arrancar en local (sin Docker)

### 1. Base de datos

Asegúrate de tener MySQL corriendo en `localhost:3306` con usuario `root` y contraseña vacía (perfil `dev` por defecto). La base de datos `hostistock` se crea automáticamente en el primer arranque vía Flyway.

### 2. Microservicio IA

```bash
cd ia
python -m venv .venv
.venv\Scripts\activate      # Windows
# source .venv/bin/activate # Linux/Mac
pip install -r requirements.txt
uvicorn app:app --port 8000
```

### 3. Backend

```bash
cd backend
./mvnw spring-boot:run
```

### 4. Cliente de escritorio

```bash
cd desktop
./mvnw javafx:run
```

## Estructura

```
hosti-stock/
├── backend/      # API REST (Spring Boot)
├── ia/           # Microservicio ML (FastAPI)
├── desktop/      # Cliente de escritorio (JavaFX)
└── docker-compose.yml
```

## Variables de entorno

Copia `.env.example` a `.env` y rellena los valores.

| Variable | Descripción |
|---|---|
| `MYSQL_ROOT_PASSWORD` | Contraseña root de MySQL |
| `MYSQL_PASSWORD` | Contraseña del usuario `hostistock` |
| `APP_JWT_SECRETO` | Secreto JWT (mínimo 32 caracteres) |
| `APP_IA_URL` | URL del microservicio IA (por defecto `http://ia:8000`) |

Genera un secreto seguro con:
```bash
openssl rand -base64 32
```
