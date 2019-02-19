package com.mateolegi.rostrum.number_cast;

public class DoubleCast implements NumberCast {

    @Override
    public Number cast(Object o) {
        if (o instanceof String) {
            return Double.parseDouble((String) o);
        }
        return ((Number) o).doubleValue();
    }
}