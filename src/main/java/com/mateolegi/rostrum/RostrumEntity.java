package com.mateolegi.rostrum;

public class RostrumEntity {

    public static Class<T> clazz;

    public static <T> T find(Object id) {
        return Rostrum.find(clazz, id);
    }

    public RostrumEntity save(RostrumEntity entity) {
        return Rostrum.save(entity);
    }
}
