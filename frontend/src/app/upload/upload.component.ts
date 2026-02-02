import { Component } from '@angular/core';
import { FileService } from '../services/file.service';

@Component({
  selector: 'app-upload',
  templateUrl: './upload.component.html'
})
export class UploadComponent {
  selected?: File;
  message = '';

  constructor(private fileService: FileService) {}

  onFileChange(event: any) {
    const f = event.target.files && event.target.files[0];
    if (f) { this.selected = f; }
  }

  upload() {
    if (!this.selected) { this.message = 'No file selected'; return; }
    this.fileService.upload(this.selected).subscribe({
      next: () => this.message = 'Upload started (check backend for implementation)',
      error: err => this.message = 'Upload failed: ' + (err?.error?.message || err.statusText)
    });
  }
}
