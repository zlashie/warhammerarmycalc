import { Component, inject, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AvgAreaDistCardComponent, DistPoint } from '../../../../shared/components/ui/avg-area-dist-card/avg-area-dist-card.component';
import { ArmyCalcComponent } from '../../army-calc.component';

@Component({
  selector: 'app-toughness-dist-card',
  standalone: true,
  imports: [CommonModule, AvgAreaDistCardComponent],
  template: `
    <app-avg-area-dist-card 
      [data]="graphData()"
      [title]="displayTitle()"
      xAxisLabel="Toughness"
      yAxisLabel="Avg Wounds"
    ></app-avg-area-dist-card>
  `
})
export class ToughnessDistCardComponent {
  private parent = inject(ArmyCalcComponent);

  displayTitle = computed(() => {
    const selected = this.parent.editingUnit();
    return selected ? `Toughness Scaling: ${selected.name}` : 'Army Toughness Scaling';
  });

   /**
   * Transforms the backend DTO into the format required by the UI card.
   */
    graphData = computed<DistPoint[]>(() => {
    const result = this.parent.calcResult();
    if (!result?.toughnessScaling) return []; 

    return result.toughnessScaling.map(node => ({
        xLabel: node.toughness.toString(),
        average: node.average,
        lower80: node.lower80,
        upper80: node.upper80
    }));
    });
}