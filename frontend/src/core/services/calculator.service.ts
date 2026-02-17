import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../environments/environments';
import { Observable } from 'rxjs';

export interface ArmyCalculationResult {
  expectedValue: number;
  standardDeviation: number;
  dataPoints: number[];
}

@Injectable({ providedIn: 'root' })
export class CalculatorService {
  private http = inject(HttpClient);
  private readonly apiUrl = `${environment.apiUrl}/api/calculator`;

  calculateArmy(units: any[]): Observable<ArmyCalculationResult> {
    return this.http.post<ArmyCalculationResult>(`${this.apiUrl}/calculate`, units);
  }
}