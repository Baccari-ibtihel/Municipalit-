import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.css']
})
export class RegisterComponent {
  name = '';
  email = '';
  password = '';
  phone = '';
  nationalId = '';
  
  errorMessage = '';
  successMessage = '';
  loading = false;

  constructor(public auth: AuthService, private router: Router) {}

  onSubmit(): void {
    if (!this.name || !this.email || !this.password || !this.phone || !this.nationalId) {
      this.errorMessage = this.auth.currentLang === 'ar'
        ? 'الرجاء ملء جميع الحقول المطلوبة'
        : 'Veuillez remplir tous les champs obligatoires';
      return;
    }

    this.loading = true;
    this.errorMessage = '';
    this.successMessage = '';

    const signUpRequest = {
      name: this.name,
      email: this.email,
      password: this.password,
      phone: this.phone,
      nationalId: this.nationalId
    };

    this.auth.register(signUpRequest).subscribe({
      next: () => {
        this.loading = false;
        this.successMessage = this.auth.currentLang === 'ar'
          ? 'تم إنشاء الحساب بنجاح! جاري تحويلك لصفحة الدخول...'
          : 'Compte créé avec succès ! Redirection vers la page de connexion...';
        setTimeout(() => {
          this.router.navigate(['/login']);
        }, 2500);
      },
      error: (err) => {
        this.loading = false;
        this.errorMessage = err.error || (this.auth.currentLang === 'ar'
          ? 'حدث خطأ أثناء التسجيل. يرجى التحقق من المدخلات'
          : "Une erreur s'est produite lors de l'inscription");
      }
    });
  }
}
