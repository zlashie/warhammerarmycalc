import { Component, input, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { CardComponent } from '../card/card.component';

@Component({
  selector: 'app-prob-dist-card',
  standalone: true,
  imports: [CommonModule, CardComponent],
  templateUrl: './prob-dist-card.component.html',
  styleUrl: './prob-dist-card.component.css'
})
export class ProbDistCardComponent {
  data = input<number[]>([]);
  maxProb = computed(() => Math.max(...this.data(), 0.0001));
}