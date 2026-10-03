export type EstadoEnvio = 'PENDIENTE' | 'EN_TRANSITO' | 'ENTREGADO' | 'CANCELADO';

export interface Envio {
  id: number;
  codigoRastreo: string;
  destinatario: string;
  direccionDestino: string;
  montoFlete: number;
  estado: EstadoEnvio;
  fechaCreacion: string;
}

export interface CrearEnvioPayload {
  destinatario: string;
  direccionDestino: string;
  montoFlete: number | null;
}

export interface Paquete {
  descripcion: string;
  pesoKg: number;
}

export interface EnvioRegistroPayload {
  numeroTracking: string;
  destinatario: string;
  direccionDestino: string;
  montoFlete: number;
  fechaDespacho: string;
  fechaEntregaEstimada: string;
  paquetes: Paquete[];
}
