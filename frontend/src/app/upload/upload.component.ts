import { Component } from '@angular/core';
import { FileService } from '../services/file.service';

@Component({
  selector: 'app-upload',
  templateUrl: './upload.component.html'
})
export class UploadComponent {
  selected?: File;
  message = '';
  expirationDays?: number = 7;
  password = '';
  tagsText = '';
  result?: { token?: string; filename?: string; expiresAt?: string };
  history: any[] = [];

  constructor(private fileService: FileService) {}

  ngOnInit() {
    this.loadHistory();
  }

  loadHistory() {
    this.fileService.getHistory().subscribe({
      next: (data) => this.history = data,
      error: (err) => console.error('Failed to load history', err)
    });
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
