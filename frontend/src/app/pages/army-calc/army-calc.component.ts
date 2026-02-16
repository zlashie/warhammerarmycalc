import { Component, signal, inject, effect } from '@angular/core';
import { FormsModule } from '@angular/forms';
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
  imports: [FormsModule, CardComponent, AddUnitCardComponent],
  templateUrl: './army-calc.component.html',
  styleUrls: ['./army-calc.component.css']
})
export class ArmyCalcComponent {
  private calcService = inject(CalculatorService);

  armyUnits = signal<Unit[]>([this.createDefaultUnit()]);
  isLoading = signal(false);

  statFields = [
    { label: 'Models', key: 'models', placeholder: '1' },
    { label: 'Attacks', key: 'attacks', placeholder: '1' },
    { label: 'BS/WS', key: 'bsWs', placeholder: '4+' },
    { label: 'Strength', key: 'strength', placeholder: '4' },
    { label: 'AP', key: 'ap', placeholder: '0' },
    { label: 'Damage', key: 'damage', placeholder: '1' }
  ];

  toggleFields = [
    { label: 'Lethal', key: 'lethal', icon: 'L' },
    { label: 'Sustained', key: 'sustained', icon: 'S' },
    { label: 'Dev Wounds', key: 'devastating', icon: 'D' },
    { label: 'TwinLinked', key: 'twinLinked', icon: 'T' },
    { label: 'Reroll Hits', key: 'rerollHits', icon: 'A' }
  ];

  constructor() {
    effect(() => {
      const units = this.armyUnits();
      console.log('Pipeline Triggered. Sending Army to Backend:', units);
    });
  }

createDefaultUnit(): Unit {
  return {
    id: Date.now(),
    stats: { 
      models: null, 
      attacks: null, 
      bsWs: null, 
      strength: null, 
      ap: null, 
      damage: null 
    },
    toggles: { 
      lethal: false, 
      sustained: false, 
      devastating: false, 
      twinLinked: false, 
      rerollHits: false 
    }
  };
}

  addUnit() {
    this.armyUnits.update(units => [this.createDefaultUnit(), ...units]);
  }

  removeUnit(id: number) {
    this.armyUnits.update(units => units.filter(u => u.id !== id));
  }

  toggleOrb(unitId: number, key: string) {
    this.armyUnits.update(units => units.map(u => {
      if (u.id === unitId) {
        const toggles = u.toggles as any;
        return { ...u, toggles: { ...toggles, [key]: !toggles[key] } };
      }
      return u;
    }));
  }

  onUnitAdded(newUnit: any) {
    this.armyUnits.update(units => [newUnit, ...units]);
    console.log('Unit received from child component:', newUnit);
  }
}