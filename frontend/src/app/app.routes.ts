import { Routes } from '@angular/router';
import { HomeComponent } from './pages/home/home.component';
import { ArmyCalcComponent } from './pages/army-calc/army-calc.component';
import { CompareArmiesComponent } from './pages/comparearmies/comparearmies.component';
import { AboutComponent } from './pages/about/about.component';
import { ArmyManagerComponent } from './pages/army-manager/army-manager.component';

export const routes: Routes = [
  { path: 'home', component: HomeComponent },
  { path: 'calculator', component: ArmyCalcComponent },
  { path: 'comparearmies', component: CompareArmiesComponent},
  { path: 'armymanager', component: ArmyManagerComponent},
  { path: 'about', component: AboutComponent},
  { path: '', redirectTo: 'home', pathMatch: 'full' },
  { path: '**', redirectTo: 'home' }
];