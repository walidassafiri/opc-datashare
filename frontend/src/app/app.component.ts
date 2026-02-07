import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from './auth/auth.service';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html'
})
export class AppComponent {
  title = 'Frontend App';

  constructor(public auth: AuthService, private router: Router) {}

  isAuthenticated(): boolean { return this.auth.isAuthenticated(); }

  logout() {
    // call backend logout but always ensure local token removal and redirect
    this.auth.logout().subscribe({
      next: () => this.router.navigate(['/login']),
      error: () => { this.auth.logoutLocal(); this.router.navigate(['/login']); }
    });
  }
}
