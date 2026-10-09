import { Component, OnInit, AfterViewChecked } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AgentService } from '../../services/agent.service';
import { AuthService } from '../../services/auth.service';

declare const L: any;

@Component({
  selector: 'app-agent-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './agent-dashboard.component.html',
  styleUrls: ['./agent-dashboard.component.css']
})
export class AgentDashboardComponent implements OnInit, AfterViewChecked {
  activeTab: 'stats' | 'requests' | 'reports' | 'contracts' = 'stats';
  stats: any = {
    totalRequests: 0,
    pendingRequests: 0,
    totalReports: 0,
    unresolvedReports: 0,
    totalContracts: 0,
    pendingContracts: 0
  };

  requests: any[] = [];
  reports: any[] = [];
  contracts: any[] = [];

  // Notes fields for status updates
  requestNotes: { [key: number]: string } = {};
  contractNotes: { [key: number]: string } = {};

  // Map state
  private map: any = null;
  private mapInitialized = false;

  // ── Queue Management ─────────────────────────────────
  queueBaladiayaId: number = 1; // Default: Baladiya de Tunis (ID 1)
  currentQueueCount: number = 0;
  queueActionLoading: boolean = false;
  queueFeedback: string = '';

  constructor(public auth: AuthService, private agentService: AgentService) {}

  ngOnInit(): void {
    this.loadStats();
    this.loadRequests();
    this.loadReports();
    this.loadContracts();
  }

  ngAfterViewChecked(): void {
    if (this.activeTab === 'reports' && !this.mapInitialized && this.reports.length > 0) {
      this.initReportsMap();
    }
  }

  switchTab(tab: 'stats' | 'requests' | 'reports' | 'contracts'): void {
    this.activeTab = tab;
    if (tab === 'reports') {
      this.mapInitialized = false;
      setTimeout(() => {
        this.initReportsMap();
      }, 200);
    }
  }

  loadStats(): void {
    this.agentService.getStats().subscribe({
      next: (data) => this.stats = data,
      error: (err) => console.error(err)
    });
  }

  loadRequests(): void {
    this.agentService.getAllRequests().subscribe({
      next: (data) => this.requests = data,
      error: (err) => console.error(err)
    });
  }

  loadReports(): void {
    this.agentService.getAllReports().subscribe({
      next: (data) => {
        this.reports = data;
        if (this.activeTab === 'reports') {
          this.initReportsMap();
        }
      },
      error: (err) => console.error(err)
    });
  }

  loadContracts(): void {
    this.agentService.getAllContracts().subscribe({
      next: (data) => this.contracts = data,
      error: (err) => console.error(err)
    });
  }

  changeRequestStatus(id: number, status: string): void {
    const notes = this.requestNotes[id] || '';
    this.agentService.updateRequestStatus(id, status, notes).subscribe({
      next: () => {
        this.loadRequests();
        this.loadStats();
        alert(this.auth.currentLang === 'ar' ? 'تم تحديث حالة الطلب وإرسال تنبيه بالبريد الإلكتروني!' : 'Statut mis à jour et notification envoyée !');
      },
      error: (err) => {
        const msg = typeof err.error === 'string' ? err.error : (err.error?.message || err.message || 'Erreur lors de la mise à jour');
        alert('Error: ' + msg);
      }
    });
  }

  changeReportStatus(id: number, status: string): void {
    this.agentService.updateReportStatus(id, status).subscribe({
      next: () => {
        this.loadReports();
        this.loadStats();
        alert(this.auth.currentLang === 'ar' ? 'تم تحديث حالة البلاغ بنجاح!' : 'Statut du problème mis à jour !');
      },
      error: (err) => {
        const msg = typeof err.error === 'string' ? err.error : (err.error?.message || err.message || 'Erreur lors de la mise à jour');
        alert('Error: ' + msg);
      }
    });
  }

  changeContractStatus(id: number, status: string): void {
    const notes = this.contractNotes[id] || '';
    this.agentService.updateContractStatus(id, status, notes).subscribe({
      next: () => {
        this.loadContracts();
        this.loadStats();
        alert(this.auth.currentLang === 'ar' ? 'تم تحديث عقد الزواج وتنبيه الأطراف بالبريد الإلكتروني!' : 'Contrat de mariage mis à jour et courriel envoyé !');
      },
      error: (err) => {
        const msg = typeof err.error === 'string' ? err.error : (err.error?.message || err.message || 'Erreur lors de la mise à jour');
        alert('Error: ' + msg);
      }
    });
  }

  // ── Queue Management ─────────────────────────────────

  incrementQueue(): void {
    this.queueActionLoading = true;
    this.agentService.incrementQueue(this.queueBaladiayaId).subscribe({
      next: (res) => {
        this.currentQueueCount = res.queueCount;
        this.queueActionLoading = false;
        this.showQueueFeedback('+1');
      },
      error: (err) => { console.error(err); this.queueActionLoading = false; }
    });
  }

  decrementQueue(): void {
    this.queueActionLoading = true;
    this.agentService.decrementQueue(this.queueBaladiayaId).subscribe({
      next: (res) => {
        this.currentQueueCount = res.queueCount;
        this.queueActionLoading = false;
        this.showQueueFeedback('-1');
      },
      error: (err) => { console.error(err); this.queueActionLoading = false; }
    });
  }

  private showQueueFeedback(msg: string): void {
    this.queueFeedback = msg;
    setTimeout(() => this.queueFeedback = '', 1500);
  }

  private initReportsMap(): void {
    const mapEl = document.getElementById('reports-map');
    if (!mapEl || this.mapInitialized) return;

    try {
      this.mapInitialized = true;
      this.map = L.map('reports-map').setView([36.8065, 10.1815], 12);

      L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
        attribution: '© OpenStreetMap'
      }).addTo(this.map);

      // Add markers for all reports
      this.reports.forEach((rep) => {
        if (rep.latitude && rep.longitude) {
          const statusText = rep.status;
          const marker = L.marker([rep.latitude, rep.longitude]).addTo(this.map);
          marker.bindPopup(`
            <strong>${rep.type}</strong><br>
            ${rep.description}<br>
            Status: <strong>${statusText}</strong>
          `);
        }
      });
    } catch (e) {
      console.error('Failed to init reports map:', e);
    }
  }
}
