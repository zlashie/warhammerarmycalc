import { Component, signal, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { rxResource } from '@angular/core/rxjs-interop';
import { CalculatorService } from '../../../core/services/calculator.service';

@Component({
  selector: 'app-army-calc',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './army-calc.component.html',
  styleUrls: ['./army-calc.component.css']
})
export class ArmyCalcComponent {
  private calcService = inject(CalculatorService);
  numInput = signal(0);

  calc = rxResource({
    stream: () => this.calcService.getIncrement(this.numInput())
  });
}