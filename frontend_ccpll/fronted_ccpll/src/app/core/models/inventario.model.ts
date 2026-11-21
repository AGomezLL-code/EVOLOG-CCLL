export interface ActivoSerieDTO {
  id: number;
  sku: string;
  estado: 'DISPONIBLE' | 'PRESTADO' | 'EN_MANTENIMIENTO';
}

export interface InventarioResponseDTO {
  id: number;
  codigo: string;
  descripcion: string;
  unidad: string;
  stockActual: number;
  costoPromedio: number;
  stockMinimo: number;
  version: number;
  categoriaId: number;
  categoriaNombre: string;
  activo: boolean;
  requiereSku: boolean;
  ubicacionFisica?: string;
  fechaCreacion?: string;
}

export interface InventarioRequestDTO {
  codigo: string;
  descripcion: string;
  unidad: string;
  stockMinimo: number;
  categoriaId: number;
  activo?: boolean;
  requiereSku: boolean;
  ubicacionFisica?: string;
}

export interface EntradaInventarioRequestDTO {
  articuloId: number;
  sku?: string;
  cantidad: number;
  costoUnitario: number;
  fecha: string;
  proveedor?: string;
  areaDestino?: string;
}

export interface SalidaInventarioRequestDTO {
  articuloId: number;
  sku?: string;
  cantidad: number;
  fecha: string;
  cliente?: string;
  areaSolicita?: string;
}

export interface HistoriaInventarioResponseDTO {
  id: number;
  articuloId: number;
  articuloCodigo: string;
  articuloDescripcion: string;
  skuAsignado?: string;
  tipo: 'ENTRADA' | 'SALIDA' | 'PRESTAMO' | 'DEVOLUCION';
  cantidad: number;
  costoUnitario: number;
  subtotal: number;
  fecha: string;
  proveedorCliente?: string;
  area?: string;
  stockAnterior: number;
  stockNuevo: number;
  fechaCreacion?: string;
}

export interface ResumenInventarioDTO {
  totalProductos: number;
  valorTotalInventario: number;
  cantidadStockBajo: number;
}