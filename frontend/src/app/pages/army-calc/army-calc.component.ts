import { Component, signal, inject, effect } from '@angular/core';
import { CommonModule } from '@angular/common';
import { CardComponent } from '../../shared/components/ui/card/card.component';
import { CalculatorService } from '../../../core/services/calculator.service';
import { AddUnitCardComponent } from './components/add-unit-card/add-unit-card.component';

interface Unit {
  id: number;
  stats: { [key: string]: any };
  toggles: {
    lethal: boolean;
    sustained: boolean;
    devastating: boolean;
    twinLinked: boolean;
    rerollHits: boolean;
  };
}

@Component({
  selector: 'app-army-calc',
  standalone: true,
  imports: [CommonModule, CardComponent, AddUnitCardComponent],
  templateUrl: './army-calc.component.html',
  styleUrls: ['./army-calc.component.css']
})
export class ArmyCalcComponent {
  private calcService = inject(CalculatorService);

  armyUnits = signal<Unit[]>([]);
  isLoading = signal(false);

  constructor() {
    effect(() => {
      const units = this.armyUnits();
      console.log('Pipeline Triggered. Sending Army to Backend:', units);
    });
  }

  onUnitAdded(newUnit: Unit) {
    this.armyUnits.update(units => [newUnit, ...units]);
  }

  removeUnit(id: number) {
    this.armyUnits.update(units => units.filter(u => u.id !== id));
  }
}