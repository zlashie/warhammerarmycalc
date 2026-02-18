import { Component, signal, inject, effect, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { CardComponent } from '../../shared/components/ui/card/card.component';
import { CalculatorService, CalcResult } from '../../../core/services/calculator.service';
import { AddUnitCardComponent } from './components/add-unit-card/add-unit-card.component';
import { LedgerCardComponent } from './components/ledger-card/ledger-card.component';

interface Unit {
  id: number;
  name: string;
  points: string;
  stats: {
    models: string;
    attacks: string;
    bsWs: string;
    strength: string;
    ap: string;
    damage: string;
  };
  toggles: {
    lethal: boolean;
    sustained: any; 
    crit: any;
    rerollHits: any;
    rerollWounds: any;
    antiX: any;
    devastating: boolean;
  };
}

@Component({
  selector: 'app-army-calc',
  standalone: true,
  imports: [CommonModule, CardComponent, AddUnitCardComponent, LedgerCardComponent],
  templateUrl: './army-calc.component.html',
  styleUrls: ['./army-calc.component.css']
})
export class ArmyCalcComponent {
  private calcService = inject(CalculatorService);

  editingUnit = signal<Unit | null>(null);
  isLoading = signal(false);
  armyUnits = signal<Unit[]>(this.loadFromStorage());
  calcResult = signal<CalcResult | null>(null);

  constructor() {
    effect(() => {
      const units = this.armyUnits();
      localStorage.setItem('warhammer_army', JSON.stringify(units));
      
      if (units.length > 0) {
        this.performArmyCalculation(units);
      } else {
        this.calcResult.set(null);
      }
    });
  }

  private performArmyCalculation(units: Unit[]) {
    const requests = units.map(unit => ({
      unitName: unit.name,
      numberOfModels: parseInt(unit.stats.models) || 0,
      attacksPerModel: parseInt(unit.stats.attacks) || 0,
      bsValue: parseInt(unit.stats.bsWs.replace('+', '')) || 4,
      strength: parseInt(unit.stats.strength) || 0,
      ap: parseInt(unit.stats.ap) || 0,
      damage: parseInt(unit.stats.damage) || 0
    }));

    this.isLoading.set(true);

    this.calcService.calculate(requests).subscribe({
      next: (res: CalcResult) => {
        this.calcResult.set(res);
        this.isLoading.set(false);
      },
      error: (err: Error) => { 
        console.error('Army calculation failed', err);
        this.isLoading.set(false);
      }
    });
  }

  graphState = computed(() => {
    const result = this.calcResult();
    const width = 400;
    const height = 180;

    if (!result || !result.maxPossibleValue || result.standardDeviation === 0) {
        return { 
          linePath: 'M 0 180 L 400 180', 
          fillPath: 'M 0 180 L 400 180 Z', 
          min: 0, 
          max: 0 
        };
    }

    const E = result.expectedValue;
    const S = result.standardDeviation;
    const Max = result.maxPossibleValue;

    const peakProbability = 1 / (S * Math.sqrt(2 * Math.PI));
    
    let points: string[] = [];
    
    for (let i = 0; i <= 100; i++) {
        const x = (i / 100) * Max; 
        const exponent = -0.5 * Math.pow((x - E) / S, 2);
        const probability = (1 / (S * Math.sqrt(2 * Math.PI))) * Math.exp(exponent);
        
        const svgX = (i / 100) * width;
        const svgY = height - (probability / peakProbability) * 160;
        
        points.push(`${svgX},${svgY}`);
    }

    const pathData = `M ${points.join(' L ')}`;
    
    return {
        linePath: pathData,
        fillPath: `${pathData} L 400 ${height} L 0 ${height} Z`,
        min: 0,
        max: Max
    };
  });

  deviationLines = computed(() => {
    const result = this.calcResult();
    if (!result || !result.maxPossibleValue) return [];

    const E = result.expectedValue;
    const S = result.standardDeviation;
    const Max = result.maxPossibleValue;
    const width = 400;

    const sigmaWidthPercent = S / Max;
    let offsets = [0]; 
    
    if (sigmaWidthPercent > 0.05) {
      offsets = [-1, 0, 1]; 
    }
    if (sigmaWidthPercent > 0.15) {
      offsets = [-2, -1, 0, 1, 2];
    }
    if (sigmaWidthPercent > 0.25) {
      offsets = [-3, -2, -1, 0, 1, 2, 3]; 
    }

    return offsets.map(multiplier => {
      const value = E + (multiplier * S);
      if (value < 0 || value > Max) return null;

      return {
        value: Math.round(value * 100) / 100,
        xPos: (value / Max) * width, 
        label: multiplier === 0 ? 'Avg' : `${multiplier > 0 ? '+' : ''}${multiplier}σ`,
        isMean: multiplier === 0
      };
    }).filter((line): line is NonNullable<typeof line> => line !== null);
  });

  private loadFromStorage(): Unit[] {
    const saved = localStorage.getItem('warhammer_army');
    return saved ? JSON.parse(saved) : [];
  }

  onUnitAdded(newUnit: Unit) {
    this.armyUnits.update(units => [newUnit, ...units]);
  }

  onEditUnit(unit: Unit) {
    const currentEdit = this.editingUnit();
    if (currentEdit && currentEdit.id === unit.id) {
      this.editingUnit.set(null);
    } else {
      this.editingUnit.set(unit);
    }
  }

  onSaveUnit(unitData: Unit) {
    if (this.editingUnit()) {
      this.armyUnits.update(units => 
        units.map(u => u.id === unitData.id ? unitData : u)
      );
      this.editingUnit.set(null); 
    } else {
      this.armyUnits.update(units => [unitData, ...units]);
    }
  }

  removeUnit(id: number) {
    this.armyUnits.update(units => units.filter(u => u.id !== id));
  }
}