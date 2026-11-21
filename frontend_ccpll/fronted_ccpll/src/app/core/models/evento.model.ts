export interface Ambiente {
  id: number;
  nombre: string;
  capacidad: number;
  ubicacion: string;
  activo?: boolean;
}

export interface DetalleRequerimientoEvento {
  articuloId: number;
  articuloDescripcion: string;
  cantidadSolicitada: number;
}

export interface RequerimientoVinculado {
  id: number;
  codigo: string;
  estado: string;
  detalles?: DetalleRequerimientoEvento[];
}

export interface ReservaEvento {
  id: number;
  codigo: string;
  ambiente: Ambiente;
  nombreEvento: string;
  solicitante: string;
  areaSolicitante: string;
  fechaEvento: string;
  horaInicio: string;
  horaFin: string;
  estado: 'PENDIENTE' | 'APROBADO' | 'RECHAZADO';
  observaciones?: string;
  requerimiento?: RequerimientoVinculado;
  items?: ItemEventoRequest[];
}

export interface ItemEventoRequest {
  articuloId: number | null;
  cantidad: number;
}

export interface SolicitudReservaRequest {
  ambienteId: number;
  nombreEvento: string;
  solicitante: string;
  areaSolicitante: string;
  fechaEvento: string;
  horaInicio: string;
  horaFin: string;
  observaciones?: string;
  items: ItemEventoRequest[];
}

export interface CrearAmbienteRequest {
  nombre: string;
  capacidad: number;
  ubicacion: string;
}