# Laboratorio 11: Formularios reactivos avanzados y consolidación Full-Stack

Universidad de Costa Rica, Sede del Atlántico, Recinto Paraíso. IF0009 - Desarrollo de Software IV, II-2026.

Estudiante: Daniel Gómez Solano. Carné: C5F451.

## Estructura

```text
expresofast-backend/
  src/main/java/com/expresofast/
    model/
    repository/
    model/Paquete.java
    dto/EnvioRegistroDTO.java
    dto/PaqueteDTO.java
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
  src/app/components/envio-avanzado-form/
  src/app/validators/envio.validators.ts
  src/app/components/envio-tracking/
  src/app/app.component.html
  src/app/app.config.ts
  src/app/app.routes.ts
```

## Requisitos

- JDK 21 y Maven o Maven Wrapper.
- SQL Server y la base ExpresoFast_C5F451_II2026.
- Node.js ^22.22.3, ^24.15.0 o >=26.0.0 y npm, según las dependencias Angular instaladas. Consultar `node --version` y `npm --version`.
- Se usa Angular 22, con Standalone, CSS y sin SSR.

## Preparar SQL Server

Para una base nueva, ejecutar en SSMS, sobre ExpresoFast_C5F451_II2026, en este orden:

1. `expresofast-backend/database/01_schema_lab5.sql`
2. `expresofast-backend/database/02_schema_lab6_extension.sql`
3. `expresofast-backend/database/03_data_seeds.sql`
4. `expresofast-backend/database/04_schema_lab7_extension.sql`
5. `expresofast-backend/database/lab9/schema.sql`
6. `expresofast-backend/database/lab9/data.sql`
7. `expresofast-backend/database/lab10/schema.sql`
8. `expresofast-backend/database/lab11/schema.sql`

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
2. `/nuevo-envio`: rastreo elegido por el operador, destinatario, dirección, flete, fechas y paquetes dinámicos. Al guardar se muestra el código normalizado en mayúsculas y un enlace para rastrearlo.
3. `/rastreo`: buscar por código, ver detalle y progreso. Un envío cancelado muestra el aviso correspondiente.

La ruta vacía redirige a `/envios`. La navegación usa routerLink sin recargar. HttpClient usa Fetch y un interceptor agrega el JWT de la sesión a las peticiones de la API. Cerrar sesión elimina el token.

Se mantienen las transiciones anteriores: PENDIENTE → EN_TRANSITO → ENTREGADO, o PENDIENTE → CANCELADO. Cada cambio conserva el registro de bitácora del usuario autenticado. Los estados finales no se pueden modificar.

## API

Todas estas rutas requieren JWT y mantienen los permisos de los laboratorios anteriores.

| Método | Ruta | Resultado |
| --- | --- | --- |
| GET | `/api/v1/envios` | Lista completa de EnvioDTO |
| GET | `/api/v1/envios/rastreo/{codigo}` | Detalle o 404 |
| POST | `/api/v1/envios` | Registro anterior con código generado, conservado para compatibilidad |
| POST | `/api/v1/envios/registro` | EnvioRegistroDTO con paquetes → EnvioDTO, HTTP 201 |
| GET | `/api/envios/check-tracking/{trackingNumber}` | `true` si el rastreo existe; `false` si está disponible |
| GET | `/api/v1/envios/check-tracking/{trackingNumber}` | Alias utilizado por Angular |
| PATCH | `/api/v1/envios/{id}/estado` | Actualiza con `{ "nuevoEstado": "EN_TRANSITO" }` |
| GET | `/api/v1/envios/paginados` | Consulta paginada del laboratorio 9 |
| GET | `/api/v1/envios/procedimiento/{estado}` | Procedimiento almacenado anterior |

El registro avanzado recibe:

```json
{
  "numeroTracking": "EXP-2026-001",
  "destinatario": "Ana Solano",
  "direccionDestino": "Cartago, Paraíso",
  "montoFlete": 2500,
  "fechaDespacho": "2026-10-02",
  "fechaEntregaEstimada": "2026-10-03",
  "paquetes": [
    { "descripcion": "Libros", "pesoKg": 1.25 },
    { "descripcion": "Ropa", "pesoKg": 2.50 }
  ]
}
```

## Fundamentación: UX y escalabilidad

`FormArray` representa una cantidad variable de paquetes como una colección de grupos con descripción y peso. Cada grupo conserva su valor, errores y estados como `touched` y `dirty`. Al agregar o eliminar un paquete se modifica el modelo reactivo y Angular actualiza la vista. El operador ve únicamente los paquetes que necesita, recibe errores asociados a sus datos y no depende de un límite artificial de diez ítems.

Diez bloques estáticos y ocultos obligan a duplicar HTML y reglas, gestionar visibilidad e índices y decidir qué campos ocultos deben validarse o enviarse. Ocultar un elemento no elimina automáticamente su control ni sus errores. Con `FormArray`, una sola función define el grupo y sus reglas, una iteración dibuja los controles y `getRawValue()` obtiene la estructura que espera el backend. La regla de conservar al menos un paquete se aplica en un único lugar.

Los formularios reactivos permiten probar la validación sin simular toda la interacción del navegador. El tipado detecta errores de desarrollo antes de ejecutar: el peso es numérico y el arreglo contiene grupos con la misma estructura. Esto no sustituye las validaciones de entrada ni las del servidor; TypeScript desaparece al compilar. La combinación mejora mantenimiento, pruebas y respuesta visual al usuario.

## Fundamentación: ciclo de eventos

El validador cruzado es síncrono: Angular lo invoca al actualizar los controles, lee las dos fechas y recibe `null` o `{ fechasInvalidas: true }` dentro de la misma pila de ejecución. No espera recursos externos; la comparación se completa antes de continuar con las siguientes instrucciones.

El validador de rastreo depende de una respuesta HTTP. JavaScript inicia el temporizador y posteriormente la petición, pero libera la pila mientras espera. El navegador gestiona la espera fuera de esa pila y el event loop permite continuar atendiendo eventos y dibujando la interfaz. Cuando se completa la operación, sus callbacks se procesan según el mecanismo utilizado. Las reacciones de Promise se ejecutan como microtareas después de vaciar la pila y antes de la siguiente tarea. Los temporizadores programan tareas; un Observable no implica por sí mismo una cola concreta: su fuente y sus operadores determinan cuándo emite.

Angular necesita un Observable o Promise porque los errores todavía no están disponibles al iniciar la consulta. Ese contrato permite pasar a PENDING y aplicar el resultado cuando llegue: `null` para disponible o `{ trackingTomado: true }` para repetido. En este caso se devuelve un Observable finito basado en `timer` y `HttpClient`; `catchError` convierte fallas de red en un error de validación recuperable. Angular ejecuta validadores asíncronos cuando pasan los síncronos del control, evitando consultar por un rastreo vacío o de formato inválido.

La comprobación remota mejora la UX pero no garantiza exclusividad entre clientes: otro operador podría guardar el mismo código después de la consulta. Por eso el backend comprueba nuevamente y la base mantiene un índice único.

## Comprobación manual en SQL Server

1. Iniciar backend y frontend.
2. Iniciar sesión como ADMIN u OPERADOR y registrar un rastreo nuevo con dos paquetes.
3. Revisar `dbo.Envio` y `dbo.PAQUETES`: deben compartir el `envio_id`, guardar las fechas y sumar los pesos en el envío.
4. Intentar reutilizar el rastreo, invertir o igualar fechas y quitar el último paquete. El formulario debe impedir esas acciones inválidas.
5. Detener el backend y cambiar el rastreo. Debe aparecer el error de verificación; después de reiniciar, usar Reintentar verificación.

Las pruebas automáticas del backend usan H2. No sustituyen la ejecución del script y la comprobación final sobre tu instancia de SQL Server.

## Verificación realizada

- Maven `verify`: 101 pruebas correctas y control de cobertura aprobado.
- Compilador Angular `ngc --noEmit`: tipos y plantillas correctos con configuración estricta.
- Frontend: 9 pruebas correctas en 3 archivos, incluyendo el registro con paquetes y los validadores.
- `git diff --check`: sin errores de espacios.
- Compilación de producción de Angular correcta en una copia temporal con `npm ci` y el mismo `package-lock.json`. Se utilizó esa copia porque OneDrive no permitió leer algunos archivos de `node_modules` del proyecto original.
- No se ejecutó el script contra SQL Server ni se realizaron commits o push.
