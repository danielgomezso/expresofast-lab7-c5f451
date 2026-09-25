# Laboratorio 10: ExpresoFast con Angular Standalone

Universidad de Costa Rica, Sede del Atlántico, Recinto Paraíso. IF0009 - Desarrollo de Software IV, II-2026.

Estudiante: Daniel Gómez Solano. Carné: C5F451.

## Estructura

```text
expresofast-backend/
  src/main/java/com/expresofast/
    model/
    repository/
    dto/CrearEnvioDTO.java
    dto/EnvioDTO.java
    service/EnvioService.java
    service/EnvioServiceImpl.java
    controller/EnvioController.java
    security/
  database/
  docs/
  legacy-frontend/
expresofast-frontend/
  src/environments/environment.ts
  src/app/models/envio.model.ts
  src/app/services/
  src/app/components/envio-list/
  src/app/components/envio-form/
  src/app/components/envio-tracking/
  src/app/app.component.html
  src/app/app.config.ts
  src/app/app.routes.ts
```

## Requisitos

- JDK 21 y Maven o Maven Wrapper.
- SQL Server y la base ExpresoFast_C5F451_II2026.
- Node.js ^22.22.3, ^24.15.0 o >=26.0.0 y npm, según las dependencias Angular instaladas. Consultar `node --version` y `npm --version`.
- Se conservó la base Angular 22.1 creada por el estudiante, Standalone, CSS y sin SSR.

## Preparar SQL Server

Para una base nueva, ejecutar en SSMS, sobre ExpresoFast_C5F451_II2026, en este orden:

1. `expresofast-backend/database/01_schema_lab5.sql`
2. `expresofast-backend/database/02_schema_lab6_extension.sql`
3. `expresofast-backend/database/03_data_seeds.sql`
4. `expresofast-backend/database/04_schema_lab7_extension.sql`
5. `expresofast-backend/database/lab9/schema.sql`
6. `expresofast-backend/database/lab9/data.sql`
7. `expresofast-backend/database/lab10/schema.sql`

## Compilar y ejecutar

Desde la raíz, abrir una terminal PowerShell para el backend:

```powershell
$env:JAVA_HOME = 'C:\Program Files\Java\jdk-21'
cd expresofast-backend
.\mvnw.cmd spring-boot:run
```

En otra terminal, desde la raíz:

```powershell
cd expresofast-frontend
npm ci
npx ng serve
```

Abrir http://localhost:4200. El backend escucha en http://localhost:8080. La URL base del cliente está en `src/environments/environment.ts` y termina en `/api/v1/`.

Para compilar y comprobar:

```powershell
cd expresofast-backend
.\mvnw.cmd clean verify
cd ../expresofast-frontend
npm run build
npm test -- --watch=false
```

## Uso

Iniciar sesión con los usuarios de prueba de los laboratorios anteriores:

| Usuario | Contraseña | Permisos relevantes |
| --- | --- | --- |
| admin | admin123 | Consultar, crear y cambiar estado |
| operador | operador123 | Consultar y crear |
| conductor1 | cond123 | Consultar y cambiar estado |

1. `/envios`: tabla de guías, flete, insignias y selector para actualizar estados.
2. `/nuevo-envio`: destinatario, dirección y monto positivo, con validación. Al guardar se muestra el código generado y un enlace para rastrearlo.
3. `/rastreo`: buscar por código, ver detalle y progreso. Un envío cancelado muestra el aviso correspondiente.

La ruta vacía redirige a `/envios`. La navegación usa routerLink sin recargar. HttpClient usa Fetch y un interceptor agrega el JWT de la sesión a las peticiones de la API. Cerrar sesión elimina el token.

Se mantienen las transiciones anteriores: PENDIENTE → EN_TRANSITO → ENTREGADO, o PENDIENTE → CANCELADO. Cada cambio conserva el registro de bitácora del usuario autenticado. Los estados finales no se pueden modificar.

## API

Todas estas rutas requieren JWT y mantienen los permisos de los laboratorios anteriores.

| Método | Ruta | Resultado |
| --- | --- | --- |
| GET | `/api/v1/envios` | Lista completa de EnvioDTO |
| GET | `/api/v1/envios/rastreo/{codigo}` | Detalle o 404 |
| POST | `/api/v1/envios` | CrearEnvioDTO → EnvioDTO, HTTP 201 |
| PATCH | `/api/v1/envios/{id}/estado` | Actualiza con `{ "nuevoEstado": "EN_TRANSITO" }` |
| GET | `/api/v1/envios/paginados` | Consulta paginada del laboratorio 9 |
| GET | `/api/v1/envios/procedimiento/{estado}` | Procedimiento almacenado anterior |

POST recibe:

```json
{
  "destinatario": "Ana Solano",
  "direccionDestino": "Cartago, Paraíso",
  "montoFlete": 2500
}
```
