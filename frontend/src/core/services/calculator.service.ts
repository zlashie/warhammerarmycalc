import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

// NEW: Interface for the graph data points
export interface ToughnessNode {
  toughness: number;
  average: number;
  lower80: number;
  upper80: number;
}

export interface CalcResult {
  // Hit Stats
  avgValue: number;
  avgProb: number;
  probabilities: number[];
  range80: string;
  rangeTop5: string;
  probAtLeastAvg: number;

  // Wound Stats
  woundAvgValue?: number;
  woundProbabilities?: number[];
  woundRange80?: string;
  woundRangeTop5?: string;
  woundProbAtLeastAvg?: number;

  // Damage Stats (New)
  damageAvgValue?: number;
  damageProbabilities?: number[];
  damageRange80?: string;
  damageRangeTop5?: string;
  damageProbAtLeastAvg?: number;

  // NEW: Toughness Scaling Data
  toughnessScaling?: ToughnessNode[];
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