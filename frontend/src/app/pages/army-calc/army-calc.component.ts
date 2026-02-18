import { Component, signal, inject, effect } from '@angular/core';
import { CommonModule } from '@angular/common';
import { CardComponent } from '../../shared/components/ui/card/card.component';
import { CalculatorService, CalcResult } from '../../../core/services/calculator.service';
import { AddUnitCardComponent } from './components/add-unit-card/add-unit-card.component';
import { LedgerCardComponent } from './components/ledger-card/ledger-card.component';
import { HitDistCardComponent } from './components/hit-dist-card/hit-dist-card.component';

@Component({
  selector: 'app-army-calc',
  standalone: true,
  imports: [
    CommonModule, 
    CardComponent, 
    AddUnitCardComponent, 
    LedgerCardComponent, 
    HitDistCardComponent
  ],
  templateUrl: './army-calc.component.html',
  styleUrls: ['./army-calc.component.css']
})
export class ArmyCalcComponent {
  private calcService = inject(CalculatorService);

  armyUnits = signal<any[]>(this.loadFromStorage());
  editingUnit = signal<any | null>(null);
  calcResult = signal<CalcResult | null>(null);
  isLoading = signal(false);

  constructor() {
    // Automatically recalculate whenever the army changes
    effect(() => {
      const units = this.armyUnits();
      localStorage.setItem('warhammer_army', JSON.stringify(units));
      if (units.length > 0) {
        this.performArmyCalculation();
      } else {
        this.calcResult.set(null);
      }
    });
  }

  private performArmyCalculation() {
    this.isLoading.set(true);
    
    const payload = this.armyUnits().map(u => ({
      unitName: u.name,
      numberOfModels: parseInt(u.stats.models) || 0,
      attacksPerModel: parseInt(u.stats.attacks) || 0,
      bsValue: parseInt(u.stats.bsWs) || 0
    }));

    this.calcService.calculate(payload).subscribe({
      next: (res) => {
        this.calcResult.set(res);
        this.isLoading.set(false);
      },
      error: () => this.isLoading.set(false)
    });
  }
  
  private loadFromStorage(): any[] {
    const saved = localStorage.getItem('warhammer_army');
    return saved ? JSON.parse(saved) : [];
  }

  onSaveUnit(unit: any) {
    if (unit.id) {
      this.armyUnits.update(units => units.map(u => u.id === unit.id ? unit : u));
    } else {
      unit.id = Date.now();
      this.armyUnits.update(units => [unit, ...units]);
    }
    this.editingUnit.set(null);
  }

  removeUnit(id: number) {
    this.armyUnits.update(units => units.filter(u => u.id !== id));
  }

  onEditUnit(unit: any) {
    this.editingUnit.set(unit);
  }
}