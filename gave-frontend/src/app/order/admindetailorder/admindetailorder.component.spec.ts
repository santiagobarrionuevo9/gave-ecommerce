import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AdmindetailorderComponent } from './admindetailorder.component';

describe('AdmindetailorderComponent', () => {
  let component: AdmindetailorderComponent;
  let fixture: ComponentFixture<AdmindetailorderComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AdmindetailorderComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AdmindetailorderComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
