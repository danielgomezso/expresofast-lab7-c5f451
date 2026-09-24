# Laboratorio 9: Procedimientos almacenados y paginación de ExpresoFast

Continuación de la misma aplicación de los laboratorios 5, 6, 7 y 8.

## 1. Portada
* **Universidad:** Universidad de Costa Rica
* **Sede:** Sede del Atlántico - Recinto Paraíso
* **Carrera:** Informática Empresarial
* **Curso:** IF0009 - Desarrollo de Software IV
* **Ciclo:** II-2026
* **Estudiante:** Daniel Gómez Solano
* **Carné:** C5F451
---

## 2. Entorno
Este proyecto fue desarrollado y probado utilizando las siguientes tecnologías:
* **Lenguaje:** Java 21
* **Framework:** Spring Boot 3.x
* **Gestor de Dependencias:** Maven
* **Base de Datos:** Microsoft SQL Server Developer Edition
* **Navegador web de pruebas:** Google Chrome
* **Otras herramientas:** JWT (jjwt 0.12.x), Git, Postman.

---

## 3. Guía de Base de Datos
Para inicializar la base de datos `ExpresoFast_C5F451_II2026`, se deben ejecutar los scripts ubicados en la carpeta `database/` en el siguiente orden desde SQL Server Management Studio (SSMS):

1. Ejecutar `01_schema_lab5.sql` (Crea la estructura base de la Parte I).
2. Ejecutar `02_schema_lab6_extension.sql` (Crea las tablas `Usuario`, `Rol`, `usuarioRol` y `BitacoraEnvio`).
3. Ejecutar `03_data_seeds.sql` (Inserta roles, usuarios y datos iniciales).
4. Ejecutar `04_schema_lab7_extension.sql`.
5. Ejecutar `lab9/schema.sql` (Agrega destinatario y el procedimiento almacenado).
6. Ejecutar `lab9/data.sql` (Inserta 16 envíos de prueba sin duplicar códigos existentes).

---

## 4. Usuarios de Prueba
A continuación se detallan las credenciales preconfiguradas para probar el control de acceso basado en roles (RBAC):

| Usuario | Contraseña | Rol Asignado |
| :--- | :--- | :--- |
| admin | admin123 | `ROLE_ADMIN` |
| operador | operador123 | `ROLE_OPERADOR` |
| conductor1 | cond123 | `ROLE_CONDUCTOR` |

> **Nota:** Las contraseñas en la base de datos se encuentran encriptadas. Estas son las credenciales en texto plano para el inicio de sesión.
---

## 5. Instrucciones de Ejecución

### Levantar el Backend (Spring Boot)
1. Abrir una terminal en la carpeta raíz del backend: `cd backend`
2. Configurar `DB_URL`, `DB_USERNAME` y `DB_PASSWORD` como variables de entorno,
   o crear `backend/application-local.properties` con `spring.datasource.url`,
   `spring.datasource.username` y `spring.datasource.password`. Este archivo local
   está excluido de Git. Los valores locales existentes se conservan.
3. Ejecutar el proyecto con Maven:
   ```bash
   mvn spring-boot:run
   ```

### Abrir el frontend

Servir `frontend/` con Live Server en el puerto 5500 y abrir `index.html`. Al iniciar sesión se abre `dashboard_paginado.html`; el enlace Gestión de envíos permite volver al registro, cambios de estado y bitácora.

## 6. Ejecutar las pruebas del laboratorio 7

Usar **JDK 21**: JaCoCo 0.8.11, solicitado por el enunciado, no debe ejecutarse con
Java 24. En PowerShell, para esta instalación:

```powershell
$env:JAVA_HOME = 'C:\Program Files\Java\jdk-21'
cd backend
mvn clean test
mvn clean verify
```

También se puede utilizar `./mvnw.cmd clean verify` desde `backend/`.
En otros equipos, ajustar `JAVA_HOME` a su instalación de Java 21.

- `clean test`: ejecuta JUnit 5 y genera `backend/target/site/jacoco/index.html`.
- `clean verify`: además empaqueta y exige al menos **85 % de instrucciones cubiertas**.
- Surefire **3.2.5** y JaCoCo **0.8.11** están declarados en `backend/pom.xml`.
- La regla mide `cr.ac.ucr.paraiso.ie.c5f451.expresofast.business`, el paquete real
  de servicios de esta aplicación, equivalente al `com.expresofast.service` del ejemplo.
  Incluye los cuatro servicios, sin exclusiones de clases.

Las pruebas unitarias usan `@ExtendWith(MockitoExtension.class)`, `@Mock` y
`@InjectMocks`. Los cortes web usan `@WebMvcTest`, MockMvc y la configuración real
de permisos; `@WithMockUser` simula la identidad. No se desactivan los filtros para
la prueba de HTTP 403.

El test de arranque existente se conserva y utiliza H2 **solo en pruebas** mediante
`src/test/resources/application.properties`. Las pruebas no requieren SQL Server
ni utilizan sus credenciales. H2 comprueba el arranque y los mapeos; no certifica
la compatibilidad del script de migración con SQL Server.

| Clase | Casos principales |
| --- | --- |
| `EnvioServiceTest` | Creación PENDIENTE, capacidad excedida y exacta, referencias inválidas, consulta por ID, transiciones, cancelación, bitácora y tarifas parametrizadas |
| `VehiculoServiceTest` | Registro válido, placa duplicada, capacidad inválida y asignación de conductor activo/inactivo/inexistente |
| `EmpresaLogisticaServiceTest` | Registro, duplicados por nombre/cédula, datos inválidos, consulta existente/inexistente y listado |
| `AuthServiceTest` | Autenticación, generación del token, roles y rechazo de credenciales |
| `EnvioControllerTest` | HTTP 200, 201, 400, 403 y 404; contrato JSON y errores RFC 7807 |
| `AuthControllerTest` | Login HTTP 200 con token y HTTP 401 por credenciales incorrectas |

## 7. Base de datos

Los scripts se ejecutan manualmente en SSMS sobre la base de datos del proyecto. Todos los .sql estan en la carpeta database

## 8. Funcionalidades del laboratorio 9

- `EnvioDTO` entrega únicamente id, codigoRastreo, destinatario, direccionDestino, montoFlete, estado y fechaCreacion. Monto y estado se obtienen de los campos existentes costo y estadoEnvio.
- `EnvioRepository.buscarPaginado` aplica búsqueda por rastreo, destinatario o dirección y filtro por estado en SQL. La proyección DTO evita cargar las relaciones y no usa JOIN FETCH sobre colecciones.
- `EnvioService.listarPaginado` crea un PageRequest con ordenamiento y un segundo orden por id para estabilizar los empates. Rechaza páginas negativas, tamaños fuera de 1 a 100, estados y campos de ordenamiento inválidos.
- El procedimiento `SP_OBTENER_ENVIOS_POR_ESTADO` recibe pEstado y devuelve envíos ordenados por fecha_creacion descendente. Se invoca con `@Procedure`, `@Param("pEstado")` y un mapeo `@NamedStoredProcedureQuery`, dentro de una transacción de lectura.
- La consulta paginada y el procedimiento requieren JWT y permiten ADMIN, OPERADOR y CONDUCTOR.
- La vista usa HTML5, el CSS existente y JavaScript sin dependencias. Renderiza los valores con textContent, muestra errores y permite navegar sin recargar la página.
- El selector de procedimiento consulta el endpoint del SP. En ese modo se desactivan la búsqueda, el ordenamiento y el paginador porque la respuesta requerida es una lista completa por estado. Elegir Consulta paginada restaura los filtros.
- No se implementó el procedimiento opcional de métricas.

### API 

| Ruta GET | Respuesta |
| --- | --- |
| `/api/v1/envios` | Página con content, number, size, totalElements, totalPages, first y last |
| `/api/v1/envios/procedimiento/PENDIENTE` | Lista del procedimiento almacenado |

Parámetros de la consulta paginada:

| Parámetro | Predeterminado | Valores |
| --- | --- | --- |
| page | 0 | Índice desde cero |
| size | 5 | 1 a 100; la interfaz ofrece 5, 10 y 20 |
| sortBy | fechaCreacion | id, codigoRastreo, destinatario, direccionDestino, montoFlete, estado, fechaCreacion |
| direction | DESC | ASC o DESC |
| busqueda | vacío | Texto por rastreo, destinatario o dirección |
| estado | vacío | PENDIENTE, EN_TRANSITO, ENTREGADO o CANCELADO |

Ejemplo: `http://localhost:8080/api/v1/envios?page=1&size=5&sortBy=montoFlete&direction=ASC&busqueda=Cartago&estado=PENDIENTE`.
Enviar `Authorization: Bearer <token>` usando el token del login existente.

### Verificación y entrega

1. Aplicar los scripts SQL en el orden de la sección 3 y arrancar el backend.
2. Iniciar sesión desde `http://localhost:5500/index.html`.
3. Con las 16 semillas y ningún otro envío, tamaño 5 produce cuatro páginas; la última contiene un envío. Si ya existen envíos, el total incluye esos registros.
4. Probar Primera, Anterior, Siguiente y Última. La pantalla suma 1 al índice de la API. Sin resultados muestra Página 0 de 0 y desactiva los botones.
5. Probar tamaños 10 y 20, búsqueda, estado y orden. Al cambiar un selector se vuelve a page=0.
6. Seleccionar cada estado del procedimiento. En Network debe aparecer `/api/v1/envios/procedimiento/{estado}`.
7. En SSMS ejecutar `EXEC dbo.SP_OBTENER_ENVIOS_POR_ESTADO @pEstado = 'PENDIENTE';` y comparar con la interfaz.
8. Ejecutar `mvn clean verify` en backend. Las pruebas nuevas cubren filtros, límites, metadatos, permisos, mapeo DTO y paginación real con H2. Estas pruebas no ejecutan el procedimiento T-SQL en SQL Server; esa comprobación requiere los pasos 1 y 7.
9. Capturar la consola en una página intermedia con botones activos y el resultado del procedimiento. Incluir esas capturas y el enlace público del repositorio en el PDF o ZIP de entrega.

El procedimiento y las semillas se entregan como scripts; no se aplicaron al SQL Server remoto configurado en este equipo. Las capturas de entrega deben realizarse después de aplicarlos y comprobar la conexión real.
