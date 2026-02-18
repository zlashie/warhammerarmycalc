import { Component, inject, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ProbDistCardComponent } from '../../../../shared/components/ui/prob-dist-card/prob-dist-card.component';
import { ArmyCalcComponent } from '../../army-calc.component';

@Component({
  selector: 'app-hit-dist-card',
  standalone: true,
  imports: [CommonModule, ProbDistCardComponent],
  template: `
    <app-prob-dist-card [data]="probabilities()">
      <h3 title>Hit Distribution</h3>
    </app-prob-dist-card>
  `
})
export class HitDistCardComponent {
  private parent = inject(ArmyCalcComponent);
  probabilities = computed(() => this.parent.calcResult()?.probabilities || []);
}