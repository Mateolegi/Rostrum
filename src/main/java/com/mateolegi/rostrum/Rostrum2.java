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

public class Rostrum2 {

    /**
     * Gets all records of the entity in the persistence context.
     * @return all records
     * @throws IllegalArgumentException if the query string is found to be invalid or if the query result is found to
     *         not be assignable to the specified type.
     */
    public List findAll() {
        requireEntityManager();
        Class<? extends Rostrum2> clazz = this.getClass();
        return getManager().createQuery(String.format("SELECT e FROM %s e", clazz.getSimpleName()), clazz)
                .getResultList();
    }

    /**
     * Get an entity from persistence context.
     * @param id primary key
     * @return entity or {@code null} if not exists
     * @throws EntityManagerNotInitializedException if entity manager can not be initialized.
     * @throws IllegalArgumentException if  id is null or the first argument does not denote an entity type or the
     *         second argument is not a valid type for that entity's primary key.
     */
    public Object find(@NotNull("Primary key can not be null.") Object id) {
        requireEntityManager();
        Class<? extends Rostrum2> clazz = this.getClass();
        return getManager().find(clazz, id);
    }

    /**
     * Persists an entity
     * @return persisted entity
     * @throws IllegalArgumentException if entity is null.
     * @throws EntityManagerNotInitializedException if entity manager can not be initialized.
     */
    public Rostrum2 save() {
        requireEntityManager();
        try {
            // if the entity does not exist, it is stored in persistence context, otherwise it is updated.
            if (!exists()) {
                Factory.beginTransaction();
                setCreatedAt();
                compareEncryptedFields(null);
                getManager().persist(this);
                Factory.commitTransaction();
                return this;
            } else {
                return update();
            }
        } catch (RuntimeException e) {
            Factory.rollbackTransaction();
            throw e;
        }
    }

    /**
     * Update an entity in persistence context.
     * @return updated entity
     * @throws IllegalArgumentException entity can't be null.
     * @throws EntityManagerNotInitializedException if entity manager can not be initialized.
     */
    public Rostrum2 update() {
        requireEntityManager();
        try {
            if (exists()) {
                Factory.beginTransaction();
                setUpdatedAt();
                compareEncryptedFields(find(getId()));
                Rostrum2 updatedEntity = getManager().merge(this);
                Factory.commitTransaction();
                return updatedEntity;
            } else {
                return save();
            }
        } catch (RuntimeException e) {
            Factory.rollbackTransaction();
            throw e;
        }
    }

    /**
     * Update an entity only if it already exists in persistence context.
     * @return updated entity
     * @throws NotExistsException if entity doesn't exists in persistence context.
     */
    public Rostrum2 updateIfExists() {
        requireEntity();
        return update();
    }

    /**
     * Remove an entity in persistence context.
     * @throws NotExistsException if entity doesn't exists in persistence context.
     */
    public void delete() {
        requireEntity();
        try {
            Factory.beginTransaction();
            getManager().remove(this);
            Factory.commitTransaction();
        } catch (RuntimeException e) {
            Factory.rollbackTransaction();
            throw e;
        }
    }

    /**
     * Valid that the entity already exists in the persistence context.
     * @return validation result
     * @throws IllegalArgumentException if entity is null.
     * @throws EntityManagerNotInitializedException if entity manager couldn't be initialized.
     */
    public boolean exists() {
        requireEntityManager();
        Object id = getId();
        if (Objects.isNull(id)) {
            return false;
        }
        return Objects.nonNull(getManager().find(this.getClass(), id));
    }

    /**
     * Find the field or method that contains the entity Id and get the value.
     * @return entity id or null if it's empty
     * @throws IllegalArgumentException if entity is null.
     * @throws NoIdFoundException if there's no field or method with the {@code Id} annotation. If the value of Id
     *         field couldn't be accessed or an error occurred when calling method that gets the Id.
     */
    private Object getId() {
        Field idField = getIdField();
        Method idMethod = getIdMethod();
        try {
            if (Objects.isNull(idField)) {
                if (Objects.isNull(idMethod)) {
                    throw new NoIdFoundException(
                            "No field or method was found with the javax.persistence.Id annotation.");
                }
                idMethod.setAccessible(true);
                return idMethod.invoke(this);
            } else {
                idField.setAccessible(true);
                return idField.get(this);
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
     * @return {@code Id} annotated field
     * @throws IllegalArgumentException if class is empty.
     * @throws MultipleIdException if there's multiple fields with {@code Id} annotation.
     */
    @Nullable
    private Field getIdField() {
        Class clazz = this.getClass();
        List<Field> idFields = Arrays.stream(clazz.getDeclaredFields())
                .filter(this::hasIdAnnotation)
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
     * @return {@code Id} annotated method
     * @throws IllegalArgumentException if class is null.
     * @throws MultipleIdException if there's multiple methods with {@code Id} annotation.
     */
    @Nullable
    private Method getIdMethod() {
        Class clazz = this.getClass();
        List<Method> idMethods = Arrays.stream(clazz.getDeclaredMethods())
                .filter(this::hasIdAnnotation)
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
     */
    private void setCreatedAt() {
        setDateField(getCreatedAtField());
    }

    /**
     * Sets actual date in the field specified.
     * @param dateField field
     */
    private void setDateField(Field dateField) {
        try {
            if (Objects.nonNull(dateField)) {
                Date actualDate = new Date();
                Class clazz = dateField.getType();
                if (clazz == Date.class) {
                    dateField.set(this, actualDate);
                } else if (clazz == Timestamp.class) {
                    dateField.set(this, new Timestamp(actualDate.getTime()));
                } else if (clazz == java.sql.Date.class) {
                    dateField.set(this, new java.sql.Date(actualDate.getTime()));
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
     */
    private void setUpdatedAt() {
        setDateField(getUpdatedAtField());
    }

    /**
     * Gets the entity create date field.
     * @return createdAt field
     */
    private Field getCreatedAtField() {
        return getField("createdAt");
    }

    /**
     * Gets the entity update date field.
     * @return updatedAt field
     */
    private Field getUpdatedAtField() {
        return getField("updatedAt");
    }

    /**
     * Get an entity field by its name.
     * @param name field name
     * @return field
     * @throws IllegalArgumentException if entity is null.
     */
    @Nullable
    private Field getField(String name) {
        try {
            Class clazz = this.getClass();
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
     * @param persisted entity founded previously in the persistence context
     * @throws IllegalArgumentException if entity is null.
     */
    private void compareEncryptedFields(Object persisted) {
        List<Field> fields = getCryptFields();
        fields.forEach(field -> {
            try {
                field.setAccessible(true);
                String oActual = (String) field.get(this);
                if (Objects.nonNull(persisted)) {
                    String oPersisted = (String) field.get(persisted);
                    if (!oActual.equals(oPersisted)) {
                        encryptField(field);
                    }
                } else {
                    encryptField(field);
                }
            } catch (IllegalAccessException e) {
                // ignored because it can not go through here.
            }
        });
    }

    /**
     * It encrypts the field in the entity before it is persisted. If the encryption type is one-way proceeds to use
     * the BCrypt method, otherwise AES256 two-way method.
     * @param field field with Crypt annotation
     * @throws IllegalArgumentException if field is null.
     */
    private void encryptField(@NotNull("Field can't be null") Field field) {
        try {
            Crypt crypt = field.getDeclaredAnnotation(Crypt.class);
            Method method = Crypt.class.getMethod("type");
            Type type = (Type) method.invoke(crypt);
            String original = (String) field.get(this);
            if (type == Type.ONE_WAY) {
                String encrypted = BCrypt.hash(original);
                field.set(this, encrypted);
            } else {
                String encrypted = AES256.encrypt(original);
                field.set(this, encrypted);
            }
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new EncryptionException("There was an error encrypting text.", e);
        }
    }

    /**
     * Get all the fields with Crypt annotation.
     * @return fields with Crypt annotation
     * @throws IllegalArgumentException if class is null.
     */
    private List<Field> getCryptFields() {
        Class clazz = this.getClass();
        return Arrays.stream(clazz.getDeclaredFields())
                .filter(this::hasCryptAnnotation)
                .collect(Collectors.toList());
    }

    /**
     * Validates if field has {@code Crypt} annotation.
     * @param f field
     * @return validation result
     */
    private boolean hasCryptAnnotation(Field f) {
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
    private boolean hasIdAnnotation(Field f) {
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
    private boolean hasIdAnnotation(Method m) {
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
    private EntityManager getManager() {
        return Factory.getEntityManager();
    }

    /**
     * Validates entity exists in persistence context.
     * @throws NotExistsException if entity doesn't exists in persistence context.
     */
    private void requireEntity() {
        if (!exists()) {
            throw new NotExistsException("Entity doesn't exists in persistence context.");
        }
    }

    /**
     * Validates entityManager is active.
     * @throws EntityManagerNotInitializedException if entity manager can not be initialized.
     */
    private void requireEntityManager() {
        if (Objects.isNull(getManager())) {
            throw new EntityManagerNotInitializedException(
                    "EntityManager hasn't been initialized. Check properties file.");
        }
    }
}
