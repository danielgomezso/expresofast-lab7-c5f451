import { AsyncValidatorFn, ValidatorFn } from '@angular/forms';
import { catchError, map, of, switchMap, timer } from 'rxjs';
import { EnvioService } from '../services/envio.service';

export const fechasValidas: ValidatorFn = control => {
  const despacho = control.get('fechaDespacho')?.value;
  const entrega = control.get('fechaEntregaEstimada')?.value;
  if (!despacho || !entrega) return null;
  return entrega > despacho ? null : { fechasInvalidas: true };
};

export function trackingDisponible(servicio: EnvioService): AsyncValidatorFn {
  return control => timer(400).pipe(
    switchMap(() => servicio.checkTracking(control.value)),
    map(existe => existe ? { trackingTomado: true } : null),
    catchError(() => of({ trackingNoVerificado: true })),
  );
}
