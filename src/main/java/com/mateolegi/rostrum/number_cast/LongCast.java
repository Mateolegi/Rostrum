package com.mateolegi.rostrum.number_cast;

public class LongCast implements NumberCast {

    @Override
    public Number cast(Object o) {
        if (o instanceof String) {
            Long.parseLong((String) o);
        }
        return ((Number) o).longValue();
    }
}
