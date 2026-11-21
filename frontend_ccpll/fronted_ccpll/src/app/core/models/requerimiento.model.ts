export interface DetalleRequerimientoRequest {
  articuloId: number;
  cantidadSolicitada: number;
  caracteristicas?: string;
  sustento?: string;
}

export interface RequerimientoRequest {
  areaSolicitante: string;
  solicitante: string;
  observacion?: string;
  detalles: DetalleRequerimientoRequest[];
}

export interface DetalleRequerimientoResponse {
  id: number;
  articuloId: number;
  articuloCodigo: string;
  articuloDescripcion: string;
  skuAsignado?: string; // <--- NUEVO CAMPO (Muestra el SKU si es préstamo o "-" si no aplica)
  cantidadSolicitada: number;
  cantidadAtendida: number;
  caracteristicas?: string;
  sustento?: string;
}

// NUEVOS ESTADOS DE PRÉSTAMO Y DEVOLUCIÓN AGREGADOS AL TYPE
export type EstadoRequerimiento = 'PENDIENTE' | 'APROBADO' | 'DESPACHADO' | 'ENTREGADO' | 'DEVUELTO' | 'RECHAZADO'| 'CANCELADO';

export interface RequerimientoResponse {
  id: number;
  codigo: string;
  areaSolicitante: string;
  solicitante: string;
  observacion?: string;
  estado: EstadoRequerimiento;
  fechaSolicitud: string;
  fechaDespacho?: string;
  usuarioCreacion?: string;
  fechaCreacion?: string;
  detalles: DetalleRequerimientoResponse[];
}

// DTO PARA PETICIÓN DE DEVOLUCIÓN DESDE EL FRONTEND
export interface DevolucionActivoRequestDTO {
  requerimientoId: number;
  requerimientoDetalleId: number;
  sku?: string;
  usuarioRecibe: string;
  observaciones?: string;
}