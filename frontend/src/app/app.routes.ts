import { Routes } from '@angular/router';
import { HomeComponent } from './pages/home/home.component';
import { ArmyCalcComponent } from './pages/army-calc/army-calc.component';

export const routes: Routes = [
  { path: 'home', component: HomeComponent },
  { path: 'calculator', component: ArmyCalcComponent },
  { path: '', redirectTo: 'home', pathMatch: 'full' },
  { path: '**', redirectTo: 'home' }
];