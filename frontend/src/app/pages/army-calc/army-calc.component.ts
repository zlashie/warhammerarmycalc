import { Component, signal } from '@angular/core';
import { httpResource } from '@angular/common/http';
import { FormsModule } from '@angular/forms';

@Component({
  standalone: true,
  imports: [FormsModule],
  template: `
    <h2>Warhammer Army Calculator</h2>
    <input type="number" 
           [ngModel]="numInput()" 
           (ngModelChange)="numInput.set($event)">
    
    @if (calc.isLoading()) {
      <p>Contacting Java 25 backend...</p>
    } @else if (calc.value() !== undefined) {
      <h3>Result: {{ calc.value() }}</h3>
    }
  `
})
export class ArmyCalcComponent {
  numInput = signal(0);
  calc = httpResource<number>(() => 
    `http://localhost:8080/api/increment/${this.numInput()}`
  );
}