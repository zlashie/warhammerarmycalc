import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-action-orb',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './action-orb.component.html',
  styleUrls: ['./action-orb.component.css']
})
export class ActionOrbComponent {
  @Input() mode: 'add' | 'save' | 'remove' | 'delete' = 'add';
  @Output() action = new EventEmitter<void>();

  handleAction(event: Event) {
    event.stopPropagation();
    this.action.emit();
  }
}