package com.mateolegi.rostrum.number_cast;

public class FloatCast implements NumberCast {

    @Override
    public Number cast(Object o) {
        if (o instanceof String) {
            return Float.parseFloat((String) o);
        }
        return ((Number) o).floatValue();
    }
}
