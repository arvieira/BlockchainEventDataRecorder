import { SignalRef } from 'vega';
import { TypedFieldDef } from '../../../channeldef';
import { VgEncodeEntry, VgValueRef } from '../../../vega.schema';
import { UnitModel } from '../../unit';
export declare function rectPosition(model: UnitModel, channel: 'x' | 'y' | 'theta' | 'radius'): VgEncodeEntry;
/**
 * Value Ref for binned fields
 */
export declare function rectBinRef({ fieldDef, scaleName, bandPosition, offset }: {
    fieldDef: TypedFieldDef<string>;
    scaleName: string;
    bandPosition: number | SignalRef;
    offset?: number | SignalRef;
}): VgValueRef;
//# sourceMappingURL=position-rect.d.ts.map