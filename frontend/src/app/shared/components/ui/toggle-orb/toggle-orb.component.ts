import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-toggle-orb',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './toggle-orb.component.html',
  styleUrls: ['./toggle-orb.component.css']
})

export class ToggleOrbComponent {
  @Input({ required: true }) label: string = '';
  @Input({ required: true }) abbreviation: string = '';
  @Input() active: boolean = false;
  
  @Output() activeChange = new EventEmitter<boolean>();

  onToggle() {
    this.active = !this.active;
    this.activeChange.emit(this.active);
  }
}