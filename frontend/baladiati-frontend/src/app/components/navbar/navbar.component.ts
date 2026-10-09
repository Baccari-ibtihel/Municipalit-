import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './navbar.component.html',
  styleUrls: ['./navbar.component.css']
})
export class NavbarComponent {
  constructor(public auth: AuthService, private router: Router) {}

  toggleLang(): void {
    this.auth.toggleLanguage();
    // Reload to apply direction/language change dynamically
    window.location.reload();
  }

  logout(): void {
    this.auth.logout();
    this.router.navigate(['/login']);
  }
}
