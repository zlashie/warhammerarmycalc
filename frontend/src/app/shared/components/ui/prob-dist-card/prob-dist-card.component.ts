// prob-dist-card.component.ts

import { Component, input, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { CardComponent } from '../card/card.component';

interface ChartBar {
  label: string;
  value: number;
}

@Component({
  selector: 'app-prob-dist-card',
  standalone: true,
  imports: [CommonModule, CardComponent],
  templateUrl: './prob-dist-card.component.html',
  styleUrl: './prob-dist-card.component.css'
})
export class ProbDistCardComponent {
  protected readonly Math = Math;
  
  // Inputs
  data = input<number[]>([]);
  stats = input<any | null>(null);
  typeLabel = input<string>('Hits'); // 'Hits' or 'Wounds'

  private readonly CONFIG = {
    THRESHOLD: 0.0001,
    MAX_VISIBLE_BARS: 30,
    LABEL_HEIGHT_RATIO: 0.15
  };

  /**
   * Main transformation pipeline: Trim -> Bucket -> Map
   */
  displayData = computed<ChartBar[]>(() => {
    const rawData = this.data();
    if (rawData.length === 0) return [];

    const { first, last } = this.getSignificantIndices(rawData);
    const trimmed = rawData.slice(first, last + 1);

    return this.generateBars(trimmed, first);
  });

  displayMax = computed(() => {
    const values = this.displayData().map(d => d.value);
    return Math.max(...values, this.CONFIG.THRESHOLD);
  });

  /**
   * Data: displayed data with dynamic labels and fixed NaN probability
   */
  displayStats = computed(() => {
    const s = this.stats();
    const rawData = this.data();
    if (!s || !rawData || rawData.length === 0) return [];

    const isWound = this.typeLabel() === 'Wounds';
    const isDamage = this.typeLabel() === 'Damage'; // Add this

    const avgVal = isDamage 
        ? (s.damageAvgValue ?? 0)
        : isWound 
        ? (s.woundAvgValue ?? s.avgValue ?? 0) 
        : (s.avgValue ?? 0);

    const probAtLeastAvg = isDamage
        ? (s.damageProbAtLeastAvg ?? 0)
        : isWound 
        ? (s.woundProbAtLeastAvg ?? s.probAtLeastAvg ?? 0) 
        : (s.probAtLeastAvg ?? 0);

    const range80 = isDamage
        ? (s.damageRange80 ?? '0 - 0')
        : isWound 
        ? (s.woundRange80 ?? s.range80 ?? '0 - 0') 
        : (s.range80 ?? '0 - 0');

    const rangeTop5 = isDamage
        ? (s.damageRangeTop5 ?? '0 - 0')
        : isWound 
        ? (s.woundRangeTop5 ?? s.rangeTop5 ?? '0 - 0') 
        : (s.rangeTop5 ?? '0 - 0');

    const roundedAvgIndex = Math.round(avgVal);
    const safeIndex = Math.min(roundedAvgIndex, rawData.length - 1);
    const probAtAvg = rawData[safeIndex] 
      ? Math.round(rawData[safeIndex] * 100) 
      : 0;

    return [
      {
        value: Math.round(avgVal),
        label: `Avg ${this.typeLabel()}`,
        sub: `${probAtAvg}% Probability`,
        cssClass: ''
      },
      {
        value: `${Math.round(probAtLeastAvg)}%`,
        label: 'Prob ≥ Avg',
        sub: 'Success',
        cssClass: ''
      },
      {
        value: range80,
        label: '80% Range',
        sub: 'Reliability',
        cssClass: range80.length > 3 ? 'long-value' : ''
      },
      {
        value: rangeTop5,
        label: 'Top 5%',
        sub: 'Lucky Roll',
        cssClass: rangeTop5.length > 3 ? 'long-value' : ''
      }
    ];
  });

  /**
   * Helper: Find indices where data starts/ends above threshold
   */
  private getSignificantIndices(data: number[]) {
    const first = data.findIndex(p => p > this.CONFIG.THRESHOLD);
    const last = data.length - 1 - [...data].reverse().findIndex(p => p > this.CONFIG.THRESHOLD);
    
    return {
      first: first === -1 ? 0 : first,
      last: last >= data.length ? data.length - 1 : last
    };
  }

  /**
   * Helper: Logic for creating bars (direct mapping vs bucketing)
   */
  private generateBars(data: number[], offset: number): ChartBar[] {
    if (data.length <= this.CONFIG.MAX_VISIBLE_BARS) {
      return data.map((val, i) => ({ label: (offset + i).toString(), value: val }));
    }

    const bucketSize = Math.ceil(data.length / this.CONFIG.MAX_VISIBLE_BARS);
    return Array.from({ length: Math.ceil(data.length / bucketSize) }, (_, i) => {
      const chunk = data.slice(i * bucketSize, (i + 1) * bucketSize);
      return {
        label: (offset + (i * bucketSize)).toString(),
        value: chunk.reduce((sum, v) => sum + v, 0)
      };
    });
  }

  shouldShowLabel(index: number): boolean {
    const total = this.displayData().length;
    if (total <= 15) return true;
    if (total <= 30) return index % 5 === 0;
    return index % 10 === 0;
  }
}