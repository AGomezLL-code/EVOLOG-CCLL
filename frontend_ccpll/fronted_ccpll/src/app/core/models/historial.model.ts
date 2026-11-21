export interface HistoriaInventarioResponseDTO {
  id: number;
  articuloId: number;
  articuloCodigo: string;
  articuloDescripcion: string;
  tipo: 'ENTRADA' | 'SALIDA';
  cantidad: number;
  costoUnitario: number;
  subtotal: number;
  fecha: string;
  proveedorCliente: string;
  area: string;
  stockAnterior: number;
  stockNuevo: number;
  fechaCreacion: string;
}