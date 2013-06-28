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

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.caching.api.CachingStore;

public class EhcacheStore implements CachingStore {
	
	protected final Log log = LogFactory.getLog(this.getClass());
	
	private static final String CACHE = "openmrs";
    private CacheManager cacheManager;

    @Override
    public void initialize(int timeToLiveSeconds) {
    	cacheManager = CacheManager.create();
    	if (cacheManager.cacheExists(CACHE))
    		cacheManager.removeCache(CACHE);
    	cacheManager.addCache(CACHE);
    	
    	if (timeToLiveSeconds>0) {
    		cacheManager.getCache(CACHE).getCacheConfiguration().setTimeToLiveSeconds(timeToLiveSeconds);
    	}
    }

    @Override
    public void shutdown() {
    	cacheManager.shutdown();
    }

    @Override
    public synchronized Object get(String key) {
    	Element e = cacheManager.getCache(CACHE).get(key);
    	return e!=null ? e.getObjectValue() : null;
    }

    @Override
    public synchronized void put(String key, Object value) {
    	cacheManager.getCache(CACHE).put(new Element(key, value));
    }
}
