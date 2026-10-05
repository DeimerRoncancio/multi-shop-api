# Multi Shop API

Backend de Multi Shop dividido en microservicios con Spring Boot 3.5 y Java 17. La tienda (`ecommerce-multi-shop`) y el panel (`control-panel-multi-shop`) solo hablan con el **gateway** en `http://localhost:8080`.

## Servicios

| Servicio | Puerto | Atiende | Base de datos |
|---|---|---|---|
| `api-gateway` | 8080 | Puerta de entrada: reparte las rutas y responde CORS | — |
| `identity-service` | 8084 | `/login`, `/app/users/**`, `/app/roles/**`, `/app/quantity` | `multishop_identity` |
| `catalog-service` | 8083 | `/app/products/**`, `/app/categories/**`, `/app/variants/**` | `multishop_catalog` |
| `transactions-service` | 8085 | `/app/payments/**` (compras, clientes y Stripe) | `multishop_transactions` |
| `media-service` | 8082 | Uso interno: subir y borrar fotos en Cloudinary | `multishop_media` |
| `db` (MySQL 8.0) | 3307 | Las cuatro bases | — |

Solo el gateway es accesible desde otros equipos de la red. Los demás puertos solo escuchan en `127.0.0.1`.

Los servicios se llaman entre sí con OpenFeign:

```
transactions → catalog   (precio y nombre de los productos)
transactions → identity  (datos del cliente con sesión)
identity     → catalog   (conteos de /app/quantity)
identity     → media     (foto de perfil)
catalog      → media     (fotos de productos)
```

## Requisitos

- Docker Desktop
- Java 17, solo para ejecutar los tests o un servicio fuera de Docker

## Configuración

Copia `.env.example` a `.env` y rellena los valores:

| Variable | Qué es | La usa |
|---|---|---|
| `JWT_PRIVATE_KEY` | Clave privada RSA para firmar los tokens (PKCS#8 en Base64) | identity |
| `JWT_PUBLIC_KEY` | Clave pública RSA para comprobar los tokens (X.509 en Base64) | identity, catalog, transactions |
| `STRIPE_SECRET_KEY`, `STRIPE_PUBLIC_KEY` | Claves de Stripe (`sk_test_…`, `pk_test_…` en desarrollo) | transactions |
| `STRIPE_WEBHOOK_SECRET` | Secreto del webhook (`whsec_…`) | transactions |
| `CLOUDINARY_CLOUD_NAME`, `CLOUDINARY_API_KEY`, `CLOUDINARY_API_SECRET` | Credenciales de Cloudinary | media |

`.env` está en `.gitignore`: nunca lo subas.

Para generar las claves JWT:

```bash
openssl genpkey -algorithm RSA -pkeyopt rsa_keygen_bits:2048 -out private.pem
openssl pkcs8 -topk8 -nocrypt -in private.pem -outform DER | base64 -w0   # JWT_PRIVATE_KEY
openssl pkey -in private.pem -pubout -outform DER | base64 -w0            # JWT_PUBLIC_KEY
rm private.pem
```

## Levantar todo

```bash
docker compose up -d --build
```

La primera vez tarda unos minutos porque descarga las dependencias de Maven. Para ver los logs de un servicio:

```bash
docker compose logs -f transactions
```

## Tests

Desde la raíz se prueban los cinco servicios. Los tests de integración usan Testcontainers, así que Docker tiene que estar encendido:

```bash
./mvnw test
```

Un solo servicio:

```bash
./mvnw -f catalog-service/pom.xml test
```

Cada servicio guarda en `src/test/resources/contracts` las respuestas JSON que recibe la tienda. Si un cambio altera alguna, su test falla.

## Pagos de prueba con Stripe

Para que los avisos de Stripe lleguen a tu equipo:

```bash
stripe listen --forward-to localhost:8080/app/payments/webhook
```

El `whsec_…` que muestra al arrancar debe coincidir con `STRIPE_WEBHOOK_SECRET`. Si lo cambias, recrea el servicio con `docker compose up -d transactions`.

## Carpeta `docker/mysql`

| Carpeta | Cuándo se usa |
|---|---|
| `init/` | La ejecuta MySQL sola la primera vez que arranca con el volumen vacío: crea las bases, sus usuarios y los roles `ROLE_USER` y `ROLE_ADMIN` |
| `migrations/` | Solo para una base que ya tiene datos del monolito anterior. Se ejecutan a mano y en orden, del `001` al `005` |

## Documentación de la API

Con todo levantado: `http://localhost:8080/swagger-ui.html` (rutas de transactions-service).
