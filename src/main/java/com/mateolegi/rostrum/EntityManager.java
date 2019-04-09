package com.mateolegi.rostrum;

import org.eclipse.persistence.internal.jpa.EntityManagerFactoryDelegate;
import org.eclipse.persistence.internal.jpa.EntityManagerImpl;
import org.eclipse.persistence.internal.sessions.AbstractSession;

import javax.persistence.SynchronizationType;
import javax.persistence.TypedQuery;
import java.util.Map;

public class EntityManager extends EntityManagerImpl {

    public EntityManager(String sessionName) {
        super(sessionName);
    }

    public EntityManager(AbstractSession databaseSession, SynchronizationType syncType) {
        super(databaseSession, syncType);
    }

    public EntityManager(AbstractSession databaseSession, Map properties, SynchronizationType syncType) {
        super(databaseSession, properties, syncType);
    }

    public EntityManager(EntityManagerFactoryDelegate factory, Map properties, SynchronizationType syncType) {
        super(factory, properties, syncType);
    }

    public TypedQuery createFilteredQuery(Class<?> clazz, Map<String, Object> queryParams) {
        return FilteredQuery.createFilteredQuery(clazz, this, queryParams);
    }
}
