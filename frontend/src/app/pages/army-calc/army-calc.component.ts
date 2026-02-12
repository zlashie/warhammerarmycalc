import { Component, signal, inject, effect } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CalculatorService } from '../../../core/services/calculator.service';

@Component({
  selector: 'app-army-calc',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './army-calc.component.html'
})

export class ArmyCalcComponent {
  private calcService = inject(CalculatorService);
  
  numInput = signal(0);
  result = signal<number | undefined>(undefined);
  isLoading = signal(false);

  constructor() {
    effect(() => {
      const val = this.numInput();
      this.isLoading.set(true);
      
      this.calcService.getIncrement(val).subscribe({
        next: (val) => {
          this.result.set(val);
          this.isLoading.set(false);
        },
        error: () => this.isLoading.set(false)
      });
    });
  }

  onInput(val: string) {
  const num = Number(val);
  console.log('UI Sending:', num);
  this.numInput.set(num);
  }
}