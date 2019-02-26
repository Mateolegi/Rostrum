package com.mateolegi.rostrum;

import com.google.common.base.CaseFormat;
import com.mateolegi.rostrum.number_cast.*;
import org.eclipse.persistence.config.QueryHints;
import org.eclipse.persistence.config.ResultType;
import org.eclipse.persistence.internal.helper.DatabaseField;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.persistence.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.sql.Timestamp;
import java.util.*;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * It fulfills the function of transforming the generic objects returned by the native JPA queries in a POJO with the
 * required attributes.
 * @author <a href="https://mateolegi.github.io">Mateo Leal</a>
 * @version 2.1.0
 */
public class EntityFactory {

    private static final Map<Class<?>, NumberCast> numberCast = new HashMap<>();
    private static final Logger LOGGER = Logger.getLogger(EntityFactory.class.getSimpleName());

    static {
        numberCast.put(Long.class, new LongCast());
        numberCast.put(Integer.class, new IntegerCast());
        numberCast.put(Double.class, new DoubleCast());
        numberCast.put(Float.class, new FloatCast());
    }

    @Contract(" -> fail")
    private EntityFactory() {
        throw new AssertionError("There are no instances for you!");
    }

    /**
     * Transforms a returned object through a native JPA query in a POJO. The POJO must have a constructor with the
     * parameters and the order of the query.
     * @param <T> POJO class to be returned
     * @param o object returned by the native query
     * @param clazz POJO class
     * @return POJO with the information of the object
     */
    public static <T> T reflect(final Object o, final Class<T> clazz) {
        return objectToDTO(o, clazz);
    }

    /**
     * Transforms a returned object by a native JPA query into a POJO. The POJO must have a constructor with the
     * parameters and the order of the query.
     * @param <T> POJO class to be returned
     * @param o object returned by the native query
     * @param clazz POJO class
     * @return POJO with the information of the object
     */
    public static <T> List<T> reflectList(final Object o, final Class<T> clazz) {
        List<T> results = new ArrayList<>();
        if (o instanceof List<?>) {
            List<?> l = (List<?>) o;
            l.forEach(i -> results.add(objectToDTO(i, clazz)));
        } else {
            results.add(objectToDTO(o, clazz));
        }
        return results;
    }

    /**
     * Execute a query and map the result to a DTO. Return a single result, if the query returns more than one record
     * or no record, the exceptions of {@code Query} are thrown.
     * @param <T> entity type
     * @param query query after assigning parameters
     * @param clazz class to which you want to transform the result of the query
     * @return instance
     * @since 2.0.0
     * @throws IllegalArgumentException if query is null
     * @throws NoResultException if there is no result
     * @throws NonUniqueResultException if there is more than one result
     * @throws IllegalStateException if it is called for an UPDATE or DELETE statement from the Java persistence query
     *         language
     * @throws QueryTimeoutException if the execution of the query exceeds the value of the query timeout and only the
     *         instruction is reversed
     * @throws TransactionRequiredException if a blocking mode other than {@code NONE} has been set and there is no
     *         transaction or the persistence context has not joined the transaction
     * @throws PessimisticLockException if the pessimistic blockade fails and the transaction is reversed
     * @throws LockTimeoutException if the pessimistic blockade fails and only the instruction will be reversed
     * @throws PersistenceException if the execution of the query exceeds the timeout value of the query and the
     *         transaction rolls back
     */
    @SuppressWarnings("unchecked")
    public static <T> T reflect(@NotNull("Query can't be null") final Query query, final Class<T> clazz) {
        query.setHint(QueryHints.RESULT_TYPE, ResultType.Map);
        Map<DatabaseField, ?> map = (Map<DatabaseField, ?>) query.getSingleResult();
        return mapToDTO(map, clazz);
    }

    /**
     * Execute a query and map the result to a DTO. Returns a single result, if the query returns more than one record
     * or no record, the exceptions of {@code Query} are thrown.
     * @param <T> entity type
     * @param query query after assigning parameters
     * @param clazz class to which you want to transform the result of the query
     * @param noResultExceptionCallback function that will be executed in the case that query does not get results
     * @return instance
     * @since 2.1.0
     * @throws IllegalArgumentException if query is null
     * @throws NonUniqueResultException if there is more than one result
     * @throws IllegalStateException if it is called for an UPDATE or DELETE statement from the Java persistence query
     *         language
     * @throws QueryTimeoutException if the execution of the query exceeds the value of the query timeout and only the
     *         instruction is reversed
     * @throws TransactionRequiredException if a blocking mode other than {@code NONE} has been set and there is no
     *         transaction or the persistence context has not joined the transaction
     * @throws PessimisticLockException if the pessimistic blockade fails and the transaction is reversed
     * @throws LockTimeoutException if the pessimistic blockade fails and only the instruction will be reversed
     * @throws PersistenceException if the execution of the query exceeds the timeout value of the query and the
     *         transaction rolls back
     */
    @SuppressWarnings("unchecked")
    public static <T> T reflect(@NotNull("Query can't be null") final Query query, final Class<T> clazz,
                                Function<NoResultException, T> noResultExceptionCallback) {
        try {
            query.setHint(QueryHints.RESULT_TYPE, ResultType.Map);
            Map<DatabaseField, ?> map = (Map<DatabaseField, ?>) query.getSingleResult();
            return mapToDTO(map, clazz);
        } catch (NoResultException e) {
            return noResultExceptionCallback.apply(e);
        }
    }

    /**
     * Execute a query and map the result to a DTO. Return a single result, if the query returns more than one record
     * or no record, the exceptions of {@code Query} are thrown.
     * @param <T> entity type
     * @param query query after assigning parameters
     * @param clazz class to which you want to transform the result of the query
     * @param noResultExceptionCallback function that will be executed in the case that query does not get any results
     * @param nonUniqueResultExceptionCallback function that will be executed in the case that the query gets more than
     *                                         one result
     * @return instance
     * @since 2.1.0
     * @throws IllegalArgumentException if query is null
     * @throws IllegalStateException if it is called for an UPDATE or DELETE statement from the Java persistence query
     *         language
     * @throws QueryTimeoutException if the execution of the query exceeds the value of the query timeout and only the
     *         instruction is reversed
     * @throws TransactionRequiredException if a blocking mode other than {@code NONE} has been set and there is no
     *         transaction or the persistence context has not joined the transaction
     * @throws PessimisticLockException if the pessimistic blockade fails and the transaction is reversed
     * @throws LockTimeoutException if the pessimistic blockade fails and the transaction is reversed
     * @throws PersistenceException if the execution of the query exceeds the timeout value of the query and the
     *         transaction rolls back
     */
    @SuppressWarnings("unchecked")
    public static <T> T reflect(@NotNull("Query can't be null") final Query query, final Class<T> clazz,
                                Function<NoResultException, T> noResultExceptionCallback,
                                Function<NonUniqueResultException, T> nonUniqueResultExceptionCallback) {
        try {
            query.setHint(QueryHints.RESULT_TYPE, ResultType.Map);
            Map<DatabaseField, ?> map = (Map<DatabaseField, ?>) query.getSingleResult();
            return mapToDTO(map, clazz);
        } catch (NoResultException e) {
            return noResultExceptionCallback.apply(e);
        } catch (NonUniqueResultException e) {
            return nonUniqueResultExceptionCallback.apply(e);
        }
    }

    /**
     * Executes a query and map the result to a DTO.
     * @param <T> entity type
     * @param query query after assigning parameters
     * @param clazz class to which you want to transform the result of the query
     * @return instance
     * @since 2.0.0
     * @throws IllegalArgumentException if query is null
     * @throws IllegalStateException if it is called for an UPDATE or DELETE statement from the Java persistence query
     *         language
     * @throws QueryTimeoutException if the execution of the query exceeds the value of the query timeout and only the
     *         instruction is reversed
     * @throws TransactionRequiredException if a blocking mode other than {@code NONE} has been set and there is no
     *         transaction or the persistence context has not joined the transaction
     * @throws PessimisticLockException if the pessimistic blockade fails and the transaction is reversed
     * @throws LockTimeoutException if the pessimistic blockade fails and the transaction is reversed
     * @throws PersistenceException if the execution of the query exceeds the timeout value of the query and the
     *         transaction rolls back
     */
    @SuppressWarnings("unchecked")
    public static <T> List<T> reflectList(@NotNull("Query can't be null") final Query query, final Class<T> clazz) {
        List<T> response = new ArrayList<>();
        query.setHint(QueryHints.RESULT_TYPE, ResultType.Map);
        List<Map<DatabaseField, ?>> res = query.getResultList();
        res.forEach(map -> response.add(mapToDTO(map, clazz)));
        return response;
    }

    /**
     * Transforms a map returned by the Query in an instance of the class given by parameter.
     * @param <T> entity type
     * @param map map with the result of the query
     * @param clazz class to which you are going to map
     * @return instance with map values
     * @throws IllegalArgumentException if map or class is null
     * @since 2.0.0
     */
    @Nullable
    private static <T> T mapToDTO(@NotNull("Map can't be null") Map<DatabaseField, ?> map,
                                  @NotNull("Class can't be null") final Class<T> clazz) {
        try {
            T instance = clazz.newInstance();
            List<Field> fields = Arrays.asList(clazz.getDeclaredFields());
            map.entrySet().stream()
                    .filter(field -> getField(field.getKey().getName(), fields) != null)
                    .forEach(i -> {
                        Field field = getField(i.getKey().getName(), fields);
                        setValue(field, i.getValue(), instance);
                    });
            return instance;
        } catch (InstantiationException | IllegalAccessException e) {
            LOGGER.log(Level.SEVERE, "Error creating an instance of " + clazz.getName(), e);
        }
        return null;
    }

    /**
     * Assign the value to the instance attribute.
     * @param <T> entity type
     * @param field field to which the value is assigned
     * @param o value to be assigned
     * @param instance instance of the field
     * @throws IllegalArgumentException if field is null
     * @since 2.0.0
     */
    private static <T> void setValue(@NotNull("Field can't be null") Field field, Object o, T instance) {
        try {
            boolean isAccessible = field.isAccessible();
            field.setAccessible(true);
            field.set(instance, castParam(o, field.getType()));
            field.setAccessible(isAccessible);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            LOGGER.log(Level.WARNING, "Error setting the value " + o + " in the " + field.getName() + " field.", e);
        }
    }

    /**
     * Find matches between the name of the column and the name of the attribute. Regardless of whether it is
     * {@code camelCase} or {@code SNAKE_CASE}.
     * @param databaseField column name
     * @param fields list with the fields of the class
     * @return the field of the class that matches, otherwise {@code null}
     * @throws IllegalArgumentException if field lists is null
     * @since 2.0.0
     */
    private static Field getField(String databaseField, @NotNull("Field list can't be null") List<Field> fields) {
        return fields.parallelStream()
                .filter(field -> field.getName().equalsIgnoreCase(getNameField(databaseField)))
                .findFirst()
                .orElse(null);
    }

    /**
     * Valid if the name of the column is in {@code SNAKE_CASE} and transforms it to {@code camelCase} and validates if
     * they have the same value.
     * @param queryName name of the query column
     * @return transformed name of the column
     * @throws IllegalArgumentException if queryName is null
     * @since 2.0.0
     */
    private static String getNameField(@NotNull("QueryName is null") final String queryName) {
        if (queryName.contains("_")) {
            return CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, queryName);
        }
        return queryName;
    }

    /**
     * Build the POJO from the data of the native query object.
     * @param <T> entity type
     * @param o object returned by the native query
     * @param clazz POJO class
     * @return POJO with the information of the object
     * @throws ClassCastException if the object can't be instantiated
     * @throws IllegalArgumentException if there is no constructor for the query attributes
     */
    private static <T> T objectToDTO(Object o, Class<T> clazz) {
        Object[] oA;
        try {
            oA = (Object[]) o;
        } catch (ClassCastException e) {
            return castSimpleObject(o, clazz);
        }
        int length = oA.length;
        // Get the right constructor to build the POJO
        Constructor<T> c = getConstructor(clazz, length);
        if (c != null) {
            try {
                // Create an instance of the POJO with the constructor found
                return c.newInstance(castParams(oA, c.getParameterTypes()));
            } catch (Exception e) {
                // If an error occurs during initialization
                LOGGER.log(Level.SEVERE, "An error occurred instantiating the object.", e);
                throw new ClassCastException(e.getMessage());
            }
        } else {
            throw new IllegalArgumentException("No constructor was found for the number of elements.");
        }
    }

    /**
     * Cast a simple object.
     * @param <T> entity type
     * @param o value returned by query
     * @param clazz class to be casted
     * @return casted value
     */
    @SuppressWarnings("unchecked")
    private static <T> T castSimpleObject(Object o, Class<T> clazz) {
        return (T) castParam(o, clazz);
    }

    /**
     * Gets the first constructor with the number of parameters equal to the number of attributes of the object of the
     * query.
     * @param <T> entity type
     * @param clazz POJO class
     * @param parameterCount number of attributes of the native query object
     * @return the constructor
     * @throws IllegalArgumentException if class is null
     */
    @SuppressWarnings("unchecked")
    private static <T> Constructor<T> getConstructor(@NotNull("Class can't be null") Class<T> clazz,
                                                     int parameterCount) {
        Constructor<T>[] constructors = (Constructor<T>[]) clazz.getConstructors();
        return Arrays.stream(constructors)
                .filter(i -> i.getParameterCount() == parameterCount)
                .findFirst()
                .orElse(null);
    }

    /**
     * Cast each of the values to their respective class within the POJO.
     * @param p array with the attributes of the native query object
     * @param classes array with the respective classes for each attribute
     * @return array of objects with casted attributes
     * @throws IllegalArgumentException if attributes array is null
     */
    private static Object[] castParams(@NotNull("Attributes array can't be null") Object[] p, Class<?>[] classes) {
        Object[] castedParams = new Object[p.length];
        for (int i = 0; i < p.length; i++) {
            castedParams[i] = castParam(p[i], classes[i]);
        }
        return castedParams;
    }

    /**
     * Cast numbers to the class sent by parameter.
     * @param o number object
     * @param clazz class to be casted
     * @return casted number
     * @since 2.0.0
     */
    @Nullable
    private static Object castParam(Object o, Class<?> clazz) {
        try {
            if (o == null) {
                return null;
            } else if (clazz == Number.class || clazz.getSuperclass() == Number.class) {
                // Special cases to cast the numbers
                return castNumber(o, clazz);
            } else if (clazz == Date.class && o.getClass() == Timestamp.class) {
                Timestamp timestamp = (Timestamp) o;
                return new Date(timestamp.getTime());
            } else if (clazz == String.class && !(o instanceof String)) {
                return o.toString();
            } else {
                return clazz.cast(o);
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "An error occurred by casting the object with value " + o + " to class "
                    + clazz.getName(), e);
            throw e;
        }
    }

    /**
     * Cast numbers to the class sent by parameter.
     * @param o number object
     * @param clazz class to be casted
     * @return casted number
     */
    private static Number castNumber(Object o, Class<?> clazz) {
        return numberCast.get(clazz).cast(o);
    }
}
