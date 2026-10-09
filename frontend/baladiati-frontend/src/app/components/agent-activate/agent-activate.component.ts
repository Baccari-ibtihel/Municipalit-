import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-agent-activate',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './agent-activate.component.html',
  styleUrls: ['./agent-activate.component.css']
})
export class AgentActivateComponent implements OnInit {
  activationCode = '';
  email = '';
  password = '';

  errorMessage = '';
  successMessage = '';
  loading = false;

  constructor(public auth: AuthService, private router: Router) {}

  ngOnInit(): void {
    // Pré-remplir l'email si l'agent vient de la page d'inscription
    const pendingEmail = localStorage.getItem('pendingAgentEmail');
    if (pendingEmail) {
      this.email = pendingEmail;
    }
  }

  onSubmit(): void {
    if (!this.activationCode || !this.email || !this.password) {
      this.errorMessage = this.auth.currentLang === 'ar'
        ? 'الرجاء ملء جميع الحقول'
        : 'Veuillez remplir tous les champs';
      return;
    }

    this.loading = true;
    this.errorMessage = '';
    this.successMessage = '';

    const request = {
      activationCode: this.activationCode,
      email: this.email,
      password: this.password
    };

    this.auth.activateAgent(request).subscribe({
      next: () => {
        this.loading = false;
        localStorage.removeItem('pendingAgentEmail');
        this.successMessage = this.auth.currentLang === 'ar'
          ? 'تم تفعيل الحساب بنجاح! جاري تحويلك لصفحة الدخول...'
          : 'Compte activé avec succès ! Redirection vers la connexion...';
        setTimeout(() => {
          this.router.navigate(['/login']);
        }, 2500);
      },
      error: (err) => {
        this.loading = false;
        this.errorMessage = err.error || (this.auth.currentLang === 'ar'
          ? 'خطأ في التفعيل. تحقق من الرمز والبيانات'
          : "Erreur d'activation. Vérifiez le code et vos informations.");
      }
    });
  }
}
