export interface UsuarioPerfilResponse {
  id: number;
  username: string;
  nombreCompleto: string;
  area: string;
  rol: string;
  activo: boolean;
  fechaCreacion?: string;
  fotoUrl?: string | null
}

export interface ActualizarPerfilRequest {
  nombreCompleto: string;
  area: string;
}

export interface CambiarPasswordRequest {
  passwordActual: string;
  passwordNuevo: string;
}
