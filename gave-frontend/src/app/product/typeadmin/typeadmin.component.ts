import { Component, OnInit, signal } from '@angular/core';
import { Producttypedto } from '../../interface/product/producttypedto';
import { Form, FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ProductService } from '../../service/product.service';
import Swal from 'sweetalert2';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'app-typeadmin',
  standalone: true,
  imports: [CommonModule,ReactiveFormsModule,RouterModule],
  templateUrl: './typeadmin.component.html',
  styleUrl: './typeadmin.component.css'
})
export class TypeadminComponent implements OnInit {
  loading = signal(false);
  types: Producttypedto[] = [];
  form!:FormGroup;
  editRow: Producttypedto | null = null;
  editForm!:FormGroup;

  constructor(private fb: FormBuilder, private api: ProductService) {
    this.form = this.fb.group({
      name: ['', Validators.required],
      slug: ['', Validators.required],
      description: ['']
    }); 
    this.editForm = this.fb.group({
      name: ['', Validators.required],
      slug: ['', Validators.required],
      description: ['']
    });
    
  }

  ngOnInit(): void { this.load(); }

  load() {
    this.loading.set(true);
    this.api.getTypes().subscribe({
      next: (t)=> { this.types = t; this.loading.set(false); },
      error: (e)=> { this.loading.set(false); Swal.fire('Error','No se pudieron cargar las categorías','error'); }
    });
  }

  create() {
    if (this.form.invalid) return;
    this.api.createType(this.form.getRawValue() as Producttypedto).subscribe({
      next: ()=>{
        Swal.fire('OK','Categoría creada','success');
        this.form.reset();
        this.load();
      },
      error: (e)=> Swal.fire('Error', e?.error?.message || 'No se pudo crear', 'error')
    });
  }

  startEdit(t: Producttypedto) {
    this.editRow = t;
    this.editForm.patchValue(t);
  }

  cancelEdit(){ this.editRow = null; }

  saveEdit() {
    if (!this.editRow) return;
    if (this.editForm.invalid) return;
    this.api.updateType(this.editRow.id!, this.editForm.getRawValue() as Producttypedto).subscribe({
      next: ()=> { Swal.fire('OK','Guardado','success'); this.editRow = null; this.load(); },
      error: (e)=> Swal.fire('Error', e?.error?.message || 'No se pudo guardar', 'error')
    });
  }

  remove(t: Producttypedto) {
    Swal.fire({title:'Eliminar', text:`¿Eliminar "${t.name}"?`, icon:'warning', showCancelButton:true})
      .then(r=>{
        if (r.isConfirmed) {
          this.api.deleteType(t.id!).subscribe({
            next: ()=> { Swal.fire('OK','Eliminado','success'); this.load(); },
            error: (e)=> Swal.fire('Error', e?.error?.message || 'No se pudo eliminar', 'error')
          });
        }
      });
  }
}