import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../environments/environments';
import { Observable } from 'rxjs';

export interface CalculationRequest {
  inputValue: number;
}

@Injectable({ providedIn: 'root' })
export class CalculatorService {
  private http = inject(HttpClient);
  private readonly apiUrl = `${environment.apiUrl}/calc`;

  getIncrement(value: number): Observable<number> {
    const payload = { inputValue: value }; 

    return this.http.post<number>(`${this.apiUrl}/increment`, payload);
  }
}