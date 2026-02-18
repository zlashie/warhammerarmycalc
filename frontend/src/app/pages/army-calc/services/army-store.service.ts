import { Injectable, signal, effect } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class ArmyStoreService {
  private readonly STORAGE_KEY = 'warhammer_army';
  
  units = signal<any[]>(this.load());
  selectedUnit = signal<any | null>(null);

  constructor() {
    effect(() => {
      localStorage.setItem(this.STORAGE_KEY, JSON.stringify(this.units()));
    });
  }

  upsertUnit(unit: any) {
    this.units.update(prev => {
      const exists = prev.find(u => u.id === unit.id);
      return exists 
        ? prev.map(u => u.id === unit.id ? unit : u) 
        : [{ ...unit, id: unit.id || Date.now() }, ...prev];
    });
    this.selectedUnit.set(null); 
  }

  removeUnit(id: number) {
    this.units.update(prev => prev.filter(u => u.id !== id));
    if (this.selectedUnit()?.id === id) this.selectedUnit.set(null);
  }

  private load(): any[] {
    const saved = localStorage.getItem(this.STORAGE_KEY);
    return saved ? JSON.parse(saved) : [];
  }
}