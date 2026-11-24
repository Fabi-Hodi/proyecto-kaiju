# Kaiju · Gestión y trazabilidad de inventario

Aplicación Android desarrollada en **Kotlin + Jetpack Compose** para la empresa **Kaiju**, que reemplaza el control manual de inventario (planillas y conteos) por una app que mantiene el stock actualizado, registra cada movimiento con su historial y alerta los productos que hay que reponer.

> Proyecto académico · DSY1105 Desarrollo de Aplicaciones Móviles · Duoc UC · **Grupo 10**
> Todos los datos utilizados son **ficticios**, como exige el caso.

---

## 1. Nombre del proyecto
**Kaiju – App de inventario** (`AppKaiju_Grupo10`)
Identificador de la app (applicationId): `cl.duocuc.grupo10.kaiju.inventario`

## 2. Integrantes
| Integrante | GitHub |
|---|---|
| Alonso Valenzuela | [Alonso1-wiz](https://github.com/Alonso1-wiz) |
| Fabián muñoz | [Fabi-Hodi](https://github.com/Fabi-Hodi) |

- **Repositorio de la app:** https://github.com/Fabi-Hodi/proyecto-kaiju
- **Repositorio del microservicio:** https://github.com/SAFD666/SonidoVivo (rama `alonso/kaiju-auth`)
- **Tablero Trello:** https://trello.com/b/G53MGa8Z/kaiju

---

## 3. Funcionalidades

### Usuarios (conectado al microservicio Spring Boot)
- **Registro de operadores**: nombre, RUT (validado con módulo 11), email, contraseña y rol (Bodega / Ventas / Admin, solo informativo).
- **Inicio de sesión** contra el microservicio, que devuelve un **token JWT**.
- Los usuarios se guardan en **PostgreSQL (AWS RDS)**, en la tabla `usuario_kaiju`, y sirven desde cualquier dispositivo.
- Contraseñas protegidas con **BCrypt** en el servidor (nunca se guardan en texto plano).
- Mensajes de error claros: credenciales incorrectas, RUT o email repetido, sin conexión al servidor.

### Inventario (persistencia local con Room)
- **Inicio** con categorías (cantidad de productos por categoría) y buscador.
- **Inventario** con búsqueda en vivo, filtros por categoría y por stock bajo, alerta de reposición y etiqueta *"Reponer"*.
- **Agregar categoría** con prefijo sugerido automáticamente (ej: *Insumos → IN*).
- **Agregar producto** con **código SKU automático** por categoría (`IN-001`, `IN-002`…), generado en una transacción para que nunca se repita.
- **Movimientos de stock** (entrada, salida y ajuste) con validaciones, vista previa del stock resultante e **historial de trazabilidad** con fecha.
- **Reporte de reposición** y ficha de producto compartibles (WhatsApp, correo, etc.).

### API externa
- **Valor del dólar del día** (mindicador.cl) mostrado en el Inventario, con el valor total del inventario y el precio de cada producto en US$. Maneja los estados *cargando*, *error con reintento* y *listo*.

### Recursos nativos y experiencia de uso
- **Cámara y galería** para la foto de perfil (permisos en tiempo de ejecución + FileProvider).
- Perfil con los datos del operador que inició sesión.
- Diseño **adaptable** (celular y tablet), **modo oscuro**, animaciones y transiciones.

---

## 4. Endpoints utilizados

### API externa (pública)
| Método | URL | Uso en la app |
|---|---|---|
| GET | `https://mindicador.cl/api/dolar` | Valor del dólar observado (banner del Inventario y precios en US$) |

### Microservicio propio (Spring Boot)
URL base: `http://<IP_PUBLICA_EC2>:8080`

| Método | Endpoint | Body (JSON) | Respuesta |
|---|---|---|---|
| POST | `/api/v1/kaiju/auth/registro` | `{ "nombreCompleto", "rut", "email", "password", "rol" }` | **201** con los datos del usuario (sin contraseña) · **400** datos o RUT inválidos · **409** RUT o email repetido |
| POST | `/api/v1/kaiju/auth/login` | `{ "email", "password" }` | **200** con `accessToken` (JWT), `tokenType`, `expiresIn` y `usuario` · **401** credenciales incorrectas |

Documentación interactiva (Swagger): `http://<IP_PUBLICA_EC2>:8080/swagger-ui.html` → sección **"Kaiju - Autenticación"**.

**Base de datos:** PostgreSQL en AWS RDS, tabla `usuario_kaiju`:

| Columna | Tipo | Detalle |
|---|---|---|
| `id_usuario_kaiju` | bigint | PK autoincremental |
| `rut` | varchar(12) | único, normalizado (`12345678-5`) |
| `nombre_completo` | varchar(80) | |
| `email` | varchar(120) | único, en minúsculas |
| `password_hash` | varchar(100) | BCrypt |
| `rol` | varchar(10) | `BODEGA`, `VENTAS` o `ADMIN` |
| `fecha_registro` | timestamp | |

> Los tokens de Kaiju se firman con un destinatario propio (`kaiju-app`), por lo que quedan aislados de los demás usuarios del servidor.

---

## 5. Pasos para ejecutar

### Requisitos
- Android Studio (versión reciente) con un emulador o celular Android 9 o superior.
- Microservicio corriendo en el EC2 (AWS Academy) con la base de datos activa.

### 5.1 Levantar el microservicio (EC2)
```bash
cd ~/Backend_SonidoVivo
git checkout alonso/kaiju-auth
set -a; source .env; set +a
./mvnw spring-boot:run
```
Debe aparecer `Started ...Application` y responder Swagger en `http://<IP_PUBLICA_EC2>:8080/swagger-ui.html`.
Variables necesarias en `.env` (no incluidas en el repositorio): `DB_URL`, `DB_USER`, `DB_PASSWORD`, `JWT_SECRET`.

### 5.2 Ejecutar la app
1. Clonar el repositorio:
   ```bash
   git clone https://github.com/Fabi-Hodi/proyecto-kaiju.git
   ```
2. Abrirlo en Android Studio y esperar el **Gradle Sync**.
3. Configurar la **IP pública del EC2** en **dos archivos** (cambia cada vez que se reinicia el laboratorio de AWS):
   - `app/src/main/java/com/example/appkaiju_grupo10/data/remote/RetrofitClient.kt` → `URL_BACKEND = "http://<IP>:8080/"`
   - `app/src/main/res/xml/network_security_config.xml` → `<domain>IP</domain>`
4. Ejecutar con **▶ Run**.
5. **Registrar un operador** y luego **iniciar sesión**.

### 5.3 Ejecutar las pruebas
```bash
# Pruebas unitarias (Kotest + MockK + coroutines-test): ViewModels, Repositories y reglas del negocio
./gradlew testDebugUnitTest

# Pruebas de UI (Compose UI Test) - requieren un emulador encendido
./gradlew connectedDebugAndroidTest
```
| Tipo | Archivos | Cantidad |
|---|---|---|
| Unitarias | `ReglasNegocioTest`, `IndicadoresRepositoryTest`, `IndicadoresViewModelTest`, `AuthRepositoryTest`, `LoginViewModelTest` | 24 |
| UI | `BannerDolarTest`, `TarjetaProductoTest`, `ExampleInstrumentedTest` | 10 |

### 5.4 Instalar la APK firmada
1. Copiar `app-release.apk` al celular e instalarla (permitir la instalación desde esa fuente).
2. La app está registrada en **Android Developer Console** (cuenta de distribución limitada) con su package y la huella SHA-256 de la llave, y el dispositivo de prueba está autorizado.

---

## 6. APK firmado y llave (.jks)
- **APK:** generado en modo **release** desde *Build → Generate Signed App Bundle or APK → APK*.
- **Llave:** `kaiju-release.jks`, alias `kaiju`, validez de 25 años.
- **Registro:** package `cl.duocuc.grupo10.kaiju.inventario` con huella SHA-256 en estado *Registered* en Android Developer Console.
- Las contraseñas de la llave **no se publican** en el repositorio; se entregan al docente por un canal privado.

**Captura del APK firmado:**
![APK firmado] https://drive.google.com/file/d/1MLz0evqkTbzhGfwex9meG6MSuQAp5NNd/view?usp=sharing,
https://drive.google.com/file/d/1Oo5uNaSj6SunY3s14gSGk1dn-cWDrq8H/view?usp=sharing,
https://drive.google.com/file/d/1tB1gCbo8_3Zvaa950f2InmFha3Kd3rb0/view?usp=sharing

**Captura de la llave .jks:**
![Llave jks] https://drive.google.com/file/d/1NAU1wABuW3t51mM1kvh0FPg_cR7hSKl0/view?usp=sharing

---

## Arquitectura y tecnologías
| Capa | Tecnología |
|---|---|
| Interfaz | Kotlin, Jetpack Compose, Material 3, Navigation Compose |
| Arquitectura | MVVM (`ui/screens`, `ui/components`, `viewmodels`, `repository`, `data`, `model`) |
| Persistencia local | Room (productos, categorías y movimientos) |
| Red | Retrofit + Gson (API externa y microservicio) |
| Microservicio | Spring Boot, Spring Security, JWT, BCrypt, JPA |
| Base de datos | PostgreSQL en AWS RDS |
| Pruebas | Kotest, MockK, kotlinx-coroutines-test, Compose UI Test |
| Colaboración | GitHub y Trello |
