import { Component } from '@angular/core';
import { FileService } from '../services/file.service';

@Component({
  selector: 'app-upload',
  templateUrl: './upload.component.html',
  styleUrls: ['./upload.component.css']
})
export class UploadComponent {
  selected?: File;
  message = '';
  expirationDays?: number = 7;
  password = '';
  tagsText = '';
  result?: { token?: string; filename?: string; expiresAt?: string };
  history: any[] = [];
  historyFilter: 'all' | 'active' | 'expired' = 'all';

  constructor(private fileService: FileService) {}

  getDownloadUrl(token: string): string {
    return window.location.origin + '/download/' + token;
  }

  ngOnInit() {
    this.loadHistory();
  }

  loadHistory() {
    this.fileService.getHistory().subscribe({
      next: (data) => this.history = data,
      error: (err) => console.error('Failed to load history', err)
    });
  }

  get filteredHistory(): any[] {
    if (!this.history || this.history.length === 0) {
      return [];
    }
    if (this.historyFilter === 'active') {
      return this.history.filter(item => !this.isExpired(item));
    }
    if (this.historyFilter === 'expired') {
      return this.history.filter(item => this.isExpired(item));
    }
    return this.history;
  }

  setHistoryFilter(filter: 'all' | 'active' | 'expired') {
    this.historyFilter = filter;
  }

  isExpired(item: any): boolean {
    if (!item?.expiresAt) {
      return false;
    }
    return new Date(item.expiresAt).getTime() < Date.now();
  }

  expirationLabel(item: any): string {
    if (!item?.expiresAt) {
      return 'Expiration inconnue';
    }
    const expires = new Date(item.expiresAt).getTime();
    const diffMs = expires - Date.now();
    if (diffMs <= 0) {
      return 'Expiré';
    }
    const dayMs = 24 * 60 * 60 * 1000;
    const days = Math.ceil(diffMs / dayMs);
    if (days <= 1) {
      return 'Expire demain';
    }
    return `Expire dans ${days} jours`;
  }

  deleteFile(token: string) {
    if (confirm('Are you sure you want to delete this file?')) {
      this.fileService.deleteFile(token).subscribe({
        next: () => {
          this.message = 'File deleted successfully';
          this.loadHistory();
        },
        error: (err) => {
          this.message = 'Delete failed';
          console.error('Delete error:', err);
        }
      });
    }
  }

  onFileChange(event: any) {
    const f = event.target.files && event.target.files[0];
    if (f) { this.selected = f; }
  }

  upload() {
    if (!this.selected) { this.message = 'No file selected'; return; }
    const tags = this.tagsText ? this.tagsText.split(',').map(t => t.trim()).filter(Boolean) : [];
    this.message = 'Uploading...';
    this.fileService.upload(this.selected, this.expirationDays, this.password || undefined, tags).subscribe({
      next: (res) => {
        this.result = res;
        this.message = 'Upload successful';
        this.loadHistory();
      },
      error: err => {
        this.result = undefined;
        const status = err?.status ? `(${err.status})` : '';
        const body = err?.error?.message || err?.error || 'Unknown error';
        this.message = `Upload failed ${status}: ${typeof body === 'string' ? body : JSON.stringify(body)}`;
        console.error('Upload error:', err);
      }
    });
  }
}
