USE [ExpresoFast_C5F451_II2026];
GO
SET XACT_ABORT ON;
BEGIN TRANSACTION;

IF COL_LENGTH('dbo.Envio', 'fecha_despacho') IS NULL
    ALTER TABLE dbo.Envio ADD fecha_despacho DATE NULL;

IF COL_LENGTH('dbo.Envio', 'fecha_entrega_estimada') IS NULL
    ALTER TABLE dbo.Envio ADD fecha_entrega_estimada DATE NULL;

IF OBJECT_ID('dbo.PAQUETES', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.PAQUETES (
        id BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
        envio_id INT NOT NULL,
        descripcion VARCHAR(255) NOT NULL,
        peso_kg DECIMAL(5,2) NOT NULL,
        CONSTRAINT CK_Paquetes_Peso CHECK (peso_kg > 0),
        CONSTRAINT FK_Paquetes_Envios FOREIGN KEY (envio_id)
            REFERENCES dbo.Envio(envio_id) ON DELETE CASCADE
    );
    CREATE INDEX IX_Paquetes_Envio ON dbo.PAQUETES(envio_id);
END;

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE object_id = OBJECT_ID('dbo.Envio') AND name = 'UX_Envio_Tracking_Lab11')
    CREATE UNIQUE INDEX UX_Envio_Tracking_Lab11 ON dbo.Envio(codigo_rastreo);

COMMIT TRANSACTION;
GO
CREATE OR ALTER PROCEDURE dbo.SP_OBTENER_ENVIOS_POR_ESTADO
    @pEstado VARCHAR(20)
AS
BEGIN
    SET NOCOUNT ON;
    SELECT envio_id, codigo_rastreo, destinatario, direccion_destino, peso_kg,
           costo, estado_envio, vehiculo_id, conductor_id, fecha_creacion, fecha_modificacion,
           fecha_despacho, fecha_entrega_estimada
    FROM dbo.Envio
    WHERE estado_envio = @pEstado
    ORDER BY fecha_creacion DESC, envio_id DESC;
END;
GO
