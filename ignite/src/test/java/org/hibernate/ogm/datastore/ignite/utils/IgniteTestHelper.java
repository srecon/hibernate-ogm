/*
 * Hibernate OGM, Domain model persistence for NoSQL datastores
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.ogm.datastore.ignite.utils;

import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

import org.apache.ignite.IgniteCache;
import org.apache.ignite.binary.BinaryObject;
import org.apache.ignite.cache.CachePeekMode;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.ogm.datastore.document.options.AssociationStorageType;
import org.hibernate.ogm.datastore.ignite.Ignite;
import org.hibernate.ogm.datastore.ignite.IgniteDialect;
import org.hibernate.ogm.datastore.ignite.impl.IgniteDatastoreProvider;
import org.hibernate.ogm.datastore.ignite.impl.IgnitePortableTupleSnapshot;
import org.hibernate.ogm.datastore.spi.DatastoreConfiguration;
import org.hibernate.ogm.datastore.spi.DatastoreProvider;
import org.hibernate.ogm.dialect.spi.GridDialect;
import org.hibernate.ogm.model.key.spi.AssociationKeyMetadata;
import org.hibernate.ogm.model.key.spi.EntityKey;
import org.hibernate.ogm.model.key.spi.EntityKeyMetadata;
import org.hibernate.ogm.model.spi.TupleSnapshot;
import org.hibernate.ogm.persister.impl.OgmCollectionPersister;
import org.hibernate.ogm.persister.impl.OgmEntityPersister;
import org.hibernate.ogm.utils.GridDialectTestHelper;
import org.hibernate.persister.collection.CollectionPersister;
import org.hibernate.persister.entity.EntityPersister;

/**
 * @author Dmitriy Kozlov
 */
public class IgniteTestHelper implements GridDialectTestHelper {

	@Override
	public long getNumberOfEntities(SessionFactory sessionFactory) {
		int entityCount = 0;
		Set<IgniteCache<?, ?>> processedCaches = Collections.newSetFromMap( new IdentityHashMap<IgniteCache<?, ?>, Boolean>() );
		for ( EntityPersister entityPersister : ( (SessionFactoryImplementor) sessionFactory ).getEntityPersisters().values() ) {
			IgniteCache<?, ?> entityCache = getEntityCache( sessionFactory, ( (OgmEntityPersister) entityPersister ).getEntityKeyMetadata() );
			if ( !processedCaches.contains( entityCache ) ) {
				entityCount += entityCache.size( CachePeekMode.ALL );
				processedCaches.add( entityCache );
			}
		}
		return entityCount;
	}

	@Override
	public long getNumberOfAssociations(SessionFactory sessionFactory) {
		int associationCount = 0;
		Set<IgniteCache<?, ?>> processedCaches = Collections.newSetFromMap( new IdentityHashMap<IgniteCache<?, ?>, Boolean>() );
		for ( CollectionPersister colleactionPersister : ( (SessionFactoryImplementor) sessionFactory ).getCollectionPersisters().values() ) {
			IgniteCache<?, ?> associationCache = getAssociationCache( sessionFactory, ( (OgmCollectionPersister) colleactionPersister ).getAssociationKeyMetadata() );
			if ( !processedCaches.contains( associationCache ) ) {
				associationCount += associationCache.size( CachePeekMode.ALL );
				processedCaches.add( associationCache );
			}
		}
		return associationCount;
	}

	@Override
	public long getNumberOfAssociations(SessionFactory sessionFactory, AssociationStorageType type) {
		int asscociationCount = 0;
		Set<IgniteCache<String, BinaryObject>> processedCaches = Collections.newSetFromMap( new IdentityHashMap<IgniteCache<String, BinaryObject>, Boolean>() );

		for ( CollectionPersister collectionPersister : ( (SessionFactoryImplementor) sessionFactory ).getCollectionPersisters().values() ) {
			IgniteCache<String, BinaryObject> associationCache = getAssociationCache( sessionFactory, ( (OgmCollectionPersister) collectionPersister ).getAssociationKeyMetadata() );
			if ( !processedCaches.contains( associationCache ) ) {
				asscociationCount += associationCache.size();
				processedCaches.add( associationCache );
			}
		}

		return asscociationCount;
	}

	@Override
	public Map<String, Object> extractEntityTuple(Session session, EntityKey key) {
		SessionFactoryImplementor sessionFactory = (SessionFactoryImplementor) session.getSessionFactory();
		IgniteCache<String, BinaryObject> cache = getEntityCache( sessionFactory, key.getMetadata() );
		String cacheKey = getProvider( sessionFactory ).getKeyProvider().getKeyString( key );

		Map<String, Object> result = new HashMap<>();
		Object po = cache.get( cacheKey );

		IgniteDialect igniteDialect = (IgniteDialect) sessionFactory.getServiceRegistry().getService( GridDialect.class );
		TupleSnapshot snapshot = new IgnitePortableTupleSnapshot( po );
		for (String fieldName : snapshot.getColumnNames()) {
			result.put( fieldName, snapshot.get( fieldName ) );
		}

		return result;
	}

	@Override
	public boolean backendSupportsTransactions() {
		return true;
	}

	@Override
	public void dropSchemaAndDatabase(SessionFactory sessionFactory) {
	}

	@Override
	public Map<String, String> getEnvironmentProperties() {
		// TODO and here?
		return null;
	}

	@Override
	public GridDialect getGridDialect(DatastoreProvider datastoreProvider) {
		return new IgniteDialect((IgniteDatastoreProvider) datastoreProvider);
	}

	public static IgniteCache<String, BinaryObject> getEntityCache(SessionFactory sessionFactory, EntityKeyMetadata entityKeyMetadata) {
		IgniteDatastoreProvider castProvider = getProvider( sessionFactory );
		return castProvider.getEntityCache( entityKeyMetadata );
	}

	public static IgniteCache<String, BinaryObject> getAssociationCache(SessionFactory sessionFactory, AssociationKeyMetadata associationKeyMetadata) {
		IgniteDatastoreProvider castProvider = getProvider( sessionFactory );
		return castProvider.getAssociationCache( associationKeyMetadata );
	}

	private static IgniteDatastoreProvider getProvider(SessionFactory sessionFactory) {
		DatastoreProvider provider = ( (SessionFactoryImplementor) sessionFactory ).getServiceRegistry()
				.getService( DatastoreProvider.class );
		if ( !( IgniteDatastoreProvider.class.isInstance( provider ) ) ) {
			throw new RuntimeException( "Not testing with Ignite, cannot extract underlying cache" );
		}
		return IgniteDatastoreProvider.class.cast( provider );
	}

	@Override
	public Class<? extends DatastoreConfiguration<?>> getDatastoreConfigurationType() {
		return Ignite.class;
	}

	@Override
	public long getNumberOfEntities(Session session) {
		return getNumberOfEntities( session.getSessionFactory() );
	}

	@Override
	public long getNumberOfAssociations(Session session) {
		return getNumberOfAssociations( session.getSessionFactory() );
	}

}
