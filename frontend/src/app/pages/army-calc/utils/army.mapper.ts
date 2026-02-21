export class ArmyMapper {
  static toCalculationPayload(units: any[]): any[] {
    return units.map(unit => {
      const sActive = unit.toggles.sustained;
      const rerollValue = unit.toggles.rerollHits;
      const rerollWounds = unit.toggles.rerollWounds;

      return {
        unitName: unit.name,
        numberOfModels: parseInt(unit.stats.models) || 0,
        attacksPerModel: parseInt(unit.stats.attacks) || 0,
        bsValue: parseInt(unit.stats.bsWs?.replace('+', '')) || 0,
        strength: parseInt(unit.stats.strength) || 4,
        damageValue: unit.stats.damage || "1",
        lethalHits: !!unit.toggles.lethal,
        sustainedHits: !!sActive,
        sustainedValue: typeof sActive === 'string' ? sActive : (sActive ? "1" : "0"),
        rerollType: typeof rerollValue === 'string' ? rerollValue : "NONE",
        critHitValue: unit.toggles.crit || 6,
        woundRerollType: typeof rerollWounds === 'string' ? rerollWounds : "NONE",
        devastatingWounds: !!unit.toggles.devastating,
        critWoundValue: unit.toggles.antiX || 6,
        plusOneToHit: !!unit.toggles.plusOneHit,
        plusOneToWound: !!unit.toggles.plusOneWound
      };
    });
  }
}