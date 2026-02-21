import { Component, signal, inject, effect, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { CalculatorService, CalcResult } from '../../../core/services/calculator.service';
import { CardComponent } from '../../shared/components/ui/card/card.component';
import { AddUnitCardComponent } from './components/add-unit-card/add-unit-card.component';
import { LedgerCardComponent } from './components/ledger-card/ledger-card.component';
import { HitDistCardComponent } from './components/hit-dist-card/hit-dist-card.component';
import { WoundDistCardComponent } from './components/wound-dist-card/wound-dist-card.component';
import { DamageDistCardComponent } from './components/damage-dist-card.component/damage-dist-card.component';
import { ToughnessDistCardComponent } from './components/toughness-dist-card.component/toughness-dist-card.component';
import { SaveDistCardComponent } from './components/save-dist-card/save-dist-card.component';
import { ArmyStoreService } from './services/army-store.service'; 
import { ArmyMapper } from './utils/army.mapper';               

@Component({
  selector: 'app-army-calc',
  standalone: true,
  imports: [
    CommonModule,
    CardComponent,
    AddUnitCardComponent,
    LedgerCardComponent,
    HitDistCardComponent,
    WoundDistCardComponent,
    DamageDistCardComponent,
    ToughnessDistCardComponent,
    SaveDistCardComponent
],
  templateUrl: './army-calc.component.html',
  styleUrls: ['./army-calc.component.css']
})
export class ArmyCalcComponent {
  private store = inject(ArmyStoreService);
  private calcService = inject(CalculatorService);

  armyUnits = this.store.units;
  editingUnit = this.store.selectedUnit;

  totalPoints = computed(() => {
    return this.armyUnits().reduce((sum, unit) => {
      const pts = parseFloat(unit.points) || 0;
      return sum + pts;
    }, 0);
  });
  
  calcResult = signal<CalcResult | null>(null);
  isLoading = signal(false);

  constructor() {
    effect(() => {
      const units = this.armyUnits();
      const selected = this.editingUnit();
      
      this.runCalculation(selected ? [selected] : units);
    }, { allowSignalWrites: true });
  }

  private runCalculation(units: any[]) {
    if (units.length === 0) {
      this.calcResult.set(null);
      return;
    }

    this.isLoading.set(true);
    const payload = ArmyMapper.toCalculationPayload(units);

    this.calcService.calculate(payload).subscribe({
      next: (res) => {
        this.calcResult.set(res);
        this.isLoading.set(false);
      },
      error: () => this.isLoading.set(false)
    });
  }

  onSaveUnit(unit: any) { 
    this.store.upsertUnit(unit); 
    this.store.selectedUnit.set(unit); 
  }

  removeUnit(id: number) { this.store.removeUnit(id); }
  onEditUnit(unit: any | null) { this.store.selectedUnit.set(unit); }
}