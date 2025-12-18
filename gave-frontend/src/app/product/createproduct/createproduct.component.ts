import { Component, OnInit, signal } from '@angular/core';
import { AbstractControl, FormBuilder, FormGroup, ReactiveFormsModule, ValidationErrors, ValidatorFn, Validators } from '@angular/forms';
import { Producttypedto } from '../../interface/product/producttypedto';
import { ProductService } from '../../service/product.service';
import { Router, RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';
import { Imageproductdto } from '../../interface/product/imageproductdto';
import Swal from 'sweetalert2';

type PreviewItem = { file: File; src: string };

@Component({
  selector: 'app-createproduct',
  standalone: true,
  imports: [ReactiveFormsModule,RouterModule,CommonModule],
  templateUrl: './createproduct.component.html',
  styleUrl: './createproduct.component.css'
})
export class CreateproductComponent implements OnInit {
  form!: FormGroup;
  types: Producttypedto[] = [];

  loading = signal(false);
  successMsg: string | null = null;
  errorMsg: string | null = null;

  // múltiples archivos seleccionados
  selectedFiles: File[] = [];
  previews: PreviewItem[] = [];

  // límites
  readonly MAX_FILES = 12;
  readonly MAX_SIZE_MB = 10;

  showDiscountConfig = false;

  constructor(
    private fb: FormBuilder,
    private api: ProductService,
    private router: Router
  ) {}

  // ✅ Slug: minúsculas, números y guiones (sin espacios)
  private slugValidator(): ValidatorFn {
    const re = /^[a-z0-9]+(?:-[a-z0-9]+)*$/;
    return (control: AbstractControl): ValidationErrors | null => {
      const v = String(control.value ?? '').trim();
      if (!v) return null;
      return re.test(v) ? null : { slugFormat: true };
    };
  }

  // ✅ SKU: letras/números/guiones/underscore/punto (sin espacios)
  private skuValidator(): ValidatorFn {
    const re = /^[A-Za-z0-9._-]+$/;
    return (control: AbstractControl): ValidationErrors | null => {
      const v = String(control.value ?? '').trim();
      if (!v) return null;
      return re.test(v) ? null : { skuFormat: true };
    };
  }

  // ✅ Entero
  private integerValidator(): ValidatorFn {
    return (control: AbstractControl): ValidationErrors | null => {
      const v = control.value;
      if (v === null || v === undefined || v === '') return null;
      return Number.isInteger(Number(v)) ? null : { integer: true };
    };
  }

  // ✅ Semáforo: low <= medium
  private stockThresholdsValidator(): ValidatorFn {
    return (group: AbstractControl): ValidationErrors | null => {
      const low = group.get('stockLowThreshold')?.value;
      const med = group.get('stockMediumThreshold')?.value;

      if (low === null || low === '' || med === null || med === '') return null;

      const lowN = Number(low);
      const medN = Number(med);

      if (Number.isNaN(lowN) || Number.isNaN(medN)) return null;

      return lowN <= medN ? null : { thresholdsOrder: true };
    };
  }

  // ✅ Descuento: si uno está, el otro es obligatorio
  private discountPairValidator(): ValidatorFn {
    return (group: AbstractControl): ValidationErrors | null => {
      const th = group.get('discountThreshold')?.value;
      const pct = group.get('discountPercent')?.value;

      const hasTh = th !== null && th !== '' && th !== undefined;
      const hasPct = pct !== null && pct !== '' && pct !== undefined;

      if (!hasTh && !hasPct) return null;

      if (hasTh && !hasPct) return { discountPercentRequired: true };
      if (!hasTh && hasPct) return { discountThresholdRequired: true };

      return null;
    };
  }

  ngOnInit(): void {
    this.form = this.fb.group(
      {
        // Producto
        typeId: [null, [Validators.required]],
        name: ['', [Validators.required, Validators.maxLength(180)]],
        slug: ['', [Validators.required, Validators.maxLength(200), this.slugValidator()]],
        shortDesc: ['', [Validators.maxLength(300)]],
        description: [''],
        isActive: [true],

        sku: ['', [Validators.required, Validators.maxLength(64), this.skuValidator()]],

        // precio > 0 (si querés permitir 0, cambiá min(0.01) por min(0))
        price: [0, [Validators.required, Validators.min(0.01)]],

        // stock entero
        stock: [0, [Validators.required, Validators.min(0), this.integerValidator()]],

        // semáforo
        stockLowThreshold: [5, [Validators.required, Validators.min(0), this.integerValidator()]],
        stockMediumThreshold: [15, [Validators.required, Validators.min(0), this.integerValidator()]],

        // descuentos
        discountThreshold: [null, [Validators.min(1), this.integerValidator()]],
        discountPercent: [null, [Validators.min(0), Validators.max(100)]],

        // imágenes
        imageAlt: ['', [Validators.maxLength(140)]],
        imageSort: [0, [Validators.min(0), this.integerValidator()]],
        imageUrl: ['', [Validators.pattern(/^(https?:\/\/).+/i)]]
      },
      {
        validators: [this.stockThresholdsValidator(), this.discountPairValidator()]
      }
    );

    // 🔒 SKU siempre en MAYÚSCULAS
    this.form.get('sku')?.valueChanges.subscribe(value => {
      if (!value) return;

      const upper = String(value).toUpperCase();

      if (value !== upper) {
        this.form.get('sku')?.setValue(upper, { emitEvent: false });
      }
    });

    this.api.getTypes().subscribe({
      next: ts => (this.types = ts),
      error: () => (this.types = [])
    });
  }

  toggleDiscountConfig() {
    this.showDiscountConfig = !this.showDiscountConfig;
    if (this.showDiscountConfig) {
      this.form.get('discountThreshold')?.markAsTouched();
      this.form.get('discountPercent')?.markAsTouched();
    }
  }

  // selección de múltiples archivos
  onFilesChange(ev: Event) {
    const input = ev.target as HTMLInputElement;
    const files = input.files ? Array.from(input.files) : [];
    if (!files.length) return;

    const total = this.selectedFiles.length + files.length;
    if (total > this.MAX_FILES) {
      this.errorMsg = `Podés subir hasta ${this.MAX_FILES} imágenes.`;
      return;
    }

    for (const f of files) {
      const sizeMB = f.size / (1024 * 1024);
      if (!f.type.startsWith('image/')) {
        this.errorMsg = 'Alguno de los archivos no es imagen.';
        continue;
      }
      if (sizeMB > this.MAX_SIZE_MB) {
        this.errorMsg = `Alguna imagen excede ${this.MAX_SIZE_MB} MB.`;
        continue;
      }

      this.selectedFiles.push(f);

      const reader = new FileReader();
      reader.onload = () => {
        this.previews.push({ file: f, src: reader.result as string });
      };
      reader.readAsDataURL(f);
    }

    input.value = '';
  }

  removePreview(idx: number) {
    this.previews.splice(idx, 1);
    this.selectedFiles.splice(idx, 1);
  }

  async showSuccessAndGoToStock(message: string) {
    await Swal.fire({
      icon: 'success',
      title: 'Producto creado',
      text: message,
      confirmButtonText: 'Ir al stock'
    });

    this.router.navigate(['/admin/stock']);
  }

  submit(): void {
    this.successMsg = this.errorMsg = null;

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.loading.set(true);
    const v = this.form.getRawValue();

    const payload = {
    typeId: Number(v.typeId),
    name: String(v.name).trim(),
    slug: String(v.slug).trim(),
    sku: String(v.sku).trim(),

    shortDesc: (v.shortDesc ?? '').toString().trim() || null,
    description: (v.description ?? '').toString().trim() || null,
    isActive: Boolean(v.isActive),

    price: Number(v.price),
    stock: Number(v.stock),

    stockLowThreshold: v.stockLowThreshold !== null && v.stockLowThreshold !== ''
      ? Number(v.stockLowThreshold)
      : null,

    stockMediumThreshold: v.stockMediumThreshold !== null && v.stockMediumThreshold !== ''
      ? Number(v.stockMediumThreshold)
      : null,

    discountThreshold: v.discountThreshold !== null && v.discountThreshold !== ''
      ? Number(v.discountThreshold)
      : null,

    discountPercent: v.discountPercent !== null && v.discountPercent !== ''
      ? Number(v.discountPercent)
      : null,
  };


    this.api.createProduct(payload).subscribe({
      next: created => {
        const hasFiles = this.selectedFiles.length > 0;
        const hasUrl = !!v.imageUrl && String(v.imageUrl).trim().length > 0;

        // 1) múltiples archivos
        if (hasFiles) {
          this.api
            .uploadImages(
              created.id,
              this.selectedFiles,
              v.imageAlt || '',
              Number(v.imageSort ?? 0)
            )
            .subscribe({
              next: (imgs: Imageproductdto[]) => {
                this.loading.set(false);
                this.successMsg = `Producto creado con ${imgs.length} imagen(es).`;
                this.showSuccessAndGoToStock(
                  `Se creó el producto "${created.name}" con ${imgs.length} imagen(es).`
                );
              },
              error: err => {
                console.error(err);
                this.loading.set(false);
                this.errorMsg = 'El producto se creó, pero falló subir alguna imagen.';
                Swal.fire({
                  icon: 'warning',
                  title: 'Producto creado con advertencias',
                  text: 'El producto se creó, pero hubo errores al subir las imágenes.'
                });
              }
            });
          return;
        }

        // 2) sin archivos, pero con URL
        if (hasUrl) {
          this.api
            .addImage(created.id, {
              url: v.imageUrl,
              altText: v.imageAlt || '',
              sortOrder: Number(v.imageSort ?? 0)
            })
            .subscribe({
              next: () => {
                this.loading.set(false);
                this.successMsg = 'Producto creado y URL de imagen guardada.';
                this.showSuccessAndGoToStock(
                  `Se creó el producto "${created.name}" y se guardó la URL de la imagen.`
                );
              },
              error: err => {
                console.error(err);
                this.loading.set(false);
                this.errorMsg = 'Producto ok, pero falló guardar la URL de imagen.';
                Swal.fire({
                  icon: 'warning',
                  title: 'Producto creado con advertencias',
                  text: 'El producto se creó, pero no se pudo guardar la URL de la imagen.'
                });
              }
            });
          return;
        }

        // 3) sin imágenes
        this.loading.set(false);
        this.successMsg = 'Producto creado (sin imágenes).';
        this.showSuccessAndGoToStock(`Se creó el producto "${created.name}" (sin imágenes).`);
      },
      error: err => {
        console.error(err);
        this.loading.set(false);
        const msg = err?.error?.message || 'Error al crear el producto.';
        this.errorMsg = msg;
        Swal.fire({
          icon: 'error',
          title: 'No se pudo crear el producto',
          text: msg
        });
      }
    });
  }

  hasError(ctrl: string, err: string) {
    const c = this.form.get(ctrl);
    return !!c && c.touched && c.hasError(err);
  }
}