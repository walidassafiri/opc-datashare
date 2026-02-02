import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../auth/auth.service';

@Component({
  selector: 'app-register',
  templateUrl: './register.component.html'
})
export class RegisterComponent {
  email = '';
  password = '';
  error = '';

  constructor(private auth: AuthService, private router: Router) {}

  submit() {
    this.error = '';
    this.auth.register(this.email, this.password).subscribe({
      next: () => this.router.navigate(['/upload']),
      error: (err) => this.error = err?.error?.message || 'Registration failed'
    });
  }
}
