import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { Envio, EstadoEnvio } from '../../models/envio.model';
import { EnvioService, mensajeError } from '../../services/envio.service';

@Component({
  selector: 'app-envio-list',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './envio-list.component.html',
  styleUrl: './envio-list.component.css',
})
export class EnvioListComponent implements OnInit {
  private servicio = inject(EnvioService);
  envios = signal<Envio[]>([]);
  error = signal('');
  mensaje = signal('');
  cargando = signal(false);
  actualizando = signal<number | null>(null);

  ngOnInit() {
    this.cargar();
  }

  cargar() {
    this.cargando.set(true);
    this.error.set('');
    this.servicio.obtenerEnvios().subscribe({
      next: envios => { this.envios.set(envios); this.cargando.set(false); },
      error: error => { this.error.set(mensajeError(error)); this.cargando.set(false); },
    });
  }

  estadosPermitidos(estado: EstadoEnvio): EstadoEnvio[] {
    if (estado === 'PENDIENTE') return ['EN_TRANSITO', 'CANCELADO'];
    if (estado === 'EN_TRANSITO') return ['ENTREGADO'];
    return [];
  }

  actualizar(envio: Envio, estado: EstadoEnvio, selector: HTMLSelectElement) {
    selector.value = envio.estado;
    this.error.set('');
    this.mensaje.set('');
    this.actualizando.set(envio.id);
    this.servicio.actualizarEstado(envio.id, estado).subscribe({
      next: actualizado => {
        this.envios.update(envios => envios.map(item => item.id === actualizado.id ? actualizado : item));
        this.mensaje.set(`Estado de ${actualizado.codigoRastreo} actualizado a ${actualizado.estado}.`);
        this.actualizando.set(null);
      },
      error: error => { this.error.set(mensajeError(error)); this.actualizando.set(null); },
    });
  }
}
