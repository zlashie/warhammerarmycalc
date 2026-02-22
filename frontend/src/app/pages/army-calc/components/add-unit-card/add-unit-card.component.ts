import { Component, Output, EventEmitter, input, Input, effect } from '@angular/core'; 
import { CommonModule } from '@angular/common';
import { ActionOrbComponent } from '../../../../shared/components/ui/action-orb/action-orb.component';
import { CardComponent } from '../../../../shared/components/ui/card/card.component';
import { StatInputComponent } from '../../../../shared/components/ui/stat-input/stat-input.component';
import { ToggleOrbComponent } from '../../../../shared/components/ui/toggle-orb/toggle-orb.component';

@Component({
  selector: 'app-add-unit-card',
  standalone: true,
  imports: [CommonModule, ActionOrbComponent, CardComponent, StatInputComponent, ToggleOrbComponent],
  templateUrl: './add-unit-card.component.html',
  styleUrls: ['./add-unit-card.component.css']
})
export class AddUnitCardComponent {
  unitToEdit = input<any | null>(null); 

  @Input() mode: 'add' | 'save' | 'delete' = 'add';
  @Output() action = new EventEmitter<void>();
  @Output() unitAdded = new EventEmitter<any>();

  constructor() {
    effect(() => {
      const unit = this.unitToEdit();
      if (unit) {
        this.currentUnit = JSON.parse(JSON.stringify(unit));
      } else {
        this.currentUnit = this.getInitialUnitState();
      }
    });
  }

  getIconName(): string {
    switch (this.mode) {
      case 'save': return 'save';
      case 'delete': return 'delete';
      case 'add': 
      default: return 'add';
    }
  }

  onClick() {
    this.action.emit();
  }

  private getInitialUnitState() {
    return {
      id: null as number | null,
      name: '',
      points: '',
      stats: {
        models: '', attacks: '', bsWs: '', strength: '', ap: '', damage: ''
      },
      toggles: {
        lethal: false,        
        sustained: false,     
        crit: 6,          
        rerollHits: 'NONE',    
        rerollWounds: 'NONE', 
        antiX: 6,         
        devastating: false,
        plusOneHit: false,
        plusOneWound: false,
        torrent: false   
      }
    };
  }

  currentUnit = this.getInitialUnitState();

  submitUnit() {
    const unitData = {
      ...this.currentUnit,
      id: this.currentUnit.id || Date.now() 
    };

    this.unitAdded.emit(unitData);
  }

  getFriendlyRerollLabel(value: string): string {
    const mapping: any = {
      'ONES': '1s',
      'FAIL': 'Fail',
      'ALL': 'Fish'
    };
  return mapping[value] || false; 
  } 

  setRerollFromFriendly(friendly: any) {
    const mapping: any = {
      '1s': 'ONES',
      'Fail': 'FAIL',
      'Fish': 'ALL'
    };

    this.currentUnit.toggles.rerollHits = mapping[friendly] || 'NONE';
  }

  getCritLabel(value: number): string | boolean {
    if (value <= 5 && value >= 2) return value + '+';
    return false; 
  }

  getFriendlyWoundRerollLabel(value: string): string {
    const mapping: any = {
      'ONES': '1s',
      'FAIL': 'Fail',
      'ALL': 'Fish'
    }
    return mapping[value] || false; 
  }

  setWoundRerollFromFriendly(friendly: any) {
    const mapping: any = {
      '1s': 'ONES',
      'Fail': 'FAIL',
      'Fish': 'ALL'
    }
    this.currentUnit.toggles.rerollWounds = mapping[friendly] || 'NONE';
  }

  setAntiXFromFriendly(f: any) { this.currentUnit.toggles.antiX = this.parsePlusValue(f, 6); }
  setCritFromFriendly(f: any) { this.currentUnit.toggles.crit = this.parsePlusValue(f, 6); }

  private parsePlusValue(val: any, fallback: number): number {
    if (!val) return fallback;
    const parsed = parseInt(val.toString().replace('+', ''), 10);
    return isNaN(parsed) ? fallback : parsed;
  }
}