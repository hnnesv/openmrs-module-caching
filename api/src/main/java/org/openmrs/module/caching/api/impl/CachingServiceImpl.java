/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.module.caching.api.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.BaseOpenmrsObject;
import org.openmrs.api.context.Context;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.caching.api.CachingService;
import org.openmrs.module.caching.api.CachingStore;
import org.openmrs.module.caching.api.CachingStoreInitializationFailed;

/**
 * It is a default implementation of {@link CachingService}.
 */
public class CachingServiceImpl extends BaseOpenmrsService implements CachingService {
	
	protected final Log log = LogFactory.getLog(this.getClass());
	
	private static final String PROP_BACKEND = "caching.backend";
	private static final String PROP_TIMETOLIVE = "caching.timetolive";
	private static final String BACKEND_EHCACHE = "ehcache";
	private static final String BACKEND_MEMCACHED = "memcached";
	private static final String PROP_MEMCACHED_HOST = "caching.memcached.host";
	private static final String PROP_MEMCACHED_PORT = "caching.memcached.port";
	
	private CachingStore store;

	@Override
	public void initialize() throws CachingStoreInitializationFailed {
		String backend = getProp(PROP_BACKEND);
		
		if (BACKEND_EHCACHE.equalsIgnoreCase(backend)) {
			log.info("Initializing Ehcache backend...");
			store = new EhcacheStore();
		} else if (BACKEND_MEMCACHED.equalsIgnoreCase(backend)) {
			log.info("Initializing Memcached backend...");
			String host = getProp(PROP_MEMCACHED_HOST);
			String portStr = getProp(PROP_MEMCACHED_PORT);
			try {
				store = new MemcachedStore(host, Integer.parseInt(portStr), 600);
			} catch (NumberFormatException ex) {
				throw new CachingStoreInitializationFailed("Invalid memcached port specified");
			}
		} else {
			throw new CachingStoreInitializationFailed("Unknown caching backend specified");
		}
		
		store.initialize(getTimeToLiveProp());
		log.info("Caching store initialized");
	}
	
	private int getTimeToLiveProp() throws CachingStoreInitializationFailed {
		String timeToLiveStr = getProp(PROP_TIMETOLIVE);
		if (timeToLiveStr==null || timeToLiveStr.isEmpty())
			return 0;
		
		try {
			return Integer.parseInt(timeToLiveStr);
		} catch (NumberFormatException ex) {
			throw new CachingStoreInitializationFailed("Invalid time to live setting specified");
		}
	}
	
	private String getProp(String name) throws CachingStoreInitializationFailed {
		String prop = Context.getAdministrationService().getGlobalProperty(name);
		if (prop==null)
			throw new CachingStoreInitializationFailed(String.format("Global property '%s' not set", name));
		return prop;
	}

	
	@Override
	public void shutdown() {
		if (store!=null) store.shutdown();
	}
	

	@Override
	public Object get(String key) {
		return store.get(key);
	}

	@Override
	public void put(String key, Object value) {
		store.put(key, value);
		
	}
	
	@Override
	public <T extends BaseOpenmrsObject> T getOpenmrsObject(Class<T> type, String key) {
		Object res = store.get(type.getCanonicalName() + "_" + key);
		return res!=null ? type.cast(res) : null;
	}
	
	@Override
	public <T extends BaseOpenmrsObject> void putOpenmrsObject(String key, T value) {
		store.put(value.getClass().getCanonicalName() + "_" + key, value);
	}
	
	@Override
	public <T extends BaseOpenmrsObject> T getOpenmrsObject(Class<T> type, Integer id) {
		Object res = store.get(type.getCanonicalName() + "_id_" + id);
		return res!=null ? type.cast(res) : null;
	}
	
	@Override
	public <T extends BaseOpenmrsObject> void putOpenmrsObject(Integer id, T value) {
		store.put(value.getClass().getCanonicalName() + "_id_" + id, value);
	}
}
