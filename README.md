# Laboratorio 6: Plataforma Full-Stack de Logística "ExpresoFast" (Parte II)

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
cd "d:\temporada 4\Software 4\DSW4_workspace\Laboratorio_6"

git init
git branch -M main
git remote add origin https://github.com/danielgomezso/expresofast-lab6-c5f451.git
---

## 5. Instrucciones de Ejecución

### Levantar el Backend (Spring Boot)
1. Abrir una terminal en la carpeta raíz del backend: `cd expresofast`
2. Verificar que el archivo `application.properties` tenga las credenciales correctas de tu base de datos SQL Server.
3. Ejecutar el proyecto con Maven:
   ```bash
   mvn spring-boot:run