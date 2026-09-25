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
