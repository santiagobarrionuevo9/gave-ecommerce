import { Component } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ProductService } from '../../service/product.service';
import Swal from 'sweetalert2';
import { finalize } from 'rxjs';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-descuentos',
  standalone: true,
  imports: [  ReactiveFormsModule ,CommonModule ],
  templateUrl: './descuentos.component.html',
  styleUrl: './descuentos.component.css'
})
export class DescuentosComponent {
loading = false;

  form!: FormGroup;

  constructor(private fb: FormBuilder, private productService: ProductService) {
    this.form = this.fb.group({
      keyword: ['', [Validators.required, Validators.minLength(2)]],
      discountThreshold: [null as number | null], // null = no tocar
      discountPercent: [null as number | null],   // null = no tocar
      activeOnly: [true],
    });
  }

  private getNumberOrNull(v: any): number | null {
    if (v === '' || v === undefined || v === null) return null;
    const n = Number(v);
    return Number.isFinite(n) ? n : null;
  }

  submit() {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const keyword = (this.form.value.keyword || '').trim();
    const discountThreshold = this.getNumberOrNull(this.form.value.discountThreshold);
    const discountPercent = this.getNumberOrNull(this.form.value.discountPercent);
    const activeOnly = !!this.form.value.activeOnly;

    // Validaciones front
    if (!keyword || keyword.length < 2) {
      Swal.fire('Error', 'Keyword inválido (mínimo 2 caracteres).', 'error');
      return;
    }
    if (discountThreshold !== null && discountThreshold < 0) {
      Swal.fire('Error', 'El umbral (cantidad) no puede ser negativo.', 'error');
      return;
    }
    if (discountPercent !== null && (discountPercent < 0 || discountPercent > 100)) {
      Swal.fire('Error', 'El % de descuento debe estar entre 0 y 100.', 'error');
      return;
    }
    if (discountThreshold === null && discountPercent === null) {
      Swal.fire('Error', 'Tenés que completar al menos un campo a modificar (umbral o %).', 'error');
      return;
    }

    const payload = {
      keyword,
      discountThreshold,
      discountPercent,
      activeOnly,
    };

    // Confirmación
    Swal.fire({
      title: 'Confirmar actualización masiva',
      html: `
        <div style="text-align:left">
          <div><b>Keyword:</b> ${payload.keyword}</div>
          <div><b>Solo activos:</b> ${payload.activeOnly ? 'Sí' : 'No'}</div>
          <hr/>
          <div><b>Umbral:</b> ${payload.discountThreshold ?? '(sin cambios)'}</div>
          <div><b>% Descuento:</b> ${payload.discountPercent ?? '(sin cambios)'}</div>
        </div>
      `,
      icon: 'warning',
      showCancelButton: true,
      confirmButtonText: 'Aplicar',
      cancelButtonText: 'Cancelar',
    }).then(r => {
      if (!r.isConfirmed) return;

      this.loading = true;
      this.productService.bulkDiscountByName(payload)
        .pipe(finalize(() => (this.loading = false)))
        .subscribe({
          next: (res) => {
            Swal.fire('Listo', `Se actualizaron ${res.updatedCount} productos para "${res.keyword}".`, 'success');
            // opcional: reset parcial
            // this.form.patchValue({ discountThreshold: null, discountPercent: null });
          },
          error: (err) => {
            const msg =
              err?.error?.message ||
              err?.error?.error ||
              'No se pudo aplicar la actualización masiva.';
            Swal.fire('Error', msg, 'error');
          }
        });
    });
  }
}
