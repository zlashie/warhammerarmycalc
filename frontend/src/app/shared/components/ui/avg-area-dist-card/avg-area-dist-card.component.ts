import { Component, input, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { CardComponent } from '../card/card.component';

export interface DistPoint {
  xLabel: string | number;
  average: number;
  lower80: number;
  upper80: number;
}

/**
 * A reusable chart component that renders a line graph with a shaded confidence interval.
 * <p>
 * This component uses raw SVG math to draw the graph, ensuring complete control over styling
 * and performance without the need for external charting libraries.
 * It expects a normalized coordinate system where:
 * <ul>
 * <li><strong>X-Axis:</strong> Evenly spaced points based on the array index.</li>
 * <li><strong>Y-Axis:</strong> Scaled dynamically based on the maximum 'upper80' value.</li>
 * </ul>
 */
@Component({
  selector: 'app-avg-area-dist-card',
  standalone: true,
  imports: [CommonModule, CardComponent],
  templateUrl: './avg-area-dist-card.component.html',
  styleUrl: './avg-area-dist-card.component.css'
})
export class AvgAreaDistCardComponent {
  
  // --- Inputs ---
  data = input<DistPoint[]>([]);
  title = input<string>('');
  yAxisLabel = input<string>('Avg Wounds');
  xAxisLabel = input<string>('Toughness');

  // --- Chart Configuration ---
  // Fixed SVG viewbox dimensions. The CSS scales this to fit the container.
  readonly WIDTH = 1000;
  readonly HEIGHT = 400;
  // Padding ensures axis labels and titles are not clipped.
  readonly PADDING = { top: 20, right: 20, bottom: 90, left: 100 };

  /**
   * Calculates the vertical scale ceiling.
   * Finds the highest value in the upper confidence interval and adds 10% headroom
   * to prevent the highest peak from touching the top of the graph.
   */
  maxY = computed(() => {
    const points = this.data();
    if (!points.length) return 10;
    
    const maxVal = Math.max(...points.map(p => p.upper80));
    return maxVal * 1.1; 
  });

  /**
   * Generates the SVG path command ('d' attribute) for the shaded confidence area.
   * <p>
   * The path is constructed in two phases:
   * 1. Moves forward (Left -> Right) along the 'upper80' points.
   * 2. Moves backward (Right -> Left) along the 'lower80' points.
   * 3. Closes the shape with 'Z', creating a fillable polygon.
   */
  areaPath = computed(() => {
    const points = this.data();
    if (!points.length) return '';

    // Phase 1: Top line (Upper bound)
    const upperLine = points.map((p, i) => {
      const { x, y } = this.getCoords(i, p.upper80);
      return `${i === 0 ? 'M' : 'L'} ${x} ${y}`;
    }).join(' ');

    // Phase 2: Bottom line (Lower bound), reversed
    const lowerLine = [...points].reverse().map((p, i) => {
      const originalIndex = points.length - 1 - i;
      const { x, y } = this.getCoords(originalIndex, p.lower80);
      return `L ${x} ${y}`;
    }).join(' ');

    return `${upperLine} ${lowerLine} Z`;
  });

  /**
   * Generates the SVG path command for the central 'Average' line.
   * This is a simple polyline moving Left -> Right.
   */
  avgLinePath = computed(() => {
    return this.data().map((p, i) => {
      const { x, y } = this.getCoords(i, p.average);
      return `${i === 0 ? 'M' : 'L'} ${x} ${y}`;
    }).join(' ');
  });

  /**
   * Generates 5 evenly spaced horizontal grid lines.
   * Returns their Y-position (for the <line>) and value label (for the <text>).
   */
  gridLines = computed(() => {
    const lines = [];
    const stepCount = 5; 
    
    for (let i = 0; i < stepCount; i++) {
        const yVal = (this.maxY() / (stepCount - 1)) * i;
        
        // Calculate SVG Y position relative to the graph height
        const normalizedY = (yVal / this.maxY());
        const usableHeight = this.HEIGHT - this.PADDING.top - this.PADDING.bottom;
        const yPos = (this.HEIGHT - this.PADDING.bottom) - (normalizedY * usableHeight);
        
        lines.push({ y: yPos, value: yVal.toFixed(1) });
    }
    return lines;
  });

  /**
   * Transforms a data point into SVG coordinates.
   * <p>
   * Note: SVG coordinate system starts at (0,0) in the top-left corner.
   * Therefore, Y-values must be inverted: (Height - Padding - Value).
   * * @param index The array index (used for X position)
   * @param value The raw data value (used for Y position)
   */
  private getCoords(index: number, value: number) {
    const usableWidth = this.WIDTH - this.PADDING.left - this.PADDING.right;
    const usableHeight = this.HEIGHT - this.PADDING.top - this.PADDING.bottom;
    const count = this.data().length;

    // X: Evenly distributed across the usable width
    const x = this.PADDING.left + (index / (count - 1)) * usableWidth;
    
    // Y: Inverted scale (0 is top)
    const y = (this.HEIGHT - this.PADDING.bottom) - (value / this.maxY()) * usableHeight;
    
    return { x, y };
  }
}