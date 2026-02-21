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
  @Input() icon: string = '';
  @Input() options: string[] = []; 
  @Input() active: any = false; 
  
  @Output() activeChange = new EventEmitter<any>();

  showMenu: boolean = false;

  onToggle(): void {
    if (this.options && this.options.length > 0) {
      this.showMenu = !this.showMenu;
    } else {
      this.active = !this.active;
      this.activeChange.emit(this.active);
    }
  }

  selectOption(opt: string): void {
    this.active = opt;
    this.activeChange.emit(this.active);
    this.showMenu = false;
  }

  clear(): void {
    this.active = false;
    this.activeChange.emit(this.active);
    this.showMenu = false;
  }
}