# Laboratorio 8: Pruebas y cobertura de ExpresoFast

Continuación de la misma aplicación de los laboratorios 5,6,7

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
3. Ejecutar `03_data_seeds.sql` (Inserta los roles, usuarios de prueba con contraseñas encriptadas mediante BCrypt y datos iniciales).

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
   está excluido de Git. En este equipo se conservaron allí los valores existentes.
3. Ejecutar el proyecto con Maven:
   ```bash
   mvn spring-boot:run
   ```

### Abrir el frontend

Servir `frontend/` con Live Server en el puerto 5500 y abrir `index.html`.

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

Debe aplicar todos los scripts 01 a 04 en orden.
