import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RequerimientoList } from './requerimiento-list';

describe('RequerimientoList', () => {
  let component: RequerimientoList;
  let fixture: ComponentFixture<RequerimientoList>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [RequerimientoList],
    }).compileComponents();

    fixture = TestBed.createComponent(RequerimientoList);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
