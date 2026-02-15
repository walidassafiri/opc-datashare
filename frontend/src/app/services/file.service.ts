import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class FileService {
  constructor(private http: HttpClient) {}

  upload(file: File, expirationDays?: number, password?: string, tags?: string[]): Observable<any> {
    const fd = new FormData();
    fd.append('file', file, file.name);
    if (expirationDays != null) { fd.append('expirationDays', String(expirationDays)); }
    if (password) { fd.append('password', password); }
    if (tags && tags.length) { tags.forEach(t => fd.append('tags', t)); }
    return this.http.post<any>('/api/files/upload', fd);
  }

  getHistory(): Observable<any[]> {
    return this.http.get<any[]>('/api/files/history');
  }

  getFileInfo(token: String): Observable<any> {
    return this.http.get<any>(`/api/files/info/${token}`);
  }

  downloadFile(token: string, password?: string): Observable<Blob> {
    let url = `/api/files/download/${token}`;
    if (password) {
      url += `?password=${encodeURIComponent(password)}`;
    }
    return this.http.get(url, { responseType: 'blob' });
  }
}
