import { Component, signal, inject, effect } from '@angular/core';
import { CommonModule } from '@angular/common';
import { CalculatorService, CalcResult } from '../../../core/services/calculator.service';
import { AddUnitCardComponent } from './components/add-unit-card/add-unit-card.component';
import { LedgerCardComponent } from './components/ledger-card/ledger-card.component';
import { HitDistCardComponent } from './components/hit-dist-card/hit-dist-card.component';

@Component({
  selector: 'app-army-calc',
  standalone: true,
  imports: [
    CommonModule,  
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
    const units = this.armyUnits();
    if (!units || units.length === 0) {
      this.calcResult.set(null);
      return;
    }

    this.isLoading.set(true);
    
    try {
      const payload = units.map(u => {
        const stats = u.stats || {};
        
        const rawBs = String(stats.bsWs || '').replace(/[^0-9]/g, '');
        
        return {
          unitName: u.name || 'Unnamed Unit',
          numberOfModels: parseInt(stats.models) || 0,
          attacksPerModel: parseInt(stats.attacks) || 0,
          bsValue: parseInt(rawBs) || 0
        };
      });

      this.calcService.calculate(payload).subscribe({
        next: (res) => {
          this.calcResult.set(res);
          this.isLoading.set(false);
        },
        error: (err) => {
          console.error('Backend rejected the data:', err);
          this.isLoading.set(false);
        }
      });
    } catch (e) {
      console.error('Mapping failed - check unit structure:', e);
      this.isLoading.set(false);
    }
  }
  
  private loadFromStorage(): any[] {
    const saved = localStorage.getItem('warhammer_army');
    return saved ? JSON.parse(saved) : [];
  }

  onSaveUnit(unit: any) {
    const unitToSave = { ...unit };
    
    this.armyUnits.update(units => {
      if (unitToSave.id && units.some(u => u.id === unitToSave.id)) {
        return units.map(u => u.id === unitToSave.id ? unitToSave : u);
      } else {
        unitToSave.id = unitToSave.id || Date.now();
        return [unitToSave, ...units];
      }
    });
    this.editingUnit.set(null);
  }

  removeUnit(id: number) {
    this.armyUnits.update(units => units.filter(u => u.id !== id));
  }

  onEditUnit(unit: any) {
    this.editingUnit.set(unit);
  }
}