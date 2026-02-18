import { Component, input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { CardComponent } from '../card/card.component';

@Component({
  selector: 'app-prob-dist-card',
  standalone: true,
  imports: [CommonModule, CardComponent],
  template: `
    <app-card>
      <div class="dist-container">
        <div class="header">
          <ng-content select="[title]"></ng-content>
        </div>
        
        <div class="chart-area">
          @if (data().length > 0) {
            <div class="bars-wrapper">
              @for (prob of data(); track $index) {
                <div class="bar-group">
                  <div class="bar-hitbox" [title]="($index) + ' Hits: ' + (prob * 100).toFixed(2) + '%'">
                    <div class="bar" [style.height.%]="(prob / maxProb()) * 100"></div>
                  </div>
                  <span class="bar-label">{{ $index }}</span>
                </div>
              }
            </div>
          } @else {
            <div class="empty-state">No units in ledger</div>
          }
        </div>
      </div>
    </app-card>
  `,
  styles: [`
    .dist-container { padding: 10px; height: 250px; display: flex; flex-direction: column; }
    .chart-area { flex-grow: 1; display: flex; align-items: flex-end; padding-bottom: 20px; }
    .bars-wrapper { display: flex; align-items: flex-end; width: 100%; height: 100%; gap: 4px; }
    .bar-group { flex: 1; display: flex; flex-direction: column; align-items: center; height: 100%; justify-content: flex-end; }
    .bar-hitbox { width: 100%; height: 100%; display: flex; align-items: flex-end; cursor: help; }
    .bar { width: 100%; background: var(--gold-primary, #c6a15b); border-radius: 2px 2px 0 0; transition: height 0.3s ease; }
    .bar-label { font-size: 0.6rem; color: #666; margin-top: 4px; }
    .empty-state { width: 100%; text-align: center; opacity: 0.5; font-style: italic; }
  `]
})
export class ProbDistCardComponent {
  data = input<number[]>([]);
  maxProb = () => Math.max(...this.data(), 0.0001);
}