import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../environments/environments';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class CalculatorService {
  private http = inject(HttpClient);
  private readonly apiUrl = environment.apiUrl;

getIncrement(value: number): Observable<number> {
  return this.http.get<number>(`${this.apiUrl}/increment/${value}`);
}
}