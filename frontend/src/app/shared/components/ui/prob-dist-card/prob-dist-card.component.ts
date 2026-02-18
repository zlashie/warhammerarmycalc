import { Component, input, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { CardComponent } from '../card/card.component';

@Component({
  selector: 'app-prob-dist-card',
  standalone: true,
  imports: [CommonModule, CardComponent],
  templateUrl: './prob-dist-card.component.html',
  styleUrl: './prob-dist-card.component.css'
})

export class ProbDistCardComponent {
  data = input<number[]>([]);

  private readonly MAX_BARS = 40;
    displayData = computed(() => {
        const rawData = this.data();
        if (rawData.length === 0) return [];

        const threshold = 0.0001; 
        let firstIdx = rawData.findIndex(p => p > threshold);
        let lastIdx = [...rawData].reverse().findIndex(p => p > threshold);
        
        lastIdx = lastIdx === -1 ? rawData.length - 1 : (rawData.length - 1 - lastIdx);
        firstIdx = firstIdx === -1 ? 0 : firstIdx;

        const trimmedData = rawData.slice(firstIdx, lastIdx + 1);
        
        const maxVisibleBars = 30;

        if (trimmedData.length <= maxVisibleBars) {
            return trimmedData.map((prob, i) => ({ 
                label: (firstIdx + i).toString(), 
                value: prob 
            }));
        }

        const bucketSize = Math.ceil(trimmedData.length / maxVisibleBars);
        const result = [];

        for (let i = 0; i < trimmedData.length; i += bucketSize) {
            const chunk = trimmedData.slice(i, i + bucketSize);
            const sum = chunk.reduce((a, b) => a + b, 0);
            
            const startLabel = firstIdx + i;
            const endLabel = Math.min(firstIdx + i + bucketSize - 1, firstIdx + trimmedData.length - 1);
            
            result.push({ 
                label: startLabel === endLabel ? `${startLabel}` : `${startLabel}`, 
                value: sum 
            });
        }
        return result;
    });
  
    shouldShowLabel(index: number): boolean {
        const total = this.displayData().length;
        if (total <= 15) return true;
        if (total <= 30) return index % 5 === 0;
        return index % 10 === 0;
    }

    displayMax = computed(() => {
    const vals = this.displayData().map(d => d.value);
    return vals.length > 0 ? Math.max(...vals, 0.0001) : 0.0001;
    });
}