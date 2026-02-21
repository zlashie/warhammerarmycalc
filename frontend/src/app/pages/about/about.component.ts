import { Component } from '@angular/core';
import { CardComponent } from '../../shared/components/ui/card/card.component';

@Component({
  selector: 'app-about',
  standalone: true,
  imports: [
      CardComponent
  ],
  templateUrl: './about.component.html',
  styleUrls: ['./about.component.css']
})
export class AboutComponent {}