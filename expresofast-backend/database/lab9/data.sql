SET XACT_ABORT ON;
BEGIN TRANSACTION;

DECLARE @vehiculoId INT = (SELECT vehiculo_id FROM dbo.Vehiculo WHERE placa = 'EF-0001');
DECLARE @conductorId INT = (SELECT conductor_id FROM dbo.Conductor WHERE licencia = 'LIC-EF-0001');

IF @vehiculoId IS NULL OR @conductorId IS NULL
    THROW 50001, 'Ejecute primero database/03_data_seeds.sql.', 1;

INSERT INTO dbo.Envio (codigo_rastreo, destinatario, direccion_destino, peso_kg, costo,
                       estado_envio, vehiculo_id, conductor_id, fecha_creacion, fecha_modificacion)
SELECT s.codigo, s.destinatario, s.direccion, 5, s.costo, s.estado,
       @vehiculoId, @conductorId, DATEADD(MINUTE, s.numero, CAST('20260901' AS DATETIME)),
       DATEADD(MINUTE, s.numero, CAST('20260901' AS DATETIME))
FROM (VALUES
    (1, 'EXP-9001', 'Ana Mora', 'Cartago centro', 2500, 'PENDIENTE'),
    (2, 'EXP-9002', 'Luis Solano', 'Paraiso centro', 3000, 'EN_TRANSITO'),
    (3, 'EXP-9003', 'Maria Rojas', 'San Jose centro', 3500, 'ENTREGADO'),
    (4, 'EXP-9004', 'Carlos Castro', 'Heredia centro', 4000, 'CANCELADO'),
    (5, 'EXP-9005', 'Sofia Vargas', 'Alajuela centro', 4500, 'PENDIENTE'),
    (6, 'EXP-9006', 'Pedro Ruiz', 'Turrialba centro', 5000, 'EN_TRANSITO'),
    (7, 'EXP-9007', 'Laura Gomez', 'Orosi centro', 2500, 'ENTREGADO'),
    (8, 'EXP-9008', 'Jose Marin', 'Cachi centro', 3000, 'CANCELADO'),
    (9, 'EXP-9009', 'Elena Soto', 'Tres Rios centro', 3500, 'PENDIENTE'),
    (10, 'EXP-9010', 'Diego Leon', 'San Pedro centro', 4000, 'EN_TRANSITO'),
    (11, 'EXP-9011', 'Lucia Arias', 'Curridabat centro', 4500, 'ENTREGADO'),
    (12, 'EXP-9012', 'Pablo Vega', 'Escazu centro', 5000, 'CANCELADO'),
    (13, 'EXP-9013', 'Andrea Diaz', 'Santa Ana centro', 5500, 'PENDIENTE'),
    (14, 'EXP-9014', 'Mario Perez', 'Moravia centro', 3500, 'EN_TRANSITO'),
    (15, 'EXP-9015', 'Paula Chaves', 'Coronado centro', 4000, 'ENTREGADO'),
    (16, 'EXP-9016', 'Daniel Campos', 'San Ramon centro', 6000, 'CANCELADO')
) AS s(numero, codigo, destinatario, direccion, costo, estado)
WHERE NOT EXISTS (SELECT 1 FROM dbo.Envio e WHERE e.codigo_rastreo = s.codigo);

COMMIT;
