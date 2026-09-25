-- Seleccionar ExpresoFast_C5F451_II2026 en SSMS antes de ejecutar.
-- Extensión aditiva: conserva las tablas y los datos de laboratorios 5 y 6.
IF COL_LENGTH('dbo.Conductor', 'activo') IS NULL
    ALTER TABLE dbo.Conductor ADD activo BIT NOT NULL CONSTRAINT DF_Conductor_activo DEFAULT 1;
IF COL_LENGTH('dbo.Vehiculo', 'conductor_id') IS NULL
    ALTER TABLE dbo.Vehiculo ADD conductor_id INT NULL CONSTRAINT FK_Vehiculo_Conductor REFERENCES dbo.Conductor(conductor_id);
