import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { FileService } from '../services/file.service';

@Component({
  selector: 'app-download',
  templateUrl: './download.component.html',
  styleUrls: ['./download.component.css']
})
export class DownloadComponent implements OnInit {
  token: string = '';
  fileInfo: any = null;
  password: string = '';
  error: string = '';
  loading: boolean = true;

  constructor(private route: ActivatedRoute, private fileService: FileService) {}

  ngOnInit(): void {
    this.token = this.route.snapshot.paramMap.get('token') || '';
    if (this.token) {
      this.fileService.getFileInfo(this.token).subscribe({
        next: (info) => {
          this.fileInfo = info;
          this.loading = false;
        },
        error: (err) => {
          this.error = 'Lien invalide ou fichier expiré.';
          this.loading = false;
        }
      });
    } else {
      this.error = 'Token manquant.';
      this.loading = false;
    }
  }

  onDownload(): void {
    this.error = '';
    this.fileService.downloadFile(this.token, this.password).subscribe({
      next: (blob) => {
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = this.fileInfo.filename;
        document.body.appendChild(a);
        a.click();
        document.body.removeChild(a);
        window.URL.revokeObjectURL(url);
      },
      error: (err) => {
        if (err.status === 403) {
          this.error = 'Mot de passe incorrect.';
        } else {
          this.error = 'Erreur lors du téléchargement.';
        }
      }
    });
  }

  formatSize(bytes: number): string {
    if (bytes === 0) return '0 Bytes';
    const k = 1024;
    const sizes = ['Bytes', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
  }
}
