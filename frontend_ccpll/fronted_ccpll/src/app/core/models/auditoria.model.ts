export interface RegistroAuditoria {
  id: number;
  accion: string;
  detalle: string;
  entidad: string;
  entidadId: number | null;
  fechaHora: string;
  ipOrigen: string;
  usuario: string;
}