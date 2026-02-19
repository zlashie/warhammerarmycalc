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
  
  stats = computed(() => this.parent.calcResult());

  displayTitle = computed(() => {
    const selected = this.parent.editingUnit();
    return selected ? `Wounds: ${selected.name}` : 'Army Total Wounds';
  });
}