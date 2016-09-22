/*
 * Hibernate OGM, Domain model persistence for NoSQL datastores
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.ogm.datastore.ignite.configuration.impl;

import java.net.URL;
import java.util.Map;

import org.hibernate.ogm.datastore.ignite.IgniteProperties;
import org.hibernate.ogm.datastore.ignite.impl.IgniteDatastoreProvider;
import org.hibernate.ogm.util.configurationreader.spi.ConfigurationPropertyReader;
import org.hibernate.ogm.util.impl.Log;
import org.hibernate.ogm.util.impl.LoggerFactory;

/**
 * Configuration for {@link IgniteDatastoreProvider}.
 * @author Dmitriy Kozlov
 */
public class IgniteProviderConfiguration {

	private static final Log log = LoggerFactory.make();

	/**
	 * Name of the default Ignite configuration file
	 */
	private static final String DEFAULT_CONFIG = "ignite-config.xml";

	private URL url;
	private String instanceName;

	/**
	 * Initialize the internal values from the given {@link Map}.
	 *
	 * @param configurationMap The values to use as configuration
	 */
	public void initialize(Map configurationMap) {
		this.url = new ConfigurationPropertyReader( configurationMap )
			.property( IgniteProperties.CONFIGURATION_RESOURCE_NAME, URL.class )
			.withDefault( IgniteProviderConfiguration.class.getClassLoader().getResource( DEFAULT_CONFIG ) )
//			.withValidator(
//					new PropertyValidator<URL>() {
//						@Override
//						public void validate( URL value ) throws HibernateException {
//							if ( value == null ) {
//								throw log.missingConfigurationProperty( IgniteProperties.CONFIGURATION_RESOURCE_NAME );
//							}
//						}
//					}
//			)
			.getValue();

		this.instanceName = new ConfigurationPropertyReader( configurationMap )
				.property( IgniteProperties.IGNITE_INSTANCE_NAME, String.class )
				.getValue();
	}

	/**
	 * @see IgniteProperties#CONFIGURATION_RESOURCE_NAME
	 * @return An URL to Ignite configuration file
	 */
	public URL getUrl() {
		return url;
	}

	/**
	 * @see IgniteProperties#IGNITE_INSTANCE_NAME
	 * @return the name of existing Ignite instance
	 */
	public String getInstanceName() {
		return instanceName;
	}
}
