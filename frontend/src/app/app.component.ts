import { Component } from '@angular/core';
import { RouterOutlet, RouterLink, RouterLinkActive } from '@angular/router';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  template: `
    <nav style="padding: 1rem; background: #1a1a1a; color: white; display: flex; gap: 20px; border-bottom: 2px solid gold;">
      <a routerLink="/home" 
         routerLinkActive="active-link" 
         style="color: white; text-decoration: none;">HOME</a>
         
      <a routerLink="/calculator" 
         routerLinkActive="active-link" 
         style="color: white; text-decoration: none;">ARMY CALCULATOR</a>
    </nav>
    
    <main style="padding: 20px;">
      <router-outlet></router-outlet>
    </main>

    <style>
      .active-link { 
        border-bottom: 2px solid gold; 
        font-weight: bold; 
      }
    </style>
  `
})
export class AppComponent {}