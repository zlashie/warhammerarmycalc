import { Component } from '@angular/core';
import { CardComponent } from '../../shared/components/ui/card/card.component';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [
    CardComponent
],
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.css']
})
export class HomeComponent {}