import { Injectable } from '@angular/core';
import { HttpErrorResponse, HttpEvent, HttpHandler, HttpInterceptor, HttpRequest } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';

@Injectable()
export class ErrorInterceptor implements HttpInterceptor {
  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    return next.handle(req).pipe(
      catchError((err: unknown) => {
        if (err instanceof HttpErrorResponse) {
          const userMessage = this.toUserMessage(err);
          return throwError(() => ({ ...err, userMessage }));
        }
        return throwError(() => ({ userMessage: 'Erreur inattendue. Veuillez réessayer.' }));
      })
    );
  }

  private toUserMessage(err: HttpErrorResponse): string {
    const serverMessage = this.extractServerMessage(err);
    const url = err.url || '';
    const serverMessageLc = (serverMessage || '').toLowerCase();
    const uploadUrl = url.includes('/api/files/upload');

    if (
      uploadUrl &&
      (
        err.status === 413 ||
        serverMessageLc.includes('maximum upload size') ||
        serverMessageLc.includes('max upload size') ||
        serverMessageLc.includes('request entity too large') ||
        serverMessageLc.includes('file too large')
      )
    ) {
      return 'Fichier trop volumineux. Taille maximale autorisée: 1 Go.';
    }

    // Keep domain-specific messages when backend provides one.
    if (serverMessage) {
      return serverMessage;
    }

    if (err.status === 0) {
      return 'Service indisponible ou problème réseau.';
    }

    if (err.status === 400) {
      if (url.includes('/api/files/upload')) {
        return 'Upload invalide: vérifiez le fichier, l’extension et les paramètres.';
      }
      return 'Requête invalide. Vérifiez les champs saisis.';
    }

    if (err.status === 401) {
      return 'Session expirée. Veuillez vous reconnecter.';
    }

    if (err.status === 403) {
      if (url.includes('/api/files/download')) {
        return 'Accès refusé: mot de passe incorrect ou permissions insuffisantes.';
      }
      return 'Accès refusé.';
    }

    if (err.status === 404) {
      return 'Ressource introuvable ou expirée.';
    }

    if (err.status >= 500) {
      return 'Erreur serveur. Veuillez réessayer plus tard.';
    }

    return 'Une erreur est survenue. Veuillez réessayer.';
  }

  private extractServerMessage(err: HttpErrorResponse): string | null {
    const payload = err.error;
    if (!payload) {
      return null;
    }
    if (typeof payload === 'string' && payload.trim().length > 0) {
      return payload;
    }
    if (typeof payload === 'object') {
      const msg = (payload as any).message;
      if (typeof msg === 'string' && msg.trim().length > 0) {
        return msg;
      }
    }
    return null;
  }
}
