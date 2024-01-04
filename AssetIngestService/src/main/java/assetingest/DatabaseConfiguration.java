package assetingest;

import assetmetadataregistry.*;
import assetregistry.*;
import assetsubjectsregistry.*;
import org.hibernate.*;
import org.hibernate.Cache;
import org.hibernate.boot.*;
import org.hibernate.boot.registry.*;
import org.hibernate.boot.spi.*;
import org.hibernate.engine.spi.*;
import org.hibernate.metadata.*;
import org.hibernate.stat.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.context.annotation.*;

import javax.naming.*;
import javax.persistence.*;
import javax.persistence.criteria.*;
import java.sql.*;
import java.util.*;

@Configuration
public class DatabaseConfiguration {
    @Bean
    public SessionFactory createSessionFactory(
            @Value("${assetingest.connection.url}") String url,
            @Value("${assetingest.connection.user}") String user,
            @Value("${assetingest.connection.password}") String password) {
        try {
            final var registry = new StandardServiceRegistryBuilder()
                    .applySetting("hibernate.connection.url", url)
                    .applySetting("hibernate.connection.user", user)
                    .applySetting("hibernate.connection.password", password)
                    .applySetting("hibernate.hbm2ddl.auto", "validate")
                    .applySetting("hibernate.connection.autocommit", true)
                    .applySetting("hibernate.show_sql", true)
                    .build();
            final var metadata = new MetadataSources(registry)
                    .addAnnotatedClass(AssetRecord.class)
                    .addAnnotatedClass(AssetMetadataRecord.class)
                    .addAnnotatedClass(AssetSubjectsRecord.class)
                    .addAnnotatedClass(SubjectRecord.class)
                    .buildMetadata();

            return metadata.buildSessionFactory();
        } catch (Exception ex) {
            return new SessionFactory() {
                @Override
                public SessionFactoryOptions getSessionFactoryOptions() {
                    return null;
                }

                @Override
                public SessionBuilder withOptions() {
                    return null;
                }

                @Override
                public Session openSession() throws HibernateException {
                    return null;
                }

                @Override
                public Session getCurrentSession() throws HibernateException {
                    return null;
                }

                @Override
                public StatelessSessionBuilder withStatelessOptions() {
                    return null;
                }

                @Override
                public StatelessSession openStatelessSession() {
                    return null;
                }

                @Override
                public StatelessSession openStatelessSession(Connection connection) {
                    return null;
                }

                @Override
                public Statistics getStatistics() {
                    return null;
                }

                @Override
                public void close() throws HibernateException {

                }

                @Override
                public boolean isClosed() {
                    return false;
                }

                @Override
                public Cache getCache() {
                    return null;
                }

                @Override
                public Set getDefinedFilterNames() {
                    return null;
                }

                @Override
                public FilterDefinition getFilterDefinition(String filterName) throws HibernateException {
                    return null;
                }

                @Override
                public boolean containsFetchProfileDefinition(String name) {
                    return false;
                }

                @Override
                public TypeHelper getTypeHelper() {
                    return null;
                }

                @Override
                public ClassMetadata getClassMetadata(Class entityClass) {
                    return null;
                }

                @Override
                public ClassMetadata getClassMetadata(String entityName) {
                    return null;
                }

                @Override
                public CollectionMetadata getCollectionMetadata(String roleName) {
                    return null;
                }

                @Override
                public Map<String, ClassMetadata> getAllClassMetadata() {
                    return null;
                }

                @Override
                public Map getAllCollectionMetadata() {
                    return null;
                }

                @Override
                public Reference getReference() throws NamingException {
                    return null;
                }

                @Override
                public <T> List<EntityGraph<? super T>> findEntityGraphsByType(Class<T> entityClass) {
                    return null;
                }

                @Override
                public Metamodel getMetamodel() {
                    return null;
                }

                @Override
                public EntityManager createEntityManager() {
                    return null;
                }

                @Override
                public EntityManager createEntityManager(Map map) {
                    return null;
                }

                @Override
                public EntityManager createEntityManager(SynchronizationType synchronizationType) {
                    return null;
                }

                @Override
                public EntityManager createEntityManager(SynchronizationType synchronizationType, Map map) {
                    return null;
                }

                @Override
                public CriteriaBuilder getCriteriaBuilder() {
                    return null;
                }

                @Override
                public boolean isOpen() {
                    return false;
                }

                @Override
                public Map<String, Object> getProperties() {
                    return null;
                }

                @Override
                public PersistenceUnitUtil getPersistenceUnitUtil() {
                    return null;
                }

                @Override
                public void addNamedQuery(String s, javax.persistence.Query query) {

                }

                @Override
                public <T> T unwrap(Class<T> aClass) {
                    return null;
                }

                @Override
                public <T> void addNamedEntityGraph(String s, EntityGraph<T> entityGraph) {

                }
            };
        }
    }
}
