package com.mateolegi.rostrum;

import com.mateolegi.rostrum.annotation.Crypt;
import com.mateolegi.rostrum.annotation.Type;
import com.mateolegi.rostrum.exception.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

    private static boolean isTransactionPreviouslyActive = false;

    /**
     * Gets all records of the entity in the persistence context.
     * @param <T> entity type
     * @param clazz entity class
     * @return all records
     * @throws IllegalArgumentException if the query string is found to be invalid or if the query result is found to
     *         not be assignable to the specified type.
     */
    public static <T> List<T> findAll(Class<T> clazz) {
        return getManager().createQuery("SELECT e FROM " + clazz.getSimpleName() + " e", clazz)
                .getResultList();
    }

    /**
     * Get an entity from persistence context.
     * @param clazz entity class
     * @param id primary key
     * @param <T> entity type
     * @return entity or {@code null} if not exists
     * @throws EntityManagerNotInitializedException if entity manager can not be initialized.
     * @throws IllegalArgumentException if  id is null or the first argument does not denote an entity type or the
     *         second argument is not a valid type for that entity's primary key.
     */
    public static <T> T find(Class<T> clazz, @NotNull("Primary key can not be null.") Object id) {
        requireEntityManager();
        return getManager().find(clazz, id);
    }

    /**
     * Persists an entity
     * @param entity entity
     * @param <T> entity type
     * @return persisted entity
     * @throws IllegalArgumentException if entity is null.
     * @throws EntityManagerNotInitializedException if entity manager can not be initialized.
     */
    public static <T> T save(@NotNull("Entity can't be null") T entity) {
        requireEntityManager();
        try {
            // if the entity does not exist, it is stored in persistence context, otherwise it is updated.
            if (!exists(entity)) {
                beginTransaction();
                setCreatedAt(entity);
                compareEncryptedFields(entity, null);
                getManager().persist(entity);
                commitTransaction();
                return entity;
            } else {
                return update(entity);
            }
        } catch (RuntimeException e) {
            rollbackTransaction();
            throw e;
        }
    }

    /**
     * Update an entity in persistence context.
     * @param entity entity
     * @param <T> entity type
     * @return updated entity
     * @throws IllegalArgumentException entity can't be null.
     * @throws EntityManagerNotInitializedException if entity manager can not be initialized.
     */
    public static <T> T update(@NotNull("Entity can't be null") T entity) {
        requireEntityManager();
        try {
            if (exists(entity)) {
                beginTransaction();
                setUpdatedAt(entity);
                compareEncryptedFields(entity, find(entity.getClass(), getId(entity)));
                T updatedEntity = getManager().merge(entity);
                commitTransaction();
                return updatedEntity;
            } else {
                return save(entity);
            }
        } catch (RuntimeException e) {
            rollbackTransaction();
            throw e;
        }
    }

    /**
     * Update an entity only if it already exists in persistence context.
     * @param entity entity
     * @param <T> entity type
     * @return updated entity
     * @throws NotExistsException if entity doesn't exists in persistence context.
     */
    public static <T> T updateIfExists(T entity) {
        requireEntity(entity);
        return update(entity);
    }

    /**
     * Remove an entity in persistence context.
     * @param entity entity
     * @param <T> entity type
     * @throws NotExistsException if entity doesn't exists in persistence context.
     */
    public static <T> void delete(T entity) {
        requireEntity(entity);
        try {
            beginTransaction();
            getManager().remove(entity);
            commitTransaction();
        } catch (RuntimeException e) {
            rollbackTransaction();
            throw e;
        }
    }

    /**
     * Valid that the entity already exists in the persistence context.
     * @param entity entity
     * @param <T> entity type
     * @return validation result
     * @throws IllegalArgumentException if entity is null.
     * @throws EntityManagerNotInitializedException if entity manager couldn't be initialized.
     */
    public static <T> boolean exists(@NotNull("Entity can't be null") T entity) {
        requireEntityManager();
        Object id = getId(entity);
        if (Objects.isNull(id)) {
            return false;
        }
        return Objects.nonNull(getManager().find(entity.getClass(), id));
    }

    /**
     * Find the field or method that contains the entity Id and get the value.
     * @param entity entity
     * @param <T> entity type
     * @return entity id or null if it's empty
     * @throws IllegalArgumentException if entity is null.
     * @throws NoIdFoundException if there's no field or method with the {@code Id} annotation. If the value of Id
     *         field couldn't be accessed or an error occurred when calling method that gets the Id.
     */
    private static <T> Object getId(@NotNull("Entity can't be null") T entity) {
        Field idField = getIdField(entity.getClass());
        Method idMethod = getIdMethod(entity.getClass());
        try {
            if (Objects.isNull(idField)) {
                if (Objects.isNull(idMethod)) {
                    throw new NoIdFoundException(
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
            throw new NoIdFoundException(message, e);
        } catch (InvocationTargetException e) {
            throw new NoIdFoundException(String.format("There was an error invoking the method %s.",
                    idMethod.getName()), e);
        }
    }

    /**
     * Get field with {@code Id} annotation.
     * @param clazz entity class
     * @param <T> entity type
     * @return {@code Id} annotated field
     * @throws IllegalArgumentException if class is empty.
     * @throws MultipleIdException if there's multiple fields with {@code Id} annotation.
     */
    @Nullable
    private static <T> Field getIdField(@NotNull("Class can't be null") Class<T> clazz) {
        List<Field> idFields = Arrays.stream(clazz.getDeclaredFields())
                .filter(Rostrum::hasIdAnnotation)
                .collect(Collectors.toList());
        if (idFields.isEmpty()) {
            return null;
        } else if (idFields.size() > 1) {
            throw new MultipleIdException("There is more than one field with the Id annotation.");
        }
        return idFields.get(0);
    }

    /**
     * Get method with {@code Id} annotation.
     * @param clazz entity class
     * @param <T> entity type
     * @return {@code Id} annotated method
     * @throws IllegalArgumentException if class is null.
     * @throws MultipleIdException if there's multiple methods with {@code Id} annotation.
     */
    @Nullable
    private static <T> Method getIdMethod(@NotNull("Class can't be null") Class<T> clazz) {
        List<Method> idMethods = Arrays.stream(clazz.getDeclaredMethods())
                .filter(Rostrum::hasIdAnnotation)
                .collect(Collectors.toList());
        if (idMethods.isEmpty()) {
            return null;
        } else if (idMethods.size() > 1) {
            throw new MultipleIdException("There is more than one method with the Id annotation.");
        }
        return idMethods.get(0);
    }

    /**
     * Set actual date in createdAt field.
     * @param entity entity
     * @param <T> entity type
     */
    private static <T> void setCreatedAt(T entity) {
        setDateField(entity, getCreatedAtField(entity));
    }

    /**
     * Sets actual date in the field specified.
     * @param entity entity
     * @param dateField field
     * @param <T> entity type
     */
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

    /**
     * Set the actual date in updatedAt field.
     * @param entity entity
     * @param <T> entity type
     */
    private static <T> void setUpdatedAt(T entity) {
        setDateField(entity, getUpdatedAtField(entity));
    }

    /**
     * Gets the entity create date field.
     * @param entity entity
     * @param <T> entity type
     * @return createdAt field
     */
    private static <T> Field getCreatedAtField(T entity) {
        return getField(entity, "createdAt");
    }

    /**
     * Gets the entity update date field.
     * @param entity entity
     * @param <T> entity type
     * @return updatedAt field
     */
    private static <T> Field getUpdatedAtField(T entity) {
        return getField(entity, "updatedAt");
    }

    /**
     * Get an entity field by its name.
     * @param entity entity
     * @param name field name
     * @param <T> entity type
     * @return field
     * @throws IllegalArgumentException if entity is null.
     */
    @Nullable
    private static <T> Field getField(@NotNull("Entity can't be null") T entity, String name) {
        try {
            Class clazz = entity.getClass();
            Field field = clazz.getDeclaredField(name);
            field.setAccessible(true);
            return field;
        } catch (NoSuchFieldException e) {
            return null;
        }
    }

    /**
     * Compare the encrypted fields and validate that no changes have been made to them, otherwise proceed to encrypt
     * them.
     * @param entity entity
     * @param persisted entity founded previously in the persistence context
     * @param <T> entity type
     * @throws IllegalArgumentException if entity is null.
     */
    private static <T> void compareEncryptedFields(@NotNull("Entity can't be null") T entity, T persisted) {
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
                // ignored because it can not go through here.
            }
        });
    }

    /**
     * It encrypts the field in the entity before it is persisted. If the encryption type is one-way proceeds to use
     * the BCrypt method, otherwise AES256 two-way method.
     * @param entity entity
     * @param field field with Crypt annotation
     * @param <T> entity type
     * @throws IllegalArgumentException if field is null.
     */
    private static <T> void encryptField(T entity, @NotNull("Field can't be null") Field field) {
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

    /**
     * Get all the fields with Crypt annotation.
     * @param clazz entity class
     * @param <T> entity type
     * @return fields with Crypt annotation
     * @throws IllegalArgumentException if class is null.
     */
    private static <T> List<Field> getCryptFields(@NotNull("Class can't be null") Class<T> clazz) {
        return Arrays.stream(clazz.getDeclaredFields())
                .filter(Rostrum::hasCryptAnnotation)
                .collect(Collectors.toList());
    }

    /**
     * Validates if field has {@code Crypt} annotation.
     * @param f field
     * @return validation result
     */
    private static boolean hasCryptAnnotation(Field f) {
        try {
            return Objects.nonNull(f.getDeclaredAnnotation(Crypt.class));
        } catch (NullPointerException e) {
            return false;
        }
    }

    /**
     * Validates if field has {@code Id} annotation.
     * @param f field
     * @return validation result
     */
    private static boolean hasIdAnnotation(Field f) {
        try {
            return Objects.nonNull(f.getDeclaredAnnotation(Id.class));
        } catch (NullPointerException e) {
            return false;
        }
    }

    /**
     * Validates if method has {@code Id} annotation.
     * @param m method
     * @return validation result
     */
    private static boolean hasIdAnnotation(Method m) {
        try {
            return Objects.nonNull(m.getDeclaredAnnotation(Id.class));
        } catch (NullPointerException e) {
            return false;
        }
    }

    /**
     * Gets or create an entity manager to interact with persistence context.
     * @return entity manager
     */
    private static EntityManager getManager() {
        return Factory.getEntityManager();
    }

    /** Start a transaction if it is not active yet. */
    private static void beginTransaction() {
        if (!isTransactionPreviouslyActive()) {
            getManager().getTransaction().begin();
        }
    }

    /** Commit current transaction. */
    private static void commitTransaction() {
        if (!isTransactionPreviouslyActive) {
            getManager().getTransaction().commit();
        }
    }

    /** Roll back current transaction. */
    private static void rollbackTransaction() {
        if (!isTransactionPreviouslyActive) {
            getManager().getTransaction().rollback();
        }
    }

    /**
     * Validates if a transaction was previously active before calling action.
     * @return result
     */
    private static boolean isTransactionPreviouslyActive() {
        if (getManager().getTransaction().isActive()) {
            isTransactionPreviouslyActive = true;
        }
        return isTransactionPreviouslyActive;
    }

    /**
     * Validates entity exists in persistence context.
     * @param entity entity
     * @param <T> entity type
     * @throws NotExistsException if entity doesn't exists in persistence context.
     */
    private static <T> void requireEntity(T entity) {
        if (!exists(entity)) {
            throw new NotExistsException("Entity doesn't exists in persistence context.");
        }
    }

    /**
     * Validates entityManager is active.
     * @throws EntityManagerNotInitializedException if entity manager can not be initialized.
     */
    private static void requireEntityManager() {
        if (Objects.isNull(getManager())) {
            throw new EntityManagerNotInitializedException(
                    "EntityManager hasn't been initialized. Check properties file.");
        }
    }
}
