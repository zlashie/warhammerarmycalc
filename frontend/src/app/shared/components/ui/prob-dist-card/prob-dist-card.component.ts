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
  data = input<number[]>([]);
  stats = input<any | null>(null);

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