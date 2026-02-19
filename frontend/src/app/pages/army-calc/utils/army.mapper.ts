export class ArmyMapper {
  static toCalculationPayload(units: any[]): any[] {
    return units.map(unit => {
      const sActive = unit.toggles.sustained;

      return {
        unitName: unit.name,
        numberOfModels: parseInt(unit.stats.models) || 0,
        attacksPerModel: parseInt(unit.stats.attacks) || 0,
        bsValue: parseInt(unit.stats.bsWs?.replace('+', '')) || 0,

        // If 'active' is not false/null, sustainedHits is true
        sustainedHits: !!sActive,

        // If 'active' is a string (like "D3"), send that string.
        // If it's just boolean true, default to "1".
        // Otherwise send "0".
        sustainedValue: typeof sActive === 'string' ? sActive : (sActive ? "1" : "0")
      };
    });
  }
}