import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ProductbrowseComponent } from './productbrowse.component';

describe('ProductbrowseComponent', () => {
  let component: ProductbrowseComponent;
  let fixture: ComponentFixture<ProductbrowseComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ProductbrowseComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ProductbrowseComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
