import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface CalcResult {
  probabilities: number[]; 
  maxHits: number;
  avgValue: number;
  avgProb: number;
  range80: string;
  rangeStd: string;
  maxProbValue: number;
  maxProbPercent: number;
  probAtLeastAvg: number;
  rangeTop5: string;
}

@Injectable({
  providedIn: 'root'
})
export class CalculatorService {
  private http = inject(HttpClient);
  private apiUrl = 'http://localhost:8080/api/calculate';

  calculate(requests: any[]): Observable<CalcResult> {
    return this.http.post<CalcResult>(this.apiUrl, requests);
  }
}