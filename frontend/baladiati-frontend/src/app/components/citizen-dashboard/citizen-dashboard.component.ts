import { Component, OnInit, OnDestroy, AfterViewChecked } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { CitizenService } from '../../services/citizen.service';
import { AuthService } from '../../services/auth.service';

declare const L: any;

@Component({
  selector: 'app-citizen-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './citizen-dashboard.component.html',
  styleUrls: ['./citizen-dashboard.component.css']
})
export class CitizenDashboardComponent implements OnInit, OnDestroy, AfterViewChecked {
  activeTab: 'requests' | 'reports' | 'baladiya' = 'requests';
  requests: any[] = [];
  reports: any[] = [];

  // Doc Request Form
  docType = '';
  docDetails = '';
  docFiles: any[] = [];
  submittingDoc = false;
  docError = '';
  docSuccess = '';

  // Report Form
  reportType = '';
  reportDescription = '';
  reportPhotoUrl = '';
  latitude = 36.8065;
  longitude = 10.1815;
  submittingReport = false;
  reportError = '';
  reportSuccess = '';

  // Map state
  private map: any = null;
  private marker: any = null;
  private mapInitialized = false;

  // ── Nearest Baladiya ──────────────────────────────────
  nearestBaladiya: any = null;
  baladiayaQueueCount: number = 0;
  baladiayaIsOpen: boolean = false;
  loadingBaladiya: boolean = false;
  hasExactUserLocation: boolean = false;
  private queuePollingInterval: any = null;
  private baladiayaMarker: any = null;

  constructor(public auth: AuthService, private citizenService: CitizenService) {}

  ngOnInit(): void {
    this.loadRequests();
    this.loadReports();
    // Do not automatically load list of all Tunisia baladiyat on init.
    // User must click "Utiliser ma position exacte" or select location on map.
  }

  ngOnDestroy(): void {
    this.stopQueuePolling();
  }

  ngAfterViewChecked(): void {
    if ((this.activeTab === 'reports' || this.activeTab === 'baladiya') && !this.mapInitialized) {
      this.initMap();
    }
  }

  switchTab(tab: 'requests' | 'reports' | 'baladiya'): void {
    this.activeTab = tab;
    if (tab === 'reports' || tab === 'baladiya') {
      this.mapInitialized = false;
      setTimeout(() => {
        this.initMap();
      }, 100);
    }
  }

  loadRequests(): void {
    this.citizenService.getRequests().subscribe({
      next: (data) => this.requests = data,
      error: (err) => console.error(err)
    });
  }

  loadReports(): void {
    this.citizenService.getReports().subscribe({
      next: (data) => this.reports = data,
      error: (err) => console.error(err)
    });
  }

  onDocFileChange(event: any): void {
    const files: FileList = event.target.files;
    if (files.length > 0) {
      const file = files[0];
      this.citizenService.uploadFile(file).subscribe({
        next: (res) => {
          this.docFiles.push({
            fileName: res.fileName,
            fileType: res.fileType,
            fileUrl: res.fileUrl
          });
        },
        error: (err) => {
          this.docError = this.auth.currentLang === 'ar'
            ? 'خطأ أثناء رفع الملف'
            : 'Erreur lors du chargement du fichier';
        }
      });
    }
  }

  onReportFileChange(event: any): void {
    const files: FileList = event.target.files;
    if (files.length > 0) {
      const file = files[0];
      this.citizenService.uploadFile(file).subscribe({
        next: (res) => {
          this.reportPhotoUrl = res.fileUrl;
        },
        error: (err) => {
          this.reportError = this.auth.currentLang === 'ar'
            ? 'خطأ أثناء رفع الصورة'
            : "Erreur lors du chargement de l'image";
        }
      });
    }
  }

  submitDocRequest(): void {
    if (!this.docType || !this.docDetails) {
      this.docError = this.auth.currentLang === 'ar'
        ? 'الرجاء تعبئة نوع الطلب والتفاصيل'
        : 'Veuillez remplir le type de document et les détails';
      return;
    }

    this.submittingDoc = true;
    this.docError = '';
    this.docSuccess = '';

    const newRequest = {
      type: this.docType,
      details: this.docDetails,
      attachments: this.docFiles
    };

    this.citizenService.createRequest(newRequest).subscribe({
      next: () => {
        this.submittingDoc = false;
        this.docSuccess = this.auth.currentLang === 'ar'
          ? 'تم تقديم الطلب بنجاح!'
          : 'Demande soumise avec succès !';
        this.docType = '';
        this.docDetails = '';
        this.docFiles = [];
        this.loadRequests();
      },
      error: (err) => {
        this.submittingDoc = false;
        this.docError = err.error || 'Error submitting request';
      }
    });
  }

  submitReport(): void {
    if (!this.reportType || !this.reportDescription) {
      this.reportError = this.auth.currentLang === 'ar'
        ? 'الرجاء اختيار نوع المشكل وكتابة الوصف'
        : 'Veuillez choisir le type de problème et saisir la description';
      return;
    }

    this.submittingReport = true;
    this.reportError = '';
    this.reportSuccess = '';

    const newReport = {
      type: this.reportType,
      description: this.reportDescription,
      photoUrl: this.reportPhotoUrl,
      latitude: this.latitude,
      longitude: this.longitude
    };

    this.citizenService.createReport(newReport).subscribe({
      next: () => {
        this.submittingReport = false;
        this.reportSuccess = this.auth.currentLang === 'ar'
          ? 'تم تسجيل البلاغ بنجاح!'
          : 'Signalement enregistré avec succès !';
        this.reportType = '';
        this.reportDescription = '';
        this.reportPhotoUrl = '';
        this.loadReports();
      },
      error: (err) => {
        this.submittingReport = false;
        this.reportError = err.error || 'Error submitting report';
      }
    });
  }

  private initMap(): void {
    const mapEl = document.getElementById('map');
    if (!mapEl || this.mapInitialized) return;

    try {
      this.mapInitialized = true;
      this.map = L.map('map').setView([this.latitude, this.longitude], 11);

      L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
        maxZoom: 19,
        attribution: '© OpenStreetMap'
      }).addTo(this.map);

      // Add user position marker
      const userIcon = L.divIcon({
        className: '',
        html: `<div class="user-map-pin" title="Position">📍</div>`,
        iconSize: [36, 36],
        iconAnchor: [18, 18]
      });
      this.marker = L.marker([this.latitude, this.longitude], { icon: userIcon, draggable: true }).addTo(this.map);

      // Render markers only if exact position has been set/selected
      if (this.hasExactUserLocation && this.nearbyBaladiyatList && this.nearbyBaladiyatList.length > 0) {
        this.placeAllBaladiyaMarkers(this.nearbyBaladiyatList);
      }

      // On drag end update coords & nearest baladiya
      this.marker.on('dragend', () => {
        const position = this.marker.getLatLng();
        this.latitude = parseFloat(position.lat.toFixed(6));
        this.longitude = parseFloat(position.lng.toFixed(6));
        this.hasExactUserLocation = true;
        this.loadNearestBaladiya(this.latitude, this.longitude);
      });

      // On map click update marker, coords & nearest baladiya
      this.map.on('click', (e: any) => {
        this.latitude = parseFloat(e.latlng.lat.toFixed(6));
        this.longitude = parseFloat(e.latlng.lng.toFixed(6));
        this.marker.setLatLng(e.latlng);
        this.hasExactUserLocation = true;
        this.loadNearestBaladiya(this.latitude, this.longitude);
      });
    } catch (e) {
      console.error('Failed to load map:', e);
    }
  }

  getUserLocation(): void {
    if (navigator.geolocation) {
      navigator.geolocation.getCurrentPosition(
        (position) => {
          this.hasExactUserLocation = true;
          this.latitude = parseFloat(position.coords.latitude.toFixed(6));
          this.longitude = parseFloat(position.coords.longitude.toFixed(6));
          if (this.map && this.marker) {
            this.map.setView([this.latitude, this.longitude], 13);
            this.marker.setLatLng([this.latitude, this.longitude]);
          }
          // After getting position, load nearest baladiya (top 5)
          this.loadNearestBaladiya(this.latitude, this.longitude);
        },
        (error) => {
          console.error("Erreur de géolocalisation: ", error);
          if (error.code === error.PERMISSION_DENIED) {
            this.reportError = this.auth.currentLang === 'ar'
              ? 'يرجى السماح بالوصول إلى الموقع في المتصفح'
              : "Veuillez autoriser l'accès à la position dans votre navigateur";
          }
        },
        { enableHighAccuracy: true, timeout: 15000, maximumAge: 0 }
      );
    } else {
      this.reportError = 'Geolocation is not supported by this browser.';
    }
  }

  // ── Nearest Baladiya Logic ─────────────────────────────
  nearbyBaladiyatList: any[] = [];
  private allBaladiyaMarkers: any[] = [];

  loadNearestBaladiya(lat: number, lng: number): void {
    this.loadingBaladiya = true;
    // Request top 5 nearest baladiyat to position
    this.citizenService.getNearbyBaladiyat(lat, lng, 5).subscribe({
      next: (data) => {
        this.loadingBaladiya = false;
        if (data && data.length > 0) {
          this.nearbyBaladiyatList = data;
          this.nearestBaladiya = data[0]; // Nearest baladiya is the first item
          this.baladiayaQueueCount = data[0].queueCount;
          this.baladiayaIsOpen = data[0].isOpen;
          this.placeAllBaladiyaMarkers(data);
          this.startQueuePolling(data[0].id);
        }
      },
      error: (err) => {
        console.error('Failed to load nearby baladiyat:', err);
        this.loadingBaladiya = false;
      }
    });
  }

  selectBaladiyaFromList(baladiya: any): void {
    this.nearestBaladiya = baladiya;
    this.baladiayaQueueCount = baladiya.queueCount;
    this.baladiayaIsOpen = baladiya.isOpen;
    this.startQueuePolling(baladiya.id);
    if (this.map && baladiya.latitude && baladiya.longitude) {
      this.map.setView([baladiya.latitude, baladiya.longitude], 14);
    }
  }

  private startQueuePolling(baladiayaId: number): void {
    this.stopQueuePolling();
    this.queuePollingInterval = setInterval(() => {
      this.citizenService.getQueueStatus(baladiayaId).subscribe({
        next: (data) => {
          this.baladiayaQueueCount = data.queueCount;
          this.baladiayaIsOpen = data.isOpen;
          // also update item in nearbyBaladiyatList
          const item = this.nearbyBaladiyatList.find(b => b.id === baladiayaId);
          if (item) {
            item.queueCount = data.queueCount;
            item.isOpen = data.isOpen;
          }
        },
        error: (err) => console.error('Queue polling error:', err)
      });
    }, 10000); // Poll every 10 seconds
  }

  private stopQueuePolling(): void {
    if (this.queuePollingInterval) {
      clearInterval(this.queuePollingInterval);
      this.queuePollingInterval = null;
    }
  }

  private placeAllBaladiyaMarkers(baladiyat: any[]): void {
    if (!this.map) return;
    
    // Clear old markers
    this.allBaladiyaMarkers.forEach(m => m.remove());
    this.allBaladiyaMarkers = [];

    baladiyat.forEach((b, idx) => {
      const isNearest = idx === 0;
      const pinClass = isNearest ? 'baladiya-map-pin pin-nearest' : 'baladiya-map-pin';
      const pinIcon = isNearest ? '🏛️⭐' : '🏛️';

      const baladiayaIcon = L.divIcon({
        className: '',
        html: `<div class="${pinClass}">${pinIcon}</div>`,
        iconSize: [40, 40],
        iconAnchor: [20, 20]
      });

      const m = L.marker([b.latitude, b.longitude], { icon: baladiayaIcon }).addTo(this.map);

      const name = this.auth.currentLang === 'ar' ? b.nameAr : b.nameFr;
      const distLabel = this.auth.currentLang === 'ar' ? 'المسافة' : 'Distance';
      const queueLabel = this.auth.currentLang === 'ar' ? 'شخص في الانتظار' : 'en attente';
      const popupContent = `
        <div class="baladiya-popup">
          <strong>${name}</strong><br>
          <span>📍 ${distLabel}: ${b.distanceKm} km</span><br>
          <span>🕒 ${b.openingTime} – ${b.closingTime}</span><br>
          <span>👥 ${b.queueCount} ${queueLabel}</span>
        </div>
      `;

      m.bindPopup(popupContent);
      this.allBaladiyaMarkers.push(m);
    });

    if (this.allBaladiyaMarkers.length > 0) {
      this.allBaladiyaMarkers[0].openPopup();
    }
  }
}
