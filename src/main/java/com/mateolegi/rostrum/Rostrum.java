package com.mateolegi.rostrum;

import com.mateolegi.rostrum.annotation.Crypt;
import com.mateolegi.rostrum.annotation.Type;
import com.mateolegi.rostrum.exception.*;

import javax.persistence.EntityManager;
import javax.persistence.Id;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Gives the extended entities the functionalities of the ORM.
 * @author <a href="mateolegi.github.io">Mateo Leal</a>
 * @version 1.0.0
 */
public class Rostrum {

    /**
     * Gets all records of the entity in the persistence context.
     * @return all records
     */
    public static <T> List<T> findAll(Class<T> clazz) {
        return getManager().createQuery("SELECT e FROM " + clazz.getSimpleName() + " e", clazz)
                .getResultList();
    }

    public static <T> T find(Class<T> clazz, Object id) {
        requireEntityManager();
        return getManager().find(clazz, id);
    }

    public static <T> T save(T entity) {
        requireEntityManager();
        if (!exists(entity)) {
            setCreatedAt(entity);
            compareEncryptedFields(entity, null);
            getManager().persist(entity);
            return entity;
        } else {
            return update(entity);
        }
    }

    public static <T> T update(T entity) {
        requireEntityManager();
        if (exists(entity)) {
            setUpdatedAt(entity);
            compareEncryptedFields(entity, find(entity.getClass(), getId(entity)));
            return getManager().merge(entity);
        } else {
            return save(entity);
        }
    }

    public static <T> T updateIfExists(T entity) {
        requireEntity(entity);
        return update(entity);
    }

    public static <T> void delete(T entity) {
        requireEntity(entity);
        getManager().remove(entity);
    }

    public static <T> boolean exists(T entity) {
        requireEntityManager();
        Object id = getId(entity);
        if (Objects.isNull(id)) {
            return false;
        }
        return Objects.nonNull(getManager().find(entity.getClass(), id));
    }

    private static <T> Object getId(T entity) {
        Field idField = getIdField(entity.getClass());
        Method idMethod = getIdMethod(entity.getClass());
        try {
            if (Objects.isNull(idField)) {
                if (Objects.isNull(idMethod)) {
                    throw new NoIdFieldFoundException(
                            "No field or method was found with the javax.persistence.Id annotation.");
                }
                idMethod.setAccessible(true);
                return idMethod.invoke(entity);
            } else {
                idField.setAccessible(true);
                return idField.get(entity);
            }
        } catch (IllegalAccessException e) {
            String message = "The value of the field could not be accessed.";
            if (Objects.nonNull(idField)) {
                message = String.format("The value of the %s field could not be accessed.",
                        idField.getName());
            }
            throw new NoIdFieldFoundException(message, e);
        } catch (InvocationTargetException e) {
            throw new NoIdFieldFoundException(String.format("There was an error invoking the method %s.",
                    idMethod.getName()), e);
        }
    }

    private static <T> Field getIdField(Class<T> clazz) {
        List<Field> idFields = Arrays.stream(clazz.getDeclaredFields())
                .filter(Rostrum::hasIdAnnotation)
                .collect(Collectors.toList());
        if (idFields.isEmpty() || idFields.size() > 1) {
            return null;
        }
        return idFields.get(0);
    }

    private static <T> Method getIdMethod(Class<T> clazz) {
        List<Method> idMethods = Arrays.stream(clazz.getDeclaredMethods())
                .filter(Rostrum::hasIdAnnotation)
                .collect(Collectors.toList());
        if (idMethods.isEmpty() || idMethods.size() > 1) {
            return null;
        }
        return idMethods.get(0);
    }

    private static <T> void setCreatedAt(T entity) {
        setDateField(entity, getCreatedAtField(entity));
    }

    private static <T> void setDateField(T entity, Field dateField) {
        try {
            if (Objects.nonNull(dateField)) {
                Date actualDate = new Date();
                Class clazz = dateField.getType();
                if (clazz == Date.class) {
                    dateField.set(entity, actualDate);
                } else if (clazz == Timestamp.class) {
                    dateField.set(entity, new Timestamp(actualDate.getTime()));
                } else if (clazz == java.sql.Date.class) {
                    dateField.set(entity, new java.sql.Date(actualDate.getTime()));
                } else {
                    throw new NotSupportedDateClassException(String.format("Can't cast to %s", clazz.getName()));
                }
            }
        } catch (IllegalAccessException e) {
            throw new NotSupportedDateClassException(String.format("The value of the %s field could not be accessed",
                    dateField.getName()), e);
        }
    }

    private static <T> void setUpdatedAt(T entity) {
        setDateField(entity, getUpdatedAtField(entity));
    }

    private static <T> Field getCreatedAtField(T entity) {
        return getField(entity, "createdAt");
    }

    private static <T> Field getUpdatedAtField(T entity) {
        return getField(entity, "updatedAt");
    }

    private static <T> Field getField(T entity, String name) {
        try {
            Class clazz = entity.getClass();
            Field field = clazz.getDeclaredField(name);
            field.setAccessible(true);
            return field;
        } catch (NoSuchFieldException e) {
            return null;
        }
    }

    private static <T> void compareEncryptedFields(T entity, T persisted) {
        List<Field> fields = getCryptFields(entity.getClass());
        fields.forEach(field -> {
            try {
                field.setAccessible(true);
                String oActual = (String) field.get(entity);
                if (Objects.nonNull(persisted)) {
                    String oPersisted = (String) field.get(persisted);
                    if (!oActual.equals(oPersisted)) {
                        encryptField(entity, field);
                    }
                } else {
                    encryptField(entity, field);
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        });
    }

    private static <T> void encryptField(T entity, Field field) {
        try {
            Crypt crypt = field.getDeclaredAnnotation(Crypt.class);
            Method method = Crypt.class.getMethod("type");
            Type type = (Type) method.invoke(crypt);
            String original = (String) field.get(entity);
            if (type == Type.ONE_WAY) {
                String encrypted = BCrypt.hash(original);
                field.set(entity, encrypted);
            } else {
                String encrypted = AES256.encrypt(original);
                field.set(entity, encrypted);
            }
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new EncryptionException("There was an error encrypting text.", e);
        }
    }

    private static <T> List<Field> getCryptFields(Class<T> clazz) {
        return Arrays.stream(clazz.getDeclaredFields())
                .filter(Rostrum::hasCryptAnnotation)
                .collect(Collectors.toList());
    }

    private static boolean hasCryptAnnotation(Field f) {
        try {
            return Objects.nonNull(f.getDeclaredAnnotation(Crypt.class));
        } catch (NullPointerException e) {
            return false;
        }
    }

    private static boolean hasIdAnnotation(Field f) {
        try {
            return Objects.nonNull(f.getDeclaredAnnotation(Id.class));
        } catch (NullPointerException e) {
            return false;
        }
    }

    private static boolean hasIdAnnotation(Method m) {
        try {
            return Objects.nonNull(m.getDeclaredAnnotation(Id.class));
        } catch (NullPointerException e) {
            return false;
        }
    }

    private static EntityManager getManager() {
        return Factory.getEntityManager();
    }

    private static <T> void requireEntity(T entity) {
        if (!exists(entity)) {
            throw new NotExistsException("Entity doesn't exists in persistence context.");
        }
    }

    private static void requireEntityManager() {
        if (Objects.isNull(getManager())) {
            throw new EntityManagerNotInitializedException(
                    "EntityManager hasn't been initialized. Check properties file.");
        }
    }
}
