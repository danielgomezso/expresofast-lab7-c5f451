import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthService } from './services/auth.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [FormsModule, RouterLink, RouterLinkActive, RouterOutlet],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css',
})
export class AppComponent {
  auth = inject(AuthService);
  username = '';
  password = '';
  error = signal('');
  cargando = signal(false);

  iniciarSesion() {
    this.error.set('');
    this.cargando.set(true);
    this.auth.login(this.username, this.password).subscribe({
      next: () => {
        this.password = '';
        this.cargando.set(false);
      },
      error: () => {
        this.error.set('No se pudo iniciar sesión. Revisa tus credenciales y la conexión al backend.');
        this.cargando.set(false);
      },
    });
  }
}
