import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AdminorderstatusComponent } from './adminorderstatus.component';

describe('AdminorderstatusComponent', () => {
  let component: AdminorderstatusComponent;
  let fixture: ComponentFixture<AdminorderstatusComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AdminorderstatusComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AdminorderstatusComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
