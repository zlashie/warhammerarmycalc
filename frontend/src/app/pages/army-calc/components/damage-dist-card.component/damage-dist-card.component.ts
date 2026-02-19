import { Component, inject, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ProbDistCardComponent } from '../../../../shared/components/ui/prob-dist-card/prob-dist-card.component';
import { ArmyCalcComponent } from '../../army-calc.component';

@Component({
  selector: 'app-damage-dist-card',
  standalone: true,
  imports: [CommonModule, ProbDistCardComponent],
  templateUrl: './damage-dist-card.component.html',
  styleUrl: './damage-dist-card.component.css'
})
export class DamageDistCardComponent {
  private parent = inject(ArmyCalcComponent);

  probabilities = computed(() => this.parent.calcResult()?.damageProbabilities || []);
  
  stats = computed(() => this.parent.calcResult());

  displayTitle = computed(() => {
    const selected = this.parent.editingUnit();
    return selected ? `Damage: ${selected.name}` : 'Army Total Damage';
  });
}