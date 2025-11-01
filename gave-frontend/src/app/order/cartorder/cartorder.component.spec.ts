import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CartorderComponent } from './cartorder.component';

describe('CartorderComponent', () => {
  let component: CartorderComponent;
  let fixture: ComponentFixture<CartorderComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CartorderComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(CartorderComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
