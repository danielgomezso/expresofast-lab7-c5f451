import { inject, Injectable, signal } from '@angular/core';
import { HttpClient, HttpInterceptorFn } from '@angular/common/http';
import { tap } from 'rxjs';
import { environment } from '../../environments/environment';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private http = inject(HttpClient);
  token = signal(sessionStorage.getItem('token') || '');

  login(username: string, password: string) {
    return this.http.post<{ token: string }>(new URL('../auth/login', environment.API_URL).href,
      { username, password }).pipe(tap(respuesta => {
        sessionStorage.setItem('token', respuesta.token);
        this.token.set(respuesta.token);
      }));
  }

  logout() {
    sessionStorage.removeItem('token');
    this.token.set('');
  }
}

export const authInterceptor: HttpInterceptorFn = (request, next) => {
  const token = inject(AuthService).token();
  if (token && request.url.startsWith(environment.API_URL)) {
    request = request.clone({ setHeaders: { Authorization: `Bearer ${token}` } });
  }
  return next(request);
};
