import { TestBed } from '@angular/core/testing';

import { HistorialInventario } from './historial-inventario';

describe('HistorialInventario', () => {
  let service: HistorialInventario;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(HistorialInventario);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
