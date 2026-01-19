import { Component, OnInit, signal } from '@angular/core';
import { Productdto } from '../../interface/product/productdto';
import { Pages } from '../../interface/product/pages';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ProductService, SearchParams } from '../../service/product.service';
import Swal from 'sweetalert2';
import { StockChangeDTO } from '../../interface/product/stockchangedto';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { BulkPriceIncreaseByNameResponse } from '../../interface/product/BulkPriceIncreaseByNameResponse';
import { BulkPriceIncreaseByNameRequest } from '../../interface/product/BulkPriceIncreaseByNameRequest';

@Component({
  selector: 'app-stockadmin',
  standalone: true,
  imports: [CommonModule,ReactiveFormsModule,RouterModule],
  templateUrl: './stockadmin.component.html',
  styleUrl: './stockadmin.component.css'
})
export class StockadminComponent implements OnInit {
  form!: FormGroup;
  page?: Pages<Productdto>;
  loading = signal(false);
  deltas: Record<number, number> = {};   // para +/- cantidad
  sets:   Record<number, number> = {};   // para SET absoluto
  priceModalOpen = signal(false);
  priceForm!: FormGroup;


  constructor(private fb: FormBuilder, private api: ProductService) {
    this.form = this.fb.group({
      q: [''],
      size: [12],
      sort: ['name,asc']
    });

    this.priceForm = this.fb.group({
      keyword: ['', [Validators.required, Validators.minLength(2)]],
      percent: [10, [Validators.required, Validators.min(0.01)]],
      activeOnly: [true]
    });
  }


  ngOnInit(): void { this.search(); this.form.valueChanges.subscribe(()=> this.search()); }

  search(page = 0) {
    this.loading.set(true);
    const v = this.form.getRawValue();
    const params: SearchParams = {
      q: v.q || '',
      active: null,
      page,
      size: Number(v.size || 12),
      sort: v.sort || 'name,asc'
    };
    this.api.searchProducts(params).subscribe({
      next: (p)=> { this.page = p; this.loading.set(false); },
      error: (e)=> { this.loading.set(false); Swal.fire('Error', e?.error?.message || 'No se pudo cargar', 'error'); }
    });
  }

  goTo(page: number){ if (!this.page) return; if (page<0 || page>=this.page.totalPages) return; this.search(page); }

  setDelta(id: number, v: string){ this.deltas[id] = Math.max(0, Number(v)||0); }
  setSet(id: number, v: string){ this.sets[id]   = Math.max(0, Number(v)||0); }

  doUpdate(p: Productdto, op: StockChangeDTO['operation']) {
  let amount = 0;
  if (op === 'SET') amount = this.sets[p.id] ?? 0;
  else amount = this.deltas[p.id] ?? 0;

  if (amount <= 0) {
    Swal.fire('Atención', 'Ingresá un valor mayor a 0', 'warning');
    return;
  }

  const actionText =
    op === 'INCREMENT' ? 'sumar' :
    op === 'DECREMENT' ? 'restar' :
    'establecer';

  const opLabel =
    op === 'INCREMENT' ? 'INCREMENTAR' :
    op === 'DECREMENT' ? 'DECREMENTAR' :
    'SET';

  // Confirmación antes de aplicar el cambio
  Swal.fire({
    title: 'Confirmar actualización',
    html: `
      Vas a <b>${actionText}</b> <b>${amount}</b> unidad(es)<br>
      del producto <b>${p.name}</b><br>
      <span class="text-muted">Stock actual: ${p.stock}</span>
    `,
    icon: 'question',
    showCancelButton: true,
    confirmButtonText: 'Sí, actualizar',
    cancelButtonText: 'Cancelar',
    confirmButtonColor: '#0f4c5c'
  }).then(result => {
    if (!result.isConfirmed) return;

    const body: StockChangeDTO = { operation: op, amount };

    this.api.updateStock(p.id, body).subscribe({
      next: (prod) => {
        const msg =
          op === 'SET'
            ? `Stock establecido en ${prod.stock}.`
            : `Stock actualizado: ${prod.stock}.`;

        Swal.fire('Actualizado', msg, 'success');
        this.search(this.page?.number || 0);
      },
      error: (e) => {
        Swal.fire(
          'Error',
          e?.error?.message || 'No se pudo actualizar',
          'error'
        );
      }
    });
  });
}
openPriceModal() {
  this.priceModalOpen.set(true);
  this.priceForm.reset({ keyword: '', percent: 10, activeOnly: true });
}

closePriceModal() {
  this.priceModalOpen.set(false);
}

applyBulkPriceIncrease() {
  if (this.priceForm.invalid) {
    this.priceForm.markAllAsTouched();
    Swal.fire('Atención', 'Completá correctamente el formulario', 'warning');
    return;
  }

  const v = this.priceForm.getRawValue();
  const payload: BulkPriceIncreaseByNameRequest = {
    keyword: String(v.keyword || '').trim(),
    percent: Number(v.percent),
    activeOnly: !!v.activeOnly
  };

  if (!payload.keyword) {
    Swal.fire('Atención', 'Ingresá una marca / texto', 'warning');
    return;
  }
  if (payload.percent <= 0) {
    Swal.fire('Atención', 'El porcentaje debe ser mayor a 0', 'warning');
    return;
  }

  Swal.fire({
    title: 'Confirmar aumento masivo',
    html: `
      Vas a aumentar <b>${payload.percent}%</b> a todos los productos cuyo nombre contenga:
      <br><br>
      <code>${payload.keyword}</code>
      <br><br>
      <span class="text-muted">No distingue mayúsculas/minúsculas.</span>
    `,
    icon: 'question',
    showCancelButton: true,
    confirmButtonText: 'Sí, aplicar',
    cancelButtonText: 'Cancelar',
    confirmButtonColor: '#0f4c5c'
  }).then(r => {
    if (!r.isConfirmed) return;

    this.api.bulkIncreasePriceByName(payload).subscribe({
      next: (res: BulkPriceIncreaseByNameResponse) => {
        Swal.fire(
          'Aplicado',
          `Se actualizaron ${res.updatedCount} producto(s) con "${res.keyword}" (+${res.percent}%).`,
          'success'
        );
        this.closePriceModal();
        this.search(this.page?.number || 0);
      },
      error: (e) => {
        Swal.fire('Error', e?.error?.message || 'No se pudo aplicar el aumento', 'error');
      }
    });
  });
}


}