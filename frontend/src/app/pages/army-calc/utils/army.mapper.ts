export class ArmyMapper {
  static toCalculationPayload(units: any[]) {
    return units.map(u => {
      const stats = u.stats || {};
      const rawBs = String(stats.bsWs || '').replace(/[^0-9]/g, '');
      
      return {
        unitName: u.name || 'Unnamed Unit',
        numberOfModels: parseInt(stats.models) || 0,
        attacksPerModel: parseInt(stats.attacks) || 0,
        bsValue: parseInt(rawBs) || 0,
      };
    });
  }
}