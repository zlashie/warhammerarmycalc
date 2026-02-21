import { Component, inject, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AvgAreaDistCardComponent, DistPoint } from '../../../../shared/components/ui/avg-area-dist-card/avg-area-dist-card.component';
import { ArmyCalcComponent } from '../../army-calc.component';

@Component({
  selector: 'app-save-dist-card',
  standalone: true,
  imports: [CommonModule, AvgAreaDistCardComponent],
  template: `
    <app-avg-area-dist-card 
      [data]="graphData()"
      [title]="displayTitle()"
      xAxisLabel="Target Save"
      yAxisLabel="Avg Damage"
    ></app-avg-area-dist-card>
  `
})
export class SaveDistCardComponent {
  private parent = inject(ArmyCalcComponent);

  displayTitle = computed(() => {
    const selected = this.parent.editingUnit();
    return selected ? `Save Scaling: ${selected.name}` : 'Army Save Scaling';
  });

  graphData = computed<DistPoint[]>(() => {
    const result = this.parent.calcResult();
    if (!result?.saveScaling) return []; 

    return result.saveScaling.map(node => ({
        xLabel: node.saveLabel,
        average: node.average,
        lower80: node.lower80,
        upper80: node.upper80
    }));
  });
}