package com.mateolegi.rostrum.number_cast;

/**
 * @author <a href="https://mateolegi.github.io"> Mateo Leal </a>
 */
public interface NumberCast {

    /**
     * Transform a number that implements {@code java.lang.Number} in the implementation reference
     * for {@code NumberCast} <br>
     * @param o object to be cast
     * @return number
     */
    Number cast(Object o);
}