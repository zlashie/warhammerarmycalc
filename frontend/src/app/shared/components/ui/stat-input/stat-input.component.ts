import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-stat-input',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './stat-input.component.html',
  styleUrls: ['./stat-input.component.css']
})
export class StatInputComponent {
  @Input({ required: true }) label: string = '';
  @Input() placeholder: string = '';
  @Input() value: string = '';
  
  @Output() valueChange = new EventEmitter<string>();

  onInput(newValue: string) {
    this.value = newValue;
    this.valueChange.emit(newValue);
  }
}