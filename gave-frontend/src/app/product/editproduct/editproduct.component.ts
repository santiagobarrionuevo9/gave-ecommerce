import { Component, OnInit, signal } from '@angular/core';
import { FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { Producttypedto } from '../../interface/product/producttypedto';
import { ProductService } from '../../service/product.service';
import { Createproductdto } from '../../interface/product/createproductdto';
import { CommonModule } from '@angular/common';
import { Imageproductdto } from '../../interface/product/imageproductdto';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { ProductstateService } from '../../service/productstate.service';
import Swal from 'sweetalert2';
import { finalize } from 'rxjs';

type ImageRow = Imageproductdto & { _editing?: boolean; _alt?: string; _sort?: number };
@Component({
  selector: 'app-editproduct',
  standalone: true,
  imports: [ReactiveFormsModule,CommonModule, FormsModule,RouterModule],
  templateUrl: './editproduct.component.html',
  styleUrl: './editproduct.component.css'
})
export class EditproductComponent implements OnInit {
  loadForm!: FormGroup;
  form!: FormGroup;

  loading = signal(false);
  saving = signal(false);
  imgLoading = signal(false);

  types: Producttypedto[] = [];
  successMsg: string | null = null;
  errorMsg: string | null = null;

  currentId: number | null = null;

  images: ImageRow[] = [];
  selectedFiles: File[] = [];
  previews: string[] = [];
  readonly MAX_FILES = 20;
  readonly MAX_MB = 10;

  showDiscountConfig = false;

  private readonly filesHost = 'http://localhost:9011';

  constructor(
    private fb: FormBuilder,
    private api: ProductService,
    private route: ActivatedRoute,
    private productState: ProductstateService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadForm = this.fb.group({
      id: [null, [Validators.required, Validators.min(1)]]
    });

    this.form = this.fb.group({
      typeId: [null, [Validators.required]],
      name: ['', [Validators.required, Validators.maxLength(180)]],
      slug: ['', [Validators.required, Validators.maxLength(200)]],
      shortDesc: ['', [Validators.maxLength(300)]],
      description: [''],
      isActive: [true],

      sku: ['', [Validators.required, Validators.maxLength(64)]],
      price: [0, [Validators.required, Validators.min(0)]],
      stock: [0, [Validators.required, Validators.min(0)]],

      stockLowThreshold: [5, [Validators.min(0)]],
      stockMediumThreshold: [15, [Validators.min(0)]],

      discountThreshold: [null],
      discountPercent: [null],

    });

    this.api.getTypes().subscribe({
      next: ts => (this.types = ts),
      error: () => (this.types = [])
    });

    const paramId = this.route.snapshot.paramMap.get('id');
    if (paramId) {
      const idNum = Number(paramId);
      if (!isNaN(idNum) && idNum > 0) {
        this.loadForm.patchValue({ id: idNum });
        this.loadById();
      }
    }
  }

  loadById(): void {
    this.successMsg = this.errorMsg = null;

    if (this.loadForm.invalid) {
      this.loadForm.markAllAsTouched();
      return;
    }

    const id = Number(this.loadForm.value.id);
    this.loading.set(true);

    this.api.getProductById(id)
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe({
        next: (p) => {
          this.currentId = p.id;

          this.form.reset({
            typeId: p.typeId,
            name: p.name ?? '',
            slug: p.slug ?? '',
            sku: p.sku ?? '',
            price: p.price ?? 0,
            stock: p.stock ?? 0,

            stockLowThreshold: p.stockLowThreshold ?? 0,
            stockMediumThreshold: p.stockMediumThreshold ?? 0,

            discountThreshold: p.discountThreshold ?? null,
            discountPercent: p.discountPercent ?? null,

            isActive: p.isActive ?? true,
            shortDesc: p.shortDesc ?? '',
            description: p.description ?? '',
          });

          this.fetchImages();
        },
        error: (err) => {
          console.error(err);
          this.currentId = null;
          this.images = [];
          this.errorMsg = 'No se pudo cargar el producto. Verificá el ID.';
        }
      });
  }

  save(): void {
    // DEBUG fuerte para que veas si entra o si se frena por invalid
    console.log('SAVE CLICK', {
      currentId: this.currentId,
      saving: this.saving(),
      invalid: this.form.invalid,
      status: this.form.status
    });
    console.log('CONTROL ERRORS', Object.keys(this.form.controls).map(k => ({
      k,
      valid: this.form.get(k)?.valid,
      errors: this.form.get(k)?.errors
    })));

    this.successMsg = this.errorMsg = null;

    if (this.saving()) return; // evita doble submit

    if (this.currentId == null) {
      this.errorMsg = 'Primero cargá un producto por ID.';
      return;
    }

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      this.errorMsg = 'Revisá los campos marcados en rojo.';
      return;
    }

    const v = this.form.getRawValue();

    const payload: Createproductdto = {
      typeId: Number(v.typeId),
      name: String(v.name ?? '').trim(),
      slug: String(v.slug ?? '').trim(),
      shortDesc: String(v.shortDesc ?? '').trim() || null,
      description: String(v.description ?? '').trim() || null,
      isActive: !!v.isActive,
      sku: String(v.sku ?? '').trim(),
      price: Number(v.price),
      stock: Number(v.stock),

      stockLowThreshold:
        v.stockLowThreshold !== null && v.stockLowThreshold !== ''
          ? Number(v.stockLowThreshold)
          : null,

      stockMediumThreshold:
        v.stockMediumThreshold !== null && v.stockMediumThreshold !== ''
          ? Number(v.stockMediumThreshold)
          : null,

      discountThreshold:
        v.discountThreshold !== null && v.discountThreshold !== ''
          ? Number(v.discountThreshold)
          : null,

      discountPercent:
        v.discountPercent !== null && v.discountPercent !== ''
          ? Number(v.discountPercent)
          : null,
    };

    console.log('UPDATE PAYLOAD', payload);

    this.saving.set(true);

    this.api.updateProduct(this.currentId, payload)
      .pipe(finalize(() => this.saving.set(false)))
      .subscribe({
        next: (updated) => {
          this.successMsg = `Producto #${updated.id} actualizado correctamente.`;
          this.productState.bump();

          Swal.fire({
            icon: 'success',
            title: 'Producto actualizado',
            text: `Se actualizaron los datos del producto "${updated.name}".`,
            confirmButtonText: 'Ir al stock'
          }).then(res => {
            if (res.isConfirmed) {
              this.router.navigate(['/admin/stock']);
            }
          });
        },
        error: (err) => {
          console.error(err);
          const msg = err?.error?.message || 'Error al actualizar el producto.';
          this.errorMsg = msg;

          Swal.fire({
            icon: 'error',
            title: 'No se pudo actualizar el producto',
            text: msg
          });
        }
      });
  }

  // ===================== IMÁGENES =====================

  fetchImages(): void {
    if (this.currentId == null) return;

    this.imgLoading.set(true);
    this.api.getImages(this.currentId)
      .pipe(finalize(() => this.imgLoading.set(false)))
      .subscribe({
        next: list => {
          this.images = (list || []).map(img => ({
            ...img,
            _editing: false,
            _alt: img.altText ?? '',
            _sort: img.sortOrder ?? 0
          }));
        },
        error: e => {
          console.error(e);
          this.images = [];
        }
      });
  }

  startEdit(row: ImageRow) {
    row._editing = true;
    row._alt = row.altText ?? '';
    row._sort = row.sortOrder ?? 0;
  }

  cancelEdit(row: ImageRow) {
    row._editing = false;
    row._alt = row.altText ?? '';
    row._sort = row.sortOrder ?? 0;
  }

  saveImage(row: ImageRow) {
    this.api.updateImage(row.id, {
      altText: row._alt ?? '',
      sortOrder: Number(row._sort ?? 0)
    }).subscribe({
      next: (updated) => {
        row.altText = updated.altText;
        row.sortOrder = updated.sortOrder;
        row._editing = false;
        this.successMsg = 'Imagen actualizada.';
      },
      error: (e) => {
        console.error(e);
        this.errorMsg = 'No se pudo actualizar la imagen.';
      }
    });
  }

  deleteImage(row: ImageRow) {
    if (!confirm('¿Eliminar esta imagen?')) return;

    this.api.deleteImage(row.id).subscribe({
      next: () => {
        this.images = this.images.filter(i => i.id !== row.id);
        this.successMsg = 'Imagen eliminada.';
      },
      error: (e) => {
        console.error(e);
        this.errorMsg = 'No se pudo eliminar la imagen.';
      }
    });
  }

  onFilesChange(ev: Event) {
    const input = ev.target as HTMLInputElement;
    const files = input.files ? Array.from(input.files) : [];
    if (!files.length) return;

    const total = this.selectedFiles.length + files.length;
    if (total > this.MAX_FILES) {
      this.errorMsg = `Máximo ${this.MAX_FILES} imágenes por tanda.`;
      return;
    }

    files.forEach(f => {
      const sizeMB = f.size / (1024 * 1024);
      if (!f.type.startsWith('image/')) return;
      if (sizeMB > this.MAX_MB) return;

      this.selectedFiles.push(f);

      const reader = new FileReader();
      reader.onload = () => this.previews.push(reader.result as string);
      reader.readAsDataURL(f);
    });

    input.value = '';
  }

  clearUploads() {
    this.selectedFiles = [];
    this.previews = [];
  }

  uploadSelected() {
    if (this.currentId == null) {
      this.errorMsg = 'Primero cargá el producto.';
      return;
    }
    if (!this.selectedFiles.length) return;

    const sortStart = Math.max(0, ...this.images.map(i => i.sortOrder ?? 0), 0) + 1;

    this.imgLoading.set(true);
    this.api.uploadImages(this.currentId, this.selectedFiles, '', sortStart)
      .pipe(finalize(() => this.imgLoading.set(false)))
      .subscribe({
        next: (created) => {
          this.successMsg = `Se subieron ${created.length} imagen(es).`;
          this.clearUploads();
          this.fetchImages();
        },
        error: e => {
          console.error(e);
          this.errorMsg = 'Error al subir imágenes.';
        }
      });
  }

  abs(url?: string) {
    if (!url) return 'https://via.placeholder.com/300x200?text=Sin+imagen';
    return url.startsWith('http') ? url : `${this.filesHost}${url}`;
  }
}