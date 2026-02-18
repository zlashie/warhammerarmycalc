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
        const maxVisibleBars = 30; 

        if (rawData.length <= maxVisibleBars) {
            return rawData.map((prob, i) => ({ label: i.toString(), value: prob }));
        }

        const bucketSize = Math.ceil(rawData.length / maxVisibleBars);
        const result = [];

        for (let i = 0; i < rawData.length; i += bucketSize) {
            const chunk = rawData.slice(i, i + bucketSize);
            const sum = chunk.reduce((a, b) => a + b, 0);
            
            const end = Math.min(i + bucketSize - 1, rawData.length - 1);
            const label = i === end ? i.toString() : `${i}`; 
            
            result.push({ label, value: sum });
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