import { Routes, Router } from '@angular/router';
import { inject } from '@angular/core';
import { LoginComponent } from './components/login/login.component';
import { RegisterComponent } from './components/register/register.component';
import { CitizenDashboardComponent } from './components/citizen-dashboard/citizen-dashboard.component';
import { AgentDashboardComponent } from './components/agent-dashboard/agent-dashboard.component';
import { OfficiantDashboardComponent } from './components/officiant-dashboard/officiant-dashboard.component';
import { AgentRegisterComponent } from './components/agent-register/agent-register.component';
import { AgentActivateComponent } from './components/agent-activate/agent-activate.component';
import { AuthService } from './services/auth.service';

const roleGuard = (allowedRoles: string[]) => {
  return () => {
    const auth = inject(AuthService);
    const router = inject(Router);
    
    if (!auth.isLoggedIn()) {
      router.navigate(['/login']);
      return false;
    }
    
    const role = auth.getRole();
    if (role && allowedRoles.includes(role)) {
      return true;
    }
    
    // Redirect to matching home if logged in but on wrong path
    if (role === 'ROLE_CITIZEN') {
      router.navigate(['/citizen']);
    } else if (role === 'ROLE_AGENT') {
      router.navigate(['/agent']);
    } else if (role === 'ROLE_OFFICIANT') {
      router.navigate(['/officiant']);
    } else {
      router.navigate(['/login']);
    }
    return false;
  };
};

export const routes: Routes = [
  { path: 'login', component: LoginComponent },
  { path: 'register', component: RegisterComponent },
  { path: 'agent-register', component: AgentRegisterComponent },
  { path: 'agent-activate', component: AgentActivateComponent },
  { 
    path: 'citizen', 
    component: CitizenDashboardComponent, 
    canActivate: [roleGuard(['ROLE_CITIZEN'])] 
  },
  { 
    path: 'agent', 
    component: AgentDashboardComponent, 
    canActivate: [roleGuard(['ROLE_AGENT'])] 
  },
  { 
    path: 'officiant', 
    component: OfficiantDashboardComponent, 
    canActivate: [roleGuard(['ROLE_OFFICIANT'])] 
  },
  { 
    path: '', 
    redirectTo: '/login', 
    pathMatch: 'full' 
  },
  { 
    path: '**', 
    redirectTo: '/login' 
  }
];
