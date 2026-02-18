import { Component, inject, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ProbDistCardComponent } from '../../../../shared/components/ui/prob-dist-card/prob-dist-card.component';
import { ArmyCalcComponent } from '../../army-calc.component';

@Component({
  selector: 'app-hit-dist-card',
  standalone: true,
  imports: [CommonModule, ProbDistCardComponent],
  templateUrl: './hit-dist-card.component.html',
  styleUrl: './hit-dist-card.component.css'
})

export class HitDistCardComponent {
  private parent = inject(ArmyCalcComponent);
  
  probabilities = computed(() => this.parent.calcResult()?.probabilities || []);
  
stats = computed(() => {
    const res = this.parent.calcResult();
    return res as any; 
  });
}