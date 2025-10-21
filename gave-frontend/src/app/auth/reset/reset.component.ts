import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService } from '../../service/auth.service';

@Component({
  selector: 'app-reset',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './reset.component.html',
  styleUrl: './reset.component.css'
})
export class ResetComponent {
  token : string;
  form: FormGroup;
  loading = false; done = false; errorMsg: string | null = null;

  constructor(private fb: FormBuilder, private route: ActivatedRoute, private router: Router, private auth: AuthService){
    this.token = this.route.snapshot.queryParamMap.get('token') || '';
    this.form = this.fb.group({
      newPassword: ['', [Validators.required, Validators.minLength(6)]]
    });
  }

  submit() {
    if (this.form.invalid || !this.token) { this.form.markAllAsTouched(); return; }
    this.loading = true; this.errorMsg = null;
    this.auth.resetPassword({ token: this.token, newPassword: this.form.value.newPassword! }).subscribe({
      next: () => { this.loading = false; this.done = true; },
      error: err => { this.loading = false; this.errorMsg = err?.error?.message || 'Token inválido o expirado'; }
    });
  }

  goLogin() { this.router.navigateByUrl('/login'); }
}
