import { Component, input, computed } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-progress-circle',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './progress-circle.component.html',
  styleUrls: ['./progress-circle.component.css']
})
export class ProgressCircleComponent {
  value = input.required<number>();
  max = input<number>(2000);
  
  radius = 18;
  circumference = 2 * Math.PI * this.radius;

  isOverLimit = computed(() => this.value() > this.max());

  offset = computed(() => {
    const progress = Math.min(this.value() / this.max(), 1);
    return this.circumference * (1 - progress);
  });
}