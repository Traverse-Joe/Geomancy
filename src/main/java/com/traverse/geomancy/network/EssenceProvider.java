package com.traverse.geomancy.network;

import com.traverse.geomancy.essence.EssenceCharge;

public interface EssenceProvider {
    EssenceCharge charge();

    int capacity();

    // simulate=true reports what would be removed without mutating state, used by
    // pull-side callers to decide whether a transfer is worth attempting.
    int extract(int amount, boolean simulate);
}
