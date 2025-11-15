import { computed, Injectable, signal } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class ProductstateService {

  private _version = signal(0);

  // por si querés mostrar el número o sólo saber que cambió
  readonly version = computed(() => this._version());

  bump(): void {
    this._version.update(v => v + 1);
  }
}
