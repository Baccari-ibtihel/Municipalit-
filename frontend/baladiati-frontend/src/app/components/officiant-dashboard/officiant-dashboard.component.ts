import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { OfficiantService } from '../../services/officiant.service';
import { CitizenService } from '../../services/citizen.service';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-officiant-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './officiant-dashboard.component.html',
  styleUrls: ['./officiant-dashboard.component.css']
})
export class OfficiantDashboardComponent implements OnInit {
  contracts: any[] = [];

  // Form fields
  groomName = '';
  brideName = '';
  contractDate = '';
  place = '';
  witnessDetails = '';
  attachments: any[] = [];

  loading = false;
  successMessage = '';
  errorMessage = '';

  constructor(
    public auth: AuthService,
    private officiantService: OfficiantService,
    private citizenService: CitizenService
  ) {}

  ngOnInit(): void {
    this.loadContracts();
  }

  loadContracts(): void {
    this.officiantService.getMyContracts().subscribe({
      next: (data) => this.contracts = data,
      error: (err) => console.error(err)
    });
  }

  onFileChange(event: any): void {
    const files: FileList = event.target.files;
    if (files.length > 0) {
      const file = files[0];
      this.citizenService.uploadFile(file).subscribe({
        next: (res) => {
          this.attachments.push({
            fileName: res.fileName,
            fileType: res.fileType,
            fileUrl: res.fileUrl
          });
        },
        error: (err) => {
          this.errorMessage = this.auth.currentLang === 'ar'
            ? 'خطأ أثناء رفع الملف'
            : 'Erreur lors du chargement du fichier';
        }
      });
    }
  }

  onSubmit(): void {
    if (!this.groomName || !this.brideName || !this.contractDate || !this.place || !this.witnessDetails) {
      this.errorMessage = this.auth.currentLang === 'ar'
        ? 'الرجاء ملء جميع الحقول المطلوبة'
        : 'Veuillez remplir tous les champs obligatoires';
      return;
    }

    this.loading = true;
    this.errorMessage = '';
    this.successMessage = '';

    const newContract = {
      groomName: this.groomName,
      brideName: this.brideName,
      contractDate: this.contractDate,
      place: this.place,
      witnessDetails: this.witnessDetails,
      attachments: this.attachments
    };

    this.officiantService.createContract(newContract).subscribe({
      next: () => {
        this.loading = false;
        this.successMessage = this.auth.currentLang === 'ar'
          ? 'تم تسجيل عقد الزواج بنجاح وإرساله للمراجعة!'
          : 'Contrat de mariage enregistré et soumis avec succès !';
        
        // Reset form
        this.groomName = '';
        this.brideName = '';
        this.contractDate = '';
        this.place = '';
        this.witnessDetails = '';
        this.attachments = [];
        
        this.loadContracts();
      },
      error: (err) => {
        this.loading = false;
        this.errorMessage = err.error || 'Error submitting contract';
      }
    });
  }
}
