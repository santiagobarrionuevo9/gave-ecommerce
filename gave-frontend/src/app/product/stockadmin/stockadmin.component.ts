import { Component, OnInit, signal } from '@angular/core';
import { Productdto } from '../../interface/product/productdto';
import { Pages } from '../../interface/product/pages';
import { FormBuilder, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { ProductService, SearchParams } from '../../service/product.service';
import Swal from 'sweetalert2';
import { StockChangeDTO } from '../../interface/product/stockchangedto';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';

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

  constructor(private fb: FormBuilder, private api: ProductService) {
    this.form = this.fb.group({
      q: [''],
      size: [12],
      sort: ['name,asc']
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

}