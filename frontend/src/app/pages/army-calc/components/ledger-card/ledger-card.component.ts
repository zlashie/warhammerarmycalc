import { Component, input, output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { CardComponent } from '../../../../shared/components/ui/card/card.component';

@Component({
  selector: 'app-ledger-card',
  standalone: true,
  imports: [CommonModule, CardComponent],
  templateUrl: './ledger-card.component.html',
  styleUrls: ['./ledger-card.component.css']
})
export class LedgerCardComponent {
  units = input.required<any[]>();
  activeUnit = input<any | null>(null);
  
  deleteUnit = output<number>();

  editUnit = output<any | null>(); 

  onDelete(id: number, event: MouseEvent) {
    event.stopPropagation();
    this.deleteUnit.emit(id);
  }

  onEdit(unit: any) {
    const currentActive = this.activeUnit();
    
    if (currentActive && currentActive.id === unit.id) {
      this.editUnit.emit(null);
    } else {
      this.editUnit.emit(unit);
    }
  }
}