import { Component, OnInit, signal } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Producttypedto } from '../../interface/product/producttypedto';
import { ProductService } from '../../service/product.service';
import { Router, RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';
import { Imageproductdto } from '../../interface/product/imageproductdto';

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

  // límites / validaciones de ejemplo
  readonly MAX_FILES = 12;
  readonly MAX_SIZE_MB = 10;

  constructor(
    private fb: FormBuilder,
    private api: ProductService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.form = this.fb.group({
      // Producto
      typeId: [null, [Validators.required]],
      name: ['', [Validators.required, Validators.maxLength(180)]],
      slug: ['', [Validators.required, Validators.maxLength(200)]],
      shortDesc: ['', [Validators.maxLength(300)]],
      description: [''],
      isActive: [true],
      sku: ['', [Validators.required, Validators.maxLength(64)]],
      price: [0, [Validators.required, Validators.min(0)]],
      stock: [0, [Validators.required, Validators.min(0)]],
      // Imágenes
      imageAlt: [''],
      imageSort: [0],
      // (opcional) URL por si no querés subir archivos
      imageUrl: ['']
    });

    this.api.getTypes().subscribe({
      next: ts => (this.types = ts),
      error: () => (this.types = [])
    });
  }

  // selección de múltiples archivos
  onFilesChange(ev: Event) {
    const input = ev.target as HTMLInputElement;
    const files = input.files ? Array.from(input.files) : [];
    if (!files.length) return;

    // acumulativo: agrego a los ya existentes
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

    // limpiar input para permitir re-selección del mismo archivo si hace falta
    input.value = '';
  }

  removePreview(idx: number) {
    this.previews.splice(idx, 1);
    this.selectedFiles.splice(idx, 1);
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
      name: v.name,
      slug: v.slug,
      shortDesc: v.shortDesc || null,
      description: v.description || null,
      isActive: !!v.isActive,
      sku: v.sku,
      price: Number(v.price),
      stock: Number(v.stock)
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
                // this.router.navigate(['/producto', created.slug]);
              },
              error: err => {
                console.error(err);
                this.loading.set(false);
                this.errorMsg = 'El producto se creó, pero falló subir alguna imagen.';
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
                // this.router.navigate(['/producto', created.slug]);
              },
              error: err => {
                console.error(err);
                this.loading.set(false);
                this.errorMsg = 'Producto ok, pero falló guardar la URL de imagen.';
              }
            });
          return;
        }

        // 3) sin imágenes
        this.loading.set(false);
        this.successMsg = 'Producto creado (sin imágenes).';
        // this.router.navigate(['/catalogo']);
      },
      error: err => {
        console.error(err);
        this.loading.set(false);
        this.errorMsg = err?.error?.message || 'Error al crear el producto.';
      }
    });
  }

  hasError(ctrl: string, err: string) {
    const c = this.form.get(ctrl);
    return !!c && c.touched && c.hasError(err);
  }
}