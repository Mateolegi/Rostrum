package com.mateolegi.rostrum.number_cast;

public class IntegerCast implements NumberCast {

    @Override
    public Number cast(Object o) {
        if (o instanceof String) {
            return Integer.parseInt((String) o);
        }
        return ((Number) o).intValue();
    }
}