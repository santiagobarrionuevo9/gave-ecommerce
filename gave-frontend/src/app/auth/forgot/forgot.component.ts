import { Component } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { AuthService } from '../../service/auth.service';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-forgot',
  standalone: true,
  imports: [CommonModule,ReactiveFormsModule],
  templateUrl: './forgot.component.html',
  styleUrl: './forgot.component.css'
})
export class ForgotComponent {
  form : FormGroup;
  loading = false; sent = false; errorMsg: string | null = null;

  constructor(private fb: FormBuilder, private auth: AuthService) {
    this.form = this.fb.group({ email: ['', [Validators.required, Validators.email]] });
  }

  submit() {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    this.loading = true; this.errorMsg = null;
    this.auth.forgotPassword(this.form.value as any).subscribe({
      next: () => { this.loading = false; this.sent = true; },
      error: () => { this.loading = false; this.sent = true; } // misma UX por privacidad
    });
  }
}
