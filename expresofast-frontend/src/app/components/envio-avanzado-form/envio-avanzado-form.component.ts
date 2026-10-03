import { ChangeDetectorRef, Component, inject, signal } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { RouterLink } from '@angular/router';
import { EnvioService, mensajeError } from '../../services/envio.service';
import { fechasValidas, trackingDisponible } from '../../validators/envio.validators';

@Component({
  selector: 'app-envio-avanzado-form',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './envio-avanzado-form.component.html',
  styleUrl: './envio-avanzado-form.component.css',
})
export class EnvioAvanzadoFormComponent {
  private servicio = inject(EnvioService);
  private fb = inject(NonNullableFormBuilder);
  codigo = signal('');
  error = signal('');
  guardando = signal(false);

  formulario = this.fb.group({
    numeroTracking: this.fb.control('', {
      validators: [Validators.required, Validators.maxLength(30), Validators.pattern(/^[A-Za-z0-9-]+$/)],
      asyncValidators: [trackingDisponible(this.servicio)],
    }),
    destinatario: ['', [Validators.required, Validators.maxLength(100), Validators.pattern(/\S/)]],
    direccionDestino: ['', [Validators.required, Validators.maxLength(200), Validators.pattern(/\S/)]],
    montoFlete: [0, [Validators.required, Validators.min(0.01), Validators.max(99999999.99), Validators.pattern(/^\d+(\.\d{1,2})?$/)]],
    fechaDespacho: ['', Validators.required],
    fechaEntregaEstimada: ['', Validators.required],
    paquetes: this.fb.array([this.crearPaquete()], [Validators.required, Validators.minLength(1)]),
  }, { validators: fechasValidas });

  constructor() {
    const detector = inject(ChangeDetectorRef);
    this.formulario.statusChanges.pipe(takeUntilDestroyed()).subscribe(() => detector.markForCheck());
  }

  get paquetes() { return this.formulario.controls.paquetes; }

  private crearPaquete() {
    return this.fb.group({
      descripcion: ['', [Validators.required, Validators.maxLength(255), Validators.pattern(/\S/)]],
      pesoKg: [0, [Validators.required, Validators.min(0.01), Validators.max(999.99), Validators.pattern(/^\d+(\.\d{1,2})?$/)]],
    });
  }

  agregarPaquete() {
    this.paquetes.push(this.crearPaquete());
  }

  eliminarPaquete(indice: number) {
    if (this.paquetes.length > 1) this.paquetes.removeAt(indice);
  }

  verificarTracking() {
    this.formulario.controls.numeroTracking.updateValueAndValidity();
  }

  guardar() {
    this.formulario.markAllAsTouched();
    if (!this.formulario.valid || this.guardando()) return;
    this.error.set('');
    this.codigo.set('');
    this.guardando.set(true);
    this.servicio.registrarEnvio(this.formulario.getRawValue()).subscribe({
      next: envio => {
        this.codigo.set(envio.codigoRastreo);
        this.paquetes.clear();
        this.paquetes.push(this.crearPaquete());
        this.formulario.reset();
        this.guardando.set(false);
      },
      error: error => {
        this.error.set(mensajeError(error));
        this.guardando.set(false);
        if (error.status === 409) this.verificarTracking();
      },
    });
  }
}
