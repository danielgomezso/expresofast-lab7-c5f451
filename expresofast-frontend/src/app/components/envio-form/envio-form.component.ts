import { Component, inject, signal } from '@angular/core';
import { FormsModule, NgForm } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { CrearEnvioPayload } from '../../models/envio.model';
import { EnvioService, mensajeError } from '../../services/envio.service';

@Component({
  selector: 'app-envio-form',
  standalone: true,
  imports: [FormsModule, RouterLink],
  templateUrl: './envio-form.component.html',
  styleUrl: './envio-form.component.css',
})
export class EnvioFormComponent {
  private servicio = inject(EnvioService);
  envio: CrearEnvioPayload = { destinatario: '', direccionDestino: '', montoFlete: null };
  codigo = signal('');
  error = signal('');
  guardando = signal(false);

  guardar(formulario: NgForm) {
    this.error.set('');
    this.codigo.set('');
    if (formulario.invalid || !this.envio.destinatario.trim() || !this.envio.direccionDestino.trim()) {
      this.error.set('Completa los campos obligatorios con datos válidos.');
      return;
    }
    this.guardando.set(true);
    this.servicio.crearEnvio(this.envio).subscribe({
      next: envio => {
        this.codigo.set(envio.codigoRastreo);
        formulario.resetForm({ destinatario: '', direccionDestino: '', montoFlete: null });
        this.guardando.set(false);
      },
      error: error => { this.error.set(mensajeError(error)); this.guardando.set(false); },
    });
  }
}
