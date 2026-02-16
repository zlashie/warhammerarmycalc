import { Component, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActionOrbComponent } from '../../../../shared/components/ui/action-orb/action-orb.component';
import { CardComponent } from '../../../../shared/components/ui/card/card.component';
import { StatInputComponent } from '../../../../shared/components/ui/stat-input/stat-input.component';

@Component({
  selector: 'app-add-unit-card',
  standalone: true,
  imports: [CommonModule, ActionOrbComponent, CardComponent, StatInputComponent],
  templateUrl: './add-unit-card.component.html',
  styleUrls: ['./add-unit-card.component.css']
})
export class AddUnitCardComponent {
  @Output() unitAdded = new EventEmitter<any>();

  currentUnit = {
    stats: {
      models: '',
      attacks: '',
      bsWs: '',
      strength: '',
      ap: '',
      damage: ''
    }
  };

  submitUnit() {
    this.unitAdded.emit({ ...this.currentUnit, id: Date.now() });

    this.currentUnit = {
      stats: {
        models: '',
        attacks: '',
        bsWs: '',
        strength: '',
        ap: '',
        damage: ''
      }
    };
  }
}