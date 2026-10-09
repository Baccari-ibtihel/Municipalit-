import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-agent-register',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './agent-register.component.html',
  styleUrls: ['./agent-register.component.css']
})
export class AgentRegisterComponent {
  name = '';
  email = '';
  password = '';
  confirmPassword = '';
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

    if (this.password !== this.confirmPassword) {
      this.errorMessage = this.auth.currentLang === 'ar'
        ? 'كلمتا المرور غير متطابقتين'
        : 'Les mots de passe ne correspondent pas';
      return;
    }

    if (this.password.length < 6) {
      this.errorMessage = this.auth.currentLang === 'ar'
        ? 'يجب أن تتكون كلمة المرور من 6 أحرف على الأقل'
        : 'Le mot de passe doit contenir au moins 6 caractères';
      return;
    }

    this.loading = true;
    this.errorMessage = '';
    this.successMessage = '';

    const request = {
      name: this.name,
      email: this.email,
      password: this.password,
      phone: this.phone,
      nationalId: this.nationalId
    };

    this.auth.registerAgent(request).subscribe({
      next: () => {
        this.loading = false;
        this.successMessage = this.auth.currentLang === 'ar'
          ? 'تم إرسال رمز التفعيل إلى بريدك الإلكتروني! جاري تحويلك...'
          : 'Code d\'activation envoyé à votre email ! Redirection...';
        // Passer l'email pour pré-remplir la page d'activation
        localStorage.setItem('pendingAgentEmail', this.email);
        setTimeout(() => {
          this.router.navigate(['/agent-activate']);
        }, 2500);
      },
      error: (err) => {
        this.loading = false;
        this.errorMessage = err.error || (this.auth.currentLang === 'ar'
          ? 'حدث خطأ أثناء التسجيل'
          : "Une erreur s'est produite lors de l'inscription");
      }
    });
  }
}
