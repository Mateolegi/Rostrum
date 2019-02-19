package com.mateolegi.rostrum;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.Map;
import java.util.Objects;

/**
 * @author <a href="https://mateolegi.github.io"> Mateo Leal </a>
 */
public class FilteredQuery {

    private FilteredQuery() {
        throw new AssertionError("There are no instances for you!");
    }

    /**
     * Create a {@code TypedQuery} filtered with the parameters sent in the {@code map}.
     * In the case that one of the parameters is not recognized, it will be omitted. <br>
     * The structure of the map is as follows:
     * <ul>
     *   <li> key: name of the attribute as it is in the entity. </li>
     *   <li> value: object with the value to be compared. </li>
     * </ul>
     * @param <E> class of the consulted entity
     * @param clazz class of the consulted entity
     * @param queryParams parameter to make the filter
     * @return query with applied filters
     */
    public static <E> TypedQuery<E> createFilteredQuery(Class<E> clazz, EntityManager entityManager,
                                                        Map<String, Object> queryParams) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<E> query = builder.createQuery(clazz);
        Root<E> from = query.from(clazz);
        query.select(from).where(filter(from, builder, queryParams));
        return entityManager.createQuery(query);
    }

    /**
     * Convert a {@code MultivaluedMap} of JAX-RS into a {@code Map}.
     * In the case that there are duplicate values, only the first will be taken. <br>
     * @param multivaluedMap map of JAX-RS
     * @return map
     */
    /*public static Map<String, Object> multivaluedMapToMap(MultivaluedMap<String, String> multivaluedMap) {
        return multivaluedMap.keySet().parallelStream()
                .collect(Collectors.toMap(key -> key, key -> multivaluedMap.getFirst(key)));
    }*/

    /**
     * Applies the filters sent to the query. <br>
     * @param from entity reference
     * @param builder object used to build the query
     * @param queryParams parameters for the filter
     * @return fix with filters
     */
    private static <E> Predicate[] filter(Root<E> from, CriteriaBuilder builder, Map<String, Object> queryParams) {
        return queryParams.entrySet().parallelStream()
                .map(entry -> {
                    try {
                        return builder.equal(from.get(entry.getKey()), entry.getValue());
                    } catch (IllegalArgumentException e) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .toArray(Predicate[]::new);
    }
}