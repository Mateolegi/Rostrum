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
 * Esta clase tiene el objetivo de transformar los objetos genéricos retornados por
 * las consultas nativas de JPA en un POJO con los atributos requeridos <br>
 * Creado el 1/12/2018 a las  12:05:12 p. m. <br>
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
        throw new AssertionError("¡No hay instancias para ti!");
    }

    /**
     * Transforma un objecto retornado por una consulta nativa de JPA en un POJO.<br>
     * El POJO debe tener un constructor con los parametros y el orden de la consulta.<br>
     * Creado el 1/12/2018 a las  11:58:00 a. m. <br>
     * @param <T> Clase del POJO que se va a retornar
     * @param o objeto retornado por la consulta nativa
     * @param clazz clase del POJO
     * @return POJO con la información del objeto
     * @author <a href="https://mateolegi.github.io">Mateo Leal</a>
     */
    public static <T> T reflect(final Object o, final Class<T> clazz) {
        return objectToDTO(o, clazz);
    }

    /**
     * Transforma un objecto retornado por una consulta nativa de JPA en un POJO.<br>
     * El POJO debe tener un constructor con los parametros y el orden de la consulta.<br>
     * Creado el 1/12/2018 a las  11:58:00 a. m. <br>
     * @param <T> Clase del POJO que se va a retornar
     * @param o objeto retornado por la consulta nativa
     * @param clazz clase del POJO
     * @return POJO con la información del objeto
     * @author <a href="https://mateolegi.github.io">Mateo Leal</a>
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
     * Ejecuta una consulta y mapea el resultado en un DTO.
     * Retorna un único resultado, en caso que la consulta devuelva más
     * de un registro o ningún registro se lanzan las excepciones de {@code Query} <br>
     * Creado el 12/12/2018 a las  3:27:38 p. m. <br>
     * @param query después de asignarle los parámetros
     * @param clazz clase a la que se quiere transformar el resultado de la consulta
     * @return instancia de la clase
     * @author <a href="https://mateolegi.github.io">Mateo Leal</a>
     * @since 2.0.0
     * @throws IllegalArgumentException if query is null
     * @throws NoResultException si no hay resultado
     * @throws NonUniqueResultException si hay más de un resultado
     * @throws IllegalStateException si se llama para una instrucción UPDATE o
     * 		   DELETE del lenguaje de consulta de persistencia de Java
     * @throws QueryTimeoutException si la ejecución de la consulta supera el
     * 		   valor del tiempo de espera de la consulta y solo se revierte la
     * 		   instrucción
     * @throws TransactionRequiredException si se ha establecido un modo de
     * 		   bloqueo distinto de {@code NONE} y no hay transacción o el
     * 		   contexto de persistencia no se ha unido a la transacción
     * @throws PessimisticLockException si el bloqueo pesimista falla y
     * 		   la transacción se revierte
     * @throws LockTimeoutException si el bloqueo pesimista falla y solo
     * 		   se revertirá la instrucción
     * @throws PersistenceException si la ejecución de la consulta supera el
     * 		   valor de tiempo de espera de la consulta y la transacción se retrotrae
     */
    @SuppressWarnings("unchecked")
    public static <T> T reflect(@NotNull("Query can't be null") final Query query, final Class<T> clazz) {
        query.setHint(QueryHints.RESULT_TYPE, ResultType.Map);
        Map<DatabaseField, ?> map = (Map<DatabaseField, ?>) query.getSingleResult();
        return mapToDTO(map, clazz);
    }

    /**
     * Ejecuta una consulta y mapea el resultado en un DTO.
     * Retorna un único resultado, en caso que la consulta devuelva más
     * de un registro o ningún registro se lanzan las excepciones de {@code Query} <br>
     * Creado el 24/01/2019 a las  11:42:00 a. m. <br>
     * @param query después de asignarle los parámetros
     * @param clazz clase a la que se quiere transformar el resultado de la consulta
     * @param noResultExceptionCallback función que se va a ejecutar en el caso
     * 		  que query no arroje resultados
     * @return instancia de la clase
     * @author <a href="https://mateolegi.github.io">Mateo Leal</a>
     * @since 2.1.0
     * @throws IllegalArgumentException if query is null
     * @throws NonUniqueResultException si hay más de un resultado
     * @throws IllegalStateException si se llama para una instrucción UPDATE o
     * 		   DELETE del lenguaje de consulta de persistencia de Java
     * @throws QueryTimeoutException si la ejecución de la consulta supera el
     * 		   valor del tiempo de espera de la consulta y solo se revierte la
     * 		   instrucción
     * @throws TransactionRequiredException si se ha establecido un modo de
     * 		   bloqueo distinto de {@code NONE} y no hay transacción o el
     * 		   contexto de persistencia no se ha unido a la transacción
     * @throws PessimisticLockException si el bloqueo pesimista falla y
     * 		   la transacción se revierte
     * @throws LockTimeoutException si el bloqueo pesimista falla y solo
     * 		   se revertirá la instrucción
     * @throws PersistenceException si la ejecución de la consulta supera el
     * 		   valor de tiempo de espera de la consulta y la transacción se retrotrae
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
     * Ejecuta una consulta y mapea el resultado en un DTO.
     * Retorna un único resultado, en caso que la consulta devuelva más
     * de un registro o ningún registro se lanzan las excepciones de {@code Query} <br>
     * Creado el 24/01/2019 a las  11:42:00 a. m. <br>
     * @param query después de asignarle los parámetros
     * @param clazz clase a la que se quiere transformar el resultado de la consulta
     * @param noResultExceptionCallback función que se va a ejecutar en el caso
     * 		  que query no arroje resultados
     * @param nonUniqueResultExceptionCallback función que se va a ejectuar en
     * 		  el caso que el query arroje más de un resultado
     * @return instancia de la clase
     * @author <a href="https://mateolegi.github.io">Mateo Leal</a>
     * @since 2.1.0
     * @throws IllegalArgumentException if query is null
     * @throws IllegalStateException si se llama para una instrucción UPDATE o
     * 		   DELETE del lenguaje de consulta de persistencia de Java
     * @throws QueryTimeoutException si la ejecución de la consulta supera el
     * 		   valor del tiempo de espera de la consulta y solo se revierte la
     * 		   instrucción
     * @throws TransactionRequiredException si se ha establecido un modo de
     * 		   bloqueo distinto de {@code NONE} y no hay transacción o el
     * 		   contexto de persistencia no se ha unido a la transacción
     * @throws PessimisticLockException si el bloqueo pesimista falla y
     * 		   la transacción se revierte
     * @throws LockTimeoutException si el bloqueo pesimista falla y solo
     * 		   se revertirá la instrucción
     * @throws PersistenceException si la ejecución de la consulta supera el
     * 		   valor de tiempo de espera de la consulta y la transacción se retrotrae
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
     * Ejecuta una consulta y mapea el resultado en un DTO. <br>
     * Creado el 12/12/2018 a las  3:30:25 p. m. <br>
     * @param query después de asignarle los parámetros
     * @param clazz clase a la que se quiere transformar el resultado de la consulta
     * @return instancia de la clase
     * @author <a href="https://mateolegi.github.io">Mateo Leal</a>
     * @since 2.0.0
     * @throws IllegalArgumentException if query is null
     * @throws IllegalStateException si se llama para una instrucción UPDATE o
     * 		   DELETE del lenguaje de consulta de persistencia de Java
     * @throws QueryTimeoutException si la ejecución de la consulta supera el
     * 		   valor del tiempo de espera de la consulta y solo se revierte la instrucción
     * @throws TransactionRequiredException si se ha establecido un modo de
     * 		   bloqueo distinto de {@code NONE} y no hay transacción o el
     * 		   contexto de persistencia no se ha unido a la transacción
     * @throws PessimisticLockException si el bloqueo pesimista falla y
     * 		   la transacción se revierte
     * @throws LockTimeoutException si el bloqueo pesimista falla y solo
     * 		   se revertirá la instrucción
     * @throws PersistenceException si la ejecución de la consulta supera el
     * 		   valor de tiempo de espera de la consulta y la transacción se retrotrae
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
     * Transforma un mapa retorando por el Query en una instancia de
     * la clase dada por parámetro <br>
     * Creado el 12/12/2018 a las  3:53:57 p. m. <br>
     * @param map mapa con el resultado de la consulta
     * @param clazz clase a la que se va a mapear
     * @return instancia con los valores del mapa
     * @throws IllegalArgumentException if map or class is null
     * @author <a href="https://mateolegi.github.io">Mateo Leal</a>
     * @since 2.0.0
     */
    @Nullable
    private static <T> T mapToDTO(@NotNull("Map can't be null") Map<DatabaseField, ?> map,
                                  @NotNull("Class can't be null") final Class<T> clazz) {
        try {
            T instance = clazz.newInstance();
            List<Field> fields = Arrays.asList(clazz.getDeclaredFields());
            map.entrySet().stream()
                    .filter(field -> obtenerCampo(field.getKey().getName(), fields) != null)
                    .forEach(i -> {
                        Field field = obtenerCampo(i.getKey().getName(), fields);
                        setValue(field, i.getValue(), instance);
                    });
            return instance;
        } catch (InstantiationException | IllegalAccessException e) {
            LOGGER.log(Level.SEVERE, "Error creando una instancia de " + clazz.getName(), e);
        }
        return null;
    }

    /**
     * Asigna el valor al atributo de la instancia <br>
     * Creado el 12/12/2018 a las  3:56:49 p. m. <br>
     * @param field
     * @param o
     * @param instance
     * @throws IllegalArgumentException if field is null
     * @author <a href="https://mateolegi.github.io">Mateo Leal</a>
     * @since 2.0.0
     */
    private static <T> void setValue(@NotNull("Field can't be null") Field field, Object o, T instance) {
        try {
            boolean isAccessible = field.isAccessible();
            field.setAccessible(true);
            field.set(instance, castParam(o, field.getType()));
            field.setAccessible(isAccessible);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            LOGGER.log(Level.WARNING, "Error seteando valor " + o + " en el campo " + field.getName(), e);
        }
    }

    /**
     * Busca coincidencias entre el nombre de la columna y el nombre del atributo.
     * Independiente de si está en {@code camelCase} o en {@code SNAKE_CASE} <br>
     * Creado el 13/12/2018 a las  9:05:15 a. m. <br>
     * @param databaseField nombre de la columna
     * @param fields lista con los campos de la clase
     * @return el campo de la clase que coincide, de lo contrario {@code null}
     * @throws IllegalArgumentException if field lists is null
     * @author <a href="https://mateolegi.github.io">Mateo Leal</a>
     * @since 2.0.0
     */
    private static Field obtenerCampo(String databaseField, @NotNull("Field list can't be null") List<Field> fields) {
        return fields.stream().filter(field -> field.getName().equalsIgnoreCase(calcularNombreCampo(databaseField)))
                .findFirst()
                .orElse(null);
    }

    /**
     * Valida si el nombre de la columna está en {@code SNAKE_CASE} y la transforma a
     * {@code camelCase} y valida si tienen el mismo valor <br>
     * Creado el 13/12/2018 a las  9:07:44 a. m. <br>
     * @param queryName nombre de la columna de la consulta
     * @return nombre nombre de la columna transformado
     * @throws IllegalArgumentException if queryName is null
     * @since 2.0.0
     */
    private static String calcularNombreCampo(@NotNull("QueryName is null") final String queryName) {
        if (queryName.contains("_")) {
            return CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, queryName);
        }
        return queryName;
    }

    /**
     * Contruye el POJO a partir de los datos del objeto de la consulta nativa. <br>
     * Creado el 1/12/2018 a las  12:17:52 p. m. <br>
     * @param o objeto retornado por la consulta nativa
     * @param clazz clase del POJO
     * @return POJO con la información del objeto
     * @author <a href="https://mateolegi.github.io">Mateo Leal</a>
     */
    private static <T> T objectToDTO(Object o, Class<T> clazz) {
        Object[] oA;
        try {
            oA = (Object[]) o;
        } catch (ClassCastException e) {
            return notAList(o, clazz);
        }
        int length = oA.length;
        // Obtiene el constructor adecuado para construir el POJO
        Constructor<T> c = getConstructor(clazz, length);
        if (c != null) {
            try {
                // Crea una instancia del POJO con el constructor encontrado
                return c.newInstance(castParams(oA, c.getParameterTypes()));
            } catch (Exception e) {
                // Si ocurre algún error durante la inicialización
                LOGGER.log(Level.SEVERE, "Ocurrió un error instanciando el objeto.", e);
                throw new ClassCastException(e.getMessage());
            }
        } else {
            throw new IllegalArgumentException("No se encontró un constructor para la cantidad de elementos.");
        }
    }

    /**
     * Si es un objeto simple lo moldea y lo retorna <br>
     * Creado el 12/12/2018 a las  3:58:06 p. m. <br>
     * @param o
     * @param clazz
     * @return
     * @author <a href="https://mateolegi.github.io">Mateo Leal</a>
     */
    @SuppressWarnings("unchecked")
    private static <T> T notAList(Object o, Class<T> clazz) {
        return (T) castParam(o, clazz);
    }

    /**
     * Obitnene el primer constructor con el número de parámetros igual al número
     * de atributos del objeto de la consulta<br>
     * Creado el 1/12/2018 a las  12:11:35 p. m. <br>
     * @param clazz clase del POJO
     * @param parameterCount número de atributos del objeto de la consulta nativa
     * @return the constructor
     * @throws IllegalArgumentException if class is null
     * @author <a href="https://mateolegi.github.io">Mateo Leal</a>
     */
    @SuppressWarnings("unchecked")
    private static <T> Constructor<T> getConstructor(@NotNull("Class can't be null") Class<T> clazz,
                                                     int parameterCount) {
        Constructor<T>[] constructors = (Constructor<T>[]) clazz.getConstructors();
        return Arrays.asList(constructors).stream()
                .filter(i -> i.getParameterCount() == parameterCount)
                .findFirst()
                .orElse(null);
    }

    /**
     * Moldea cada uno de los valores a su respetiva clase dentro del POJO <br>
     * Creado el 1/12/2018 a las  12:20:40 p. m. <br>
     * @param p arreglo con los atributos del objeto de la consulta nativa
     * @param classes arreglo con las clases respectivas para cada atributo
     * @return arreglo de objetos con los atributos moldeados
     * @throws IllegalArgumentException if attributes array is null
     * @author <a href="https://mateolegi.github.io">Mateo Leal</a>
     */
    private static Object[] castParams(@NotNull("Attributes array can't be null") Object[] p, Class<?>[] classes) {
        Object[] castedParams = new Object[p.length];
        for (int i = 0; i < p.length; i++) {
            castedParams[i] = castParam(p[i], classes[i]);
        }
        return castedParams;
    }

    /**
     * Moldea un objeto a la clase dada <br>
     * Creado el 13/12/2018 a las  9:10:05 a. m. <br>
     * @param o objeto
     * @param c clase a moldear
     * @return instancia de la clase con el valor del objeto
     * @author <a href="https://mateolegi.github.io">Mateo Leal</a>
     * @since 2.0.0
     */
    @Nullable
    private static Object castParam(Object o, Class<?> c) {
        try {
            if (o == null) {
                return null;
            } else if (c == Number.class || c.getSuperclass() == Number.class) {
                // Casos especiales para moldear los números
                return castNumber(o, c);
            } else if (c == Date.class && o.getClass() == Timestamp.class) {
                Timestamp timestamp = (Timestamp) o;
                return new Date(timestamp.getTime());
            } else if (c == String.class && !(o instanceof String)) {
                return o.toString();
            } else {
                return c.cast(o);
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Ocurrió un error moldeando el objeto con valor "
                    + o + " a la clase " + c.getName(), e);
            throw e;
        }
    }

    /**
     * Moldea los números a la clase enviada por parámetro <br>
     * Creado el 1/12/2018 a las  12:23:54 p. m. <br>
     * @param o objeto con el número
     * @param clazz clase a la que debe ser moldeada
     * @return número moldeado
     * @author <a href="https://mateolegi.github.io">Mateo Leal</a>
     */
    private static Number castNumber(Object o, Class<?> clazz) {
        return numberCast.get(clazz).cast(o);
    }
}
