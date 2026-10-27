// src/app/admin/adminusers/adminusers.component.ts
import { Component, OnInit, computed, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AdminUser, AdminUsersService } from '../../service/adminusers.service';

type Filter = 'all' | 'pending' | 'approved';

@Component({
  selector: 'app-adminusers',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './adminusers.component.html'
})
export class AdminusersComponent implements OnInit {
  users = signal<AdminUser[]>([]);
  loading = signal(true);
  error = signal<string | null>(null);
  savingId = signal<number | null>(null);

  search = signal('');
  filter = signal<Filter>('pending'); // arranca mostrando los pendientes

  filtered = computed(() => {
    const q = this.search().trim().toLowerCase();
    const f = this.filter();
    return this.users().filter(u => {
      if (f === 'pending' && u.priceAccess) return false;
      if (f === 'approved' && !u.priceAccess) return false;
      if (!q) return true;
      return u.email.toLowerCase().includes(q) || (u.fullName || '').toLowerCase().includes(q);
    });
  });

  pendingCount = computed(() => this.users().filter(u => !u.priceAccess).length);

  constructor(private api : AdminUsersService) {}

  ngOnInit(): void {
    this.load();
  }

  load() {
    this.loading.set(true);
    this.error.set(null);
    this.api.list().subscribe({
      next: us => { this.users.set(us); this.loading.set(false); },
      error: () => { this.error.set('No se pudieron cargar los usuarios.'); this.loading.set(false); }
    });
  }

  toggle(u: AdminUser) {
    const newValue = !u.priceAccess;
    this.savingId.set(u.id);
    this.api.setPriceAccess(u.id, newValue).subscribe({
      next: () => {
        this.users.update(list => list.map(x => x.id === u.id ? { ...x, priceAccess: newValue } : x));
        this.savingId.set(null);
      },
      error: () => {
        this.error.set('No se pudo actualizar el usuario.');
        this.savingId.set(null);
      }
    });
  }

  setFilter(f: Filter) { this.filter.set(f); }
}