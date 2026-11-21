export interface DashboardKpiDTO {
  totalArticulos: number;
  valorTotalInventario: number;
  articulosBajoStock: number;
}

export interface ValorCategoriaDTO {
  categoria: string;
  valor_total: number;
}

export interface MovimientoRecienteDTO {
  id: number;
  fecha: string;
  codigo: string;
  descripcion: string;
  tipo: string;
  cantidad: number;
  costo_unitario: number;
  subtotal: number;
  area: string;
  usuario_registra: string;
}