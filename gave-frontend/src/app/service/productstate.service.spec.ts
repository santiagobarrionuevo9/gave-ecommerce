import { TestBed } from '@angular/core/testing';

import { ProductstateService } from './productstate.service';

describe('ProductstateService', () => {
  let service: ProductstateService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(ProductstateService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
