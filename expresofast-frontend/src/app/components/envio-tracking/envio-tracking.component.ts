import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { Envio } from '../../models/envio.model';
import { EnvioService, mensajeError } from '../../services/envio.service';

@Component({
  selector: 'app-envio-tracking',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './envio-tracking.component.html',
  styleUrl: './envio-tracking.component.css',
})
export class EnvioTrackingComponent implements OnInit {
  private servicio = inject(EnvioService);
  private ruta = inject(ActivatedRoute);
  codigo = '';
  envio = signal<Envio | null>(null);
  error = signal('');
  buscando = signal(false);

  ngOnInit() {
    this.codigo = this.ruta.snapshot.queryParamMap.get('codigo') || '';
    if (this.codigo) this.buscar();
  }

  buscar() {
    if (!this.codigo.trim()) return;
    this.envio.set(null);
    this.error.set('');
    this.buscando.set(true);
    this.servicio.obtenerPorRastreo(this.codigo).subscribe({
      next: envio => { this.envio.set(envio); this.buscando.set(false); },
      error: error => { this.error.set(mensajeError(error)); this.buscando.set(false); },
    });
  }

  progreso(envio: Envio) {
    if (envio.estado === 'ENTREGADO') return 100;
    if (envio.estado === 'EN_TRANSITO') return 50;
    return 0;
  }
}
