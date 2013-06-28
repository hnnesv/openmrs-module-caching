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

import static org.junit.Assert.*;

import org.junit.Test;
import org.openmrs.module.caching.api.CachingStore;
import org.openmrs.module.caching.api.CachingStoreInitializationFailed;

public class EhcacheStoreTest {

	@Test
	public void testBasicFunctionality() {
		CachingStore store = new EhcacheStore();
		try {
			store.initialize(0);
		} catch (CachingStoreInitializationFailed ex) {
			fail();
		}
		
		try {
			store.put("test1", "1234");
			assertNotNull(store.get("test1"));
			assertEquals(store.get("test1"), "1234");
			
			store.put("test2", new TestObject());
			assertNotNull(store.get("test2"));
			assertTrue(store.get("test2") instanceof TestObject);
			assertEquals(((TestObject)store.get("test2")).x, 1234);
			
			store.put("test3", null);
			assertNull(store.get("test3"));
			
			assertNull(store.get("test4"));
		} finally {
			store.shutdown();
		}
	}
	
	@Test
	public void testExpiry() {
		CachingStore store = new EhcacheStore();
		try {
			store.initialize(2);
		} catch (CachingStoreInitializationFailed ex) {
			fail();
		}
		
		try {
			store.put("test1", "1234");
			assertNotNull(store.get("test1"));
			assertEquals(store.get("test1"), "1234");
			
			store.put("test2", "1234");
			assertNotNull(store.get("test2"));
			assertEquals(store.get("test2"), "1234");
			try { Thread.sleep(1000); } catch (InterruptedException e) {}
			assertNotNull(store.get("test2"));
			try { Thread.sleep(1000); } catch (InterruptedException e) {}
			assertNull(store.get("test2"));
		} finally {
			store.shutdown();
		}
	}

	
	protected static class TestObject {
		int x = 1234;
	}
}
