# Inventario de Activos Tecnológicos

## 1. Descripción del proyecto

Aplicación Full Stack para la administración y el control de activos tecnológicos institucionales. Permite registrar, consultar, actualizar y gestionar activos, aplicar filtros, controlar los cambios de estado y generar reportes descargables.

La solución implementa una API REST con Spring Boot, una interfaz desarrollada en Angular y persistencia en MariaDB. La seguridad se gestiona mediante tokens JWT y roles de usuario.

## 2. Tecnologías utilizadas

**Backend**
- Java 21
- Spring Boot
- Spring Data JPA / Hibernate
- Spring Security
- JWT
- Maven

**Frontend**
- Angular
- TypeScript
- HTML y CSS
- HttpClient

**Base de datos**
- MariaDB

**Herramientas de pruebas**
- Postman
- Navegador web
- Terminal de Windows

## 3. Requisitos previos

Antes de ejecutar el proyecto, se requiere:

- JDK compatible con la versión configurada del proyecto.
- Node.js y npm compatibles con la versión de Angular.
- Angular CLI, si la ejecución del frontend lo requiere.
- MariaDB instalado y en ejecución.
- Maven o Maven Wrapper incluido en el backend.
- Postman para probar los endpoints protegidos.

## 4. Estructura de la entrega

Estructura sugerida:

```text
inventario-entrega/
├── README.md
├── database/
│   └── inventario_db.sql
├── backend/
│   ├── pom.xml
│   └── src/
└── frontend/
    ├── package.json
    └── src/
```

Los nombres de las carpetas son ilustrativos. Ajustarlos a la estructura real de los proyectos antes de entregar.

## 5. Configuración de la base de datos

La aplicación utiliza la base de datos `inventario_db`.

Configuración de referencia en `application.properties`:

```properties
spring.application.name=inventario
spring.datasource.url=jdbc:mariadb://localhost:1010/inventario_db
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.open-in-view=false
```

Las variables `DB_USERNAME` y `DB_PASSWORD` deben configurarse en el entorno antes de iniciar el backend. No publicar contraseñas reales en el repositorio.

El puerto `1010` corresponde a la configuración utilizada durante el desarrollo; confirmar que MariaDB esté escuchando en ese puerto.

## 6. Script SQL

El archivo `database/inventario_db.sql` contiene el esquema inicial de la base de datos.

Incluye las tablas:

- `categorias`: catálogo de categorías y prefijos de inventario.
- `activos`: información de los activos tecnológicos y relación con su categoría.

También incluye claves primarias, claves foráneas, restricciones de unicidad, índices y categorías iniciales.

Para ejecutar el script desde la consola de MariaDB:

```sql
SOURCE C:/ruta/de/tu/entrega/database/inventario_db.sql;
```

Verificar las tablas:

```sql
USE inventario_db;
SHOW TABLES;
SELECT * FROM categorias;
SELECT id, folio_inventario, numero_serie, estado FROM activos;
```

El script está pensado para no eliminar tablas ni registros existentes. Revisar la compatibilidad con las entidades Java antes de ejecutarlo sobre una base ya utilizada.

## 7. Instalación y ejecución

### Backend

Abrir una terminal en la carpeta del backend y ejecutar:

```bash
mvn clean install
mvn spring-boot:run
```

Si el proyecto incluye Maven Wrapper, se puede utilizar `mvnw.cmd` en Windows.

Configurar previamente las credenciales de base de datos y las variables de inicialización de usuarios que requiera la aplicación.

La URL base del backend es:

```text
http://localhost:8080
```

### Frontend

Abrir otra terminal en la carpeta del frontend:

```bash
npm install
ng serve
```

La aplicación Angular se encuentra normalmente en:

```text
http://localhost:4200
```

La interfaz consume la API REST del backend.

## 8. Autenticación y roles

La aplicación utiliza autenticación mediante JWT. Las operaciones protegidas requieren enviar el token en el encabezado HTTP `Authorization`.

Formato:

```http
Authorization: Bearer <TOKEN_JWT>
```

### Inicio de sesión

La ruta exacta depende del controlador de autenticación implementado. Si el backend utiliza `POST /api/auth/login`, los cuerpos de ejemplo serían los siguientes.

**Ejemplo de solicitud para ADMIN:**

```json
{
  "username": "admin",
  "password": "<CONTRASEÑA_CONFIGURADA_PARA_ADMIN>"
}
```

**Ejemplo de solicitud para USER:**

```json
{
  "username": "consulta",
  "password": "<CONTRASEÑA_CONFIGURADA_PARA_USER>"
}
```

Los nombres de usuario son ejemplos y deben coincidir con los usuarios inicializados por el backend. Sustituir los valores de contraseña por las credenciales de prueba realmente configuradas. No asumir que estas cuentas existen sin verificarlas.

El cuerpo JSON anterior corresponde al inicio de sesión; no debe enviarse como cuerpo en los endpoints que no lo soliciten.

### Roles

**ADMIN**
- Registrar activos.
- Editar activos.
- Ejecutar operaciones administrativas permitidas.
- Consultar información y reportes autorizados.

**USER**
- Consultar activos y utilizar las funciones de lectura que tenga habilitadas.
- No debe ejecutar operaciones administrativas restringidas.

La autorización debe validarse en el backend, no únicamente ocultando botones en Angular.

## 9. Funcionalidades principales

- Registro de activos tecnológicos.
- Consulta de activos registrados.
- Edición de la información permitida.
- Gestión de estados.
- Catálogo de categorías.
- Búsqueda por número de serie.
- Búsqueda por marca y modelo.
- Filtros por categoría y estado.
- Filtros por rango de costo.
- Paginación y ordenamiento, según la implementación.
- Generación y descarga de reportes.
- Control de acceso mediante JWT y roles.

## 10. Reglas de negocio

### Folio de inventario

El folio sigue el formato:

```text
[PREFIJO]-[AÑO]-[CONSECUTIVO]
```

Ejemplos:

```text
LAP-2026-001
MON-2026-001
CEL-2026-001
```

El prefijo depende de la categoría del activo.

### Número de serie

El número de serie debe ser único para evitar registros duplicados.

### Estados permitidos

- `DISPONIBLE`
- `ASIGNADO`
- `EN_MANTENIMIENTO`
- `BAJA`

### Baja irreversible

Un activo que se encuentre en estado `BAJA` no puede volver a un estado operativo. La regla debe aplicarse en el backend para impedir que se evada mediante llamadas directas a la API.

La prueba de esta regla debe confirmar que el backend rechaza la reactivación e informa el error correspondiente.

### Validaciones

Los datos obligatorios deben validarse antes de guardar. También deben respetarse las restricciones de integridad de la base de datos y las autorizaciones correspondientes.

## 11. Endpoints REST

URL base:

```text
http://localhost:8080/api
```

| Método | Endpoint | Descripción |
|---|---|---|
| GET | `/activos` | Consultar activos y aplicar los filtros soportados |
| POST | `/activos` | Registrar un activo |
| PUT | `/activos/{id}` | Actualizar un activo |
| PATCH | `/activos/{id}/estado?estado=DISPONIBLE` | Cambiar el estado del activo |
| GET | `/categorias` | Consultar categorías |
| POST | `/categorias` | Registrar una categoría |
| DELETE | `/categorias/{id}` | Eliminar una categoría, sujeto a las restricciones de integridad |
| GET | `/activos/reportes/zip` | Generar el reporte ZIP |

`{id}` representa el identificador del recurso. Para los activos se utiliza un UUID.

El endpoint de cambio de estado puede probarse, por ejemplo, con:

```http
PATCH http://localhost:8080/api/activos/<UUID_DEL_ACTIVO>/estado?estado=DISPONIBLE
```

Este endpoint utiliza el parámetro de consulta `estado`; no requiere un cuerpo JSON si así está implementado en el controlador.

Los endpoints y métodos anteriores deben cotejarse con los controladores definitivos antes de entregar. La ruta de autenticación y cualquier endpoint adicional deben documentarse según su implementación real.

## 12. Exportación de reportes

El endpoint de reportes permite solicitar un archivo ZIP con información del inventario.

```http
GET http://localhost:8080/api/activos/reportes/zip
```

La respuesta implementada puede contener el archivo ZIP codificado en Base64 dentro de un JSON.

El contenido del ZIP incluye los archivos de reporte definidos por la aplicación, como el archivo Excel y el registro de auditoría en texto.

Para comprobar la exportación:
1. Enviar la solicitud con el token requerido.
2. Revisar que la respuesta sea exitosa.
3. Decodificar el contenido Base64.
4. Extraer el ZIP y comprobar sus archivos.

## 13. Pruebas realizadas

Las pruebas deben registrar el resultado observado en el entorno de ejecución.

| Prueba | Resultado esperado |
|---|---|
| Consultar activos | Devuelve los registros autorizados |
| Registrar un activo | Guarda un registro válido |
| Registrar una serie duplicada | Rechaza el duplicado |
| Editar un activo | Actualiza los datos permitidos |
| Filtrar por estado | Devuelve los registros coincidentes |
| Filtrar por categoría | Devuelve los registros coincidentes |
| Cambiar el estado | Aplica un cambio permitido |
| Reactivar un activo en BAJA | Rechaza la operación |
| Acceder con USER a una operación restringida | Deniega el acceso |
| Generar reporte ZIP | Devuelve el reporte configurado |

En la prueba de irreversibilidad, el backend devolvió HTTP `400` con el mensaje:

```text
Un activo en BAJA no puede volver a un estado operativo
```

Esto demuestra que la solicitud de reactivación fue rechazada. Para completar la verificación, consultar la base de datos y confirmar que el activo conserve el estado `BAJA`.

## 14. Video de demostración

Incluir el enlace o archivo del video de demostración cuando esté disponible.

Se recomienda mostrar:
1. Inicio del backend y conexión con MariaDB.
2. Inicio del frontend Angular.
3. Inicio de sesión con los roles configurados.
4. Registro y consulta de activos.
5. Filtros de búsqueda.
6. Edición y cambio de estado.
7. Rechazo de reactivación de un activo en `BAJA`.
8. Generación y contenido del reporte ZIP.

**Video:** `Pegar aquí el enlace o la ubicación del video final`.

---

## Autor

**Alfredo Zamorano Ahumada**

Proyecto técnico: Módulo de Inventario de Activos Tecnológicos.
