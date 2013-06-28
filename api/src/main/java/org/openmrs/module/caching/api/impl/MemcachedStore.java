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

import java.io.IOException;
import java.net.InetSocketAddress;

import net.spy.memcached.MemcachedClient;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.module.caching.api.CachingStore;
import org.openmrs.module.caching.api.CachingStoreInitializationFailed;
import org.openmrs.module.serialization.xstream.XStreamShortSerializer;
import org.openmrs.serialization.SerializationException;

public class MemcachedStore implements CachingStore {
	
	protected final Log log = LogFactory.getLog(this.getClass());
	
	private MemcachedClient client;
	private String host;
	private int port;
	private int expiry;
	
	public MemcachedStore(String host, int port, int expiry) {
		this.host = host;
		this.port = port;
		this.expiry = expiry;
	}

	@Override
	public void initialize(int timeToLiveSeconds) throws CachingStoreInitializationFailed {
		try {
			client = new MemcachedClient(new InetSocketAddress(host, port));
		} catch (IOException ex) {
			throw new CachingStoreInitializationFailed(ex);
		}
		
		expiry = timeToLiveSeconds;
		if (expiry<=0) expiry = 600; //who wants to live forever?
	}

	@Override
	public void shutdown() {
		client.shutdown();
	}

	
	@SuppressWarnings("unchecked")
	@Override
	public synchronized Object get(String key) {
		try {
			if (log.isDebugEnabled()) log.debug("Retrieving object with key: " + key);
			
			MemcachedValue mVal = (MemcachedValue)client.get(key);
			Object result = null;
			if (mVal!=null) {
				result = Context.getSerializationService().deserialize(mVal.serializedValue, mVal.clazz, XStreamShortSerializer.class);
			}
			return result;
		} catch (SerializationException e) {
			log.warn("deserialization error", e);
		}
		return null;
	}

	@Override
	public synchronized void put(String key, Object value) {
		try {
			if (log.isDebugEnabled()) log.debug("Storing object with key: " + key);
			
			MemcachedValue mVal = new MemcachedValue();
			mVal.clazz = value.getClass();
			mVal.serializedValue = Context.getSerializationService().serialize(value, XStreamShortSerializer.class);
			
			client.set(key, expiry, mVal);
		} catch (SerializationException e) {
			log.warn("Serialization error", e);
		}
	}
	
	private static class MemcachedValue implements java.io.Serializable {
		private static final long serialVersionUID = -7922169977166508159L;
		@SuppressWarnings("rawtypes") Class clazz;
		String serializedValue;
	}
}
