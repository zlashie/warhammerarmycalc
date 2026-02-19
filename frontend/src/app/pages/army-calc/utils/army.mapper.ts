export class ArmyMapper {
  static toCalculationPayload(units: any[]): any[] {
    return units.map(unit => {
      const sActive = unit.toggles.sustained;
      const rerollValue = unit.toggles.rerollHits;

      return {
        unitName: unit.name,
        numberOfModels: parseInt(unit.stats.models) || 0,
        attacksPerModel: parseInt(unit.stats.attacks) || 0,
        bsValue: parseInt(unit.stats.bsWs?.replace('+', '')) || 0,
        damageValue: unit.stats.damage || "1",
        sustainedHits: !!sActive,
        sustainedValue: typeof sActive === 'string' ? sActive : (sActive ? "1" : "0"),
        rerollType: typeof rerollValue === 'string' ? rerollValue : "NONE",
        critHitValue: unit.toggles.crit || 6
      };
    });
  }
}