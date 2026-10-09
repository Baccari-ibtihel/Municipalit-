import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent {
  email = '';
  password = '';
  uniqueCode = '';
  loginType: 'citizen' | 'pro' = 'citizen';
  errorMessage = '';
  loading = false;

  constructor(public auth: AuthService, private router: Router) {
    if (this.auth.isLoggedIn()) {
      this.redirectUser(this.auth.getRole());
    }
  }

  setLoginType(type: 'citizen' | 'pro'): void {
    this.loginType = type;
    this.errorMessage = '';
  }

  onSubmit(): void {
    if (!this.email || !this.password) {
      this.errorMessage = this.auth.currentLang === 'ar' 
        ? 'الرجاء إدخال البريد الإلكتروني وكلمة المرور' 
        : 'Veuillez saisir votre email et mot de passe';
      return;
    }

    if (this.loginType === 'pro' && !this.uniqueCode) {
      this.errorMessage = this.auth.currentLang === 'ar'
        ? 'الرجاء إدخال المعرف الوحيد الخاص بك'
        : 'Veuillez saisir votre identifiant unique';
      return;
    }

    this.loading = true;
    this.errorMessage = '';

    const loginData = {
      email: this.email,
      password: this.password,
      uniqueCode: this.loginType === 'pro' ? this.uniqueCode : null
    };

    this.auth.login(loginData).subscribe({
      next: (res) => {
        this.loading = false;
        this.redirectUser(res.role);
      },
      error: (err) => {
        this.loading = false;
        this.errorMessage = err.error || (this.auth.currentLang === 'ar'
          ? 'فشل الاتصال: البيانات غير صحيحة'
          : 'Échec de connexion : informations incorrectes');
        console.error(err);
      }
    });
  }

  private redirectUser(role: string | null): void {
    if (role === 'ROLE_CITIZEN') {
      this.router.navigate(['/citizen']);
    } else if (role === 'ROLE_AGENT') {
      this.router.navigate(['/agent']);
    } else if (role === 'ROLE_OFFICIANT') {
      this.router.navigate(['/officiant']);
    } else {
      this.errorMessage = 'Unknown user role';
    }
  }
}
