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
 * @param <T> entity
 * @author <a href="mateolegi.github.io">Mateo Leal</a>
 * @version 1.0.0
 */
public class Rostrum<T extends Rostrum> {

    /** Object that interacts with the persistence context */
    private static EntityManager manager = Factory.getEntityManager();

    /**
     * Gets all records of the entity in the persistence context.
     * @return all records
     */
    public List<T> findAll() {
        return (List<T>) manager.createQuery("SELECT e FROM " + this.getClass().getSimpleName() + " e",
                this.getClass())
                .getResultList();
    }

    public T find(Object id) {
        requireEntityManager();
        return (T) manager.find(this.getClass(), id);
    }

    public T save() {
        requireEntityManager();
        if (!exists()) {
            setCreatedAt();
            compareEncryptedFields(null);
            manager.persist(this);
            return (T) this;
        } else {
            return update();
        }
    }

    public T update() {
        requireEntityManager();
        if (exists()) {
            setUpdatedAt();
            compareEncryptedFields(find(getId()));
            return (T) manager.merge(this);
        } else {
            return save();
        }
    }

    public T updateIfExists() {
        requireEntity();
        return update();
    }

    public void delete() {
        requireEntity();
        manager.remove(this);
    }

    public boolean exists() {
        requireEntityManager();
        Object id = getId();
        if (Objects.isNull(id)) {
            return false;
        }
        return Objects.nonNull(manager.find(this.getClass(), id));
    }

    private Object getId() {
        Field idField = getIdField();
        Method idMethod = getIdMethod();
        try {
            if (Objects.isNull(idField)) {
                if (Objects.isNull(idMethod)) {
                    throw new NoIdFieldFoundException(
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
            throw new NoIdFieldFoundException(message, e);
        } catch (InvocationTargetException e) {
            throw new NoIdFieldFoundException(String.format("There was an error invoking the method %s.",
                    idMethod.getName()), e);
        }
    }

    private Field getIdField() {
        Class clazz = this.getClass();
        List<Field> idFields = Arrays.stream(clazz.getDeclaredFields())
                .filter(this::hasIdAnnotation)
                .collect(Collectors.toList());
        if (idFields.isEmpty() || idFields.size() > 1) {
            return null;
        }
        return idFields.get(0);
    }

    private Method getIdMethod() {
        Class clazz = this.getClass();
        List<Method> idMethods = Arrays.stream(clazz.getDeclaredMethods())
                .filter(this::hasIdAnnotation)
                .collect(Collectors.toList());
        if (idMethods.isEmpty() || idMethods.size() > 1) {
            return null;
        }
        return idMethods.get(0);
    }

    private void setCreatedAt() {
        setDateField(getCreatedAtField());
    }

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

    private void setUpdatedAt() {
        setDateField(getUpdatedAtField());
    }

    private Field getCreatedAtField() {
        return getField("createdAt");
    }

    private Field getUpdatedAtField() {
        return getField("updatedAt");
    }

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

    private <T extends Rostrum> void compareEncryptedFields(T persisted) {
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
                e.printStackTrace();
            }
        });
    }

    private void encryptField(Field field) {
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

    private List<Field> getCryptFields() {
        return Arrays.stream(this.getClass().getDeclaredFields())
                .filter(this::hasCryptAnnotation)
                .collect(Collectors.toList());
    }

    private boolean hasCryptAnnotation(Field f) {
        try {
            return Objects.nonNull(f.getDeclaredAnnotation(Crypt.class));
        } catch (NullPointerException e) {
            return false;
        }
    }

    private boolean hasIdAnnotation(Field f) {
        try {
            return Objects.nonNull(f.getDeclaredAnnotation(Id.class));
        } catch (NullPointerException e) {
            return false;
        }
    }

    private boolean hasIdAnnotation(Method m) {
        try {
            return Objects.nonNull(m.getDeclaredAnnotation(Id.class));
        } catch (NullPointerException e) {
            return false;
        }
    }

    private void requireEntity() {
        if (!exists()) {
            throw new NotExistsException("Entity doesn't exists in persistence context.");
        }
    }

    private void requireEntityManager() {
        if (Objects.isNull(manager)) {
            throw new EntityManagerNotInitializedException(
                    "EntityManager hasn't been initialized. Check properties file.");
        }
    }
}
