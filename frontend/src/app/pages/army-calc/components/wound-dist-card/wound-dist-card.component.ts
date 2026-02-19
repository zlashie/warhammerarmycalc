import { Component, inject, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ProbDistCardComponent } from '../../../../shared/components/ui/prob-dist-card/prob-dist-card.component';
import { ArmyCalcComponent } from '../../army-calc.component';

@Component({
  selector: 'app-wound-dist-card',
  standalone: true,
  imports: [CommonModule, ProbDistCardComponent],
  templateUrl: './wound-dist-card.component.html',
  styleUrl: './wound-dist-card.component.css'
})
export class WoundDistCardComponent {
  private parent = inject(ArmyCalcComponent);

  probabilities = computed(() => this.parent.calcResult()?.woundProbabilities || []);
  
  stats = computed(() => {
    const res = this.parent.calcResult();
    if (!res) return null;
    return {
      avgValue: res.woundAvgValue || 0,
      range80: res.woundRange80 || '0 - 0',
      rangeTop5: res.woundRangeTop5 || '0 - 0',
      probAtLeastAvg: res.woundProbAtLeastAvg || 0
    };
  });

  displayTitle = computed(() => {
    const selected = this.parent.editingUnit();
    return selected ? `Wounds: ${selected.name}` : 'Army Total Wounds';
  });
}