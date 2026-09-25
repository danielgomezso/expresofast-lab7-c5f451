IF COL_LENGTH('dbo.Envio', 'destinatario') IS NULL
    ALTER TABLE dbo.Envio ADD destinatario VARCHAR(100) NULL;
GO
CREATE OR ALTER PROCEDURE dbo.SP_OBTENER_ENVIOS_POR_ESTADO
    @pEstado VARCHAR(20)
AS
BEGIN
    SET NOCOUNT ON;
    SELECT envio_id, codigo_rastreo, destinatario, direccion_destino, peso_kg,
           costo, estado_envio, vehiculo_id, conductor_id, fecha_creacion, fecha_modificacion
    FROM dbo.Envio
    WHERE estado_envio = @pEstado
    ORDER BY fecha_creacion DESC, envio_id DESC;
END;
GO
