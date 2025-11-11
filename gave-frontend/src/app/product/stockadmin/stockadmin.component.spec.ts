import { ComponentFixture, TestBed } from '@angular/core/testing';

import { StockadminComponent } from './stockadmin.component';

describe('StockadminComponent', () => {
  let component: StockadminComponent;
  let fixture: ComponentFixture<StockadminComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [StockadminComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(StockadminComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
