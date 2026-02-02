import { Injectable } from '@angular/core';
import { HttpClient, HttpEvent } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class FileService {
  constructor(private http: HttpClient) {}

  upload(file: File): Observable<HttpEvent<any>> {
    const fd = new FormData();
    fd.append('file', file, file.name);
    return this.http.post<any>('/api/files/upload', fd, { reportProgress: true, observe: 'events' });
  }
}
