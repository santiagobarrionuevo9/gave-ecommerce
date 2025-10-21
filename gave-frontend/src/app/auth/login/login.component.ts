import { Component } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { AuthService } from '../../service/auth.service';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css'
})
export class LoginComponent {
  form : FormGroup;
  loading = false;
  errorMsg: string | null = null;

  constructor(
    private fb: FormBuilder,
    private auth: AuthService,
    private router: Router,
    private route: ActivatedRoute
  ) {
    this.form = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required]]
    });
  }

  submit() {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    this.errorMsg = null; this.loading = true;
    this.auth.login(this.form.value as any).subscribe({
      next: () => {
        this.loading = false;
        const ret = this.route.snapshot.queryParamMap.get('returnUrl') || '/';
        this.router.navigateByUrl(ret);
      },
      error: err => {
        this.loading = false;
        this.errorMsg = err?.error?.message || 'Credenciales inválidas';
      }
    });
  }
}