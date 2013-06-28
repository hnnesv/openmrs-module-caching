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
package org.openmrs.module.caching.advice;

import java.lang.reflect.Method;

import org.aopalliance.aop.Advice;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.EncounterType;
import org.openmrs.api.context.Context;
import org.openmrs.module.caching.api.CachingService;
import org.springframework.aop.Advisor;

public class CachingEncounterAdvice implements Advisor {
	
	protected final Log log = LogFactory.getLog(this.getClass());

	public boolean matches(Method method, Class targetClass) {
		return method.getName().equals("saveEncounterType") ||
			method.getName().equals("purgeEncounterType") ||
			method.getName().equals("getEncounterType");
	}
	
	@Override
	public Advice getAdvice() {
		return new EncounterTypeAdvice();
	}

	@Override
	public boolean isPerInstance() {
		return false;
	}

	private class EncounterTypeAdvice implements MethodInterceptor {
		@Override
		public Object invoke(MethodInvocation mi) throws Throwable {
			String name = mi.getMethod().getName();
			
			if (name.equals("saveEncounterType"))
				return saveEncounterType(mi);
			else if (name.equals("purgeEncounterType"))
				return purgeEncounterType(mi);
			else if (name.equals("getEncounterType"))
				return getEncounterType(mi);
			
			return mi.proceed();
		}
		
		private Object saveEncounterType(MethodInvocation mi) throws Throwable {
			log.debug("saveEncounterType");
			EncounterType type = (EncounterType)mi.proceed();
			Context.getService(CachingService.class).putOpenmrsObject(type.getId(), type);
			return type;
		}
		
		private Object purgeEncounterType(MethodInvocation mi) throws Throwable {
			log.debug("purgeEncounterType");
			if (mi.getArguments()==null || mi.getArguments().length==0)
				return mi.proceed();
			
			CachingService cs = Context.getService(CachingService.class);
			EncounterType type = (EncounterType)mi.getArguments()[0];
			Object res = mi.proceed();
			
			if (cs.getOpenmrsObject(EncounterType.class, type.getId())!=null)
				cs.putOpenmrsObject(type.getId(), null);
			if (cs.getOpenmrsObject(EncounterType.class, type.getName())!=null)
				cs.putOpenmrsObject(type.getName(), null);
			
			return res;
		}
		
		private Object getEncounterType(MethodInvocation mi) throws Throwable {
			log.debug("getEncounterType");
			if (mi.getArguments()==null || mi.getArguments().length==0)
				return mi.proceed();
			
			CachingService cs = Context.getService(CachingService.class);
			Object param = mi.getArguments()[0];
			
			if (param instanceof Integer) {
				log.debug("get by id");
				EncounterType type = cs.getOpenmrsObject(EncounterType.class, (Integer)param);
				if (type==null) {
					log.debug("cache miss");
					type = (EncounterType)mi.proceed();
					cs.putOpenmrsObject(type.getId(), type);
				}
				return type;
			} else if (param instanceof String) {
				log.debug("get by name");
				EncounterType type = cs.getOpenmrsObject(EncounterType.class, (String)param);
				if (type==null) {
					log.debug("cache miss");
					type = (EncounterType)mi.proceed();
					cs.putOpenmrsObject(type.getName(), type);
				}
				return type;
			}
			
			return mi.proceed();
		}
	}
}
