/*
 * Copyright 2014 Avanza Bank AB
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.avanza.astrix.core.util;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class ReflectionUtil {
	
	@SuppressWarnings("unchecked")
	public static <T> T newProxy(Class<T> type, InvocationHandler handler) {
		return (T) Proxy.newProxyInstance(type.getClassLoader(), new Class[]{type}, handler);
	}
	
	/**
	 * Reflectively invokes the given Method. InvocationTargetExcpetion's thrown
	 * will be unfolded.
	 * 
	 * @param method
	 * @param target
	 * @param args
	 * @return
	 * @throws Throwable
	 */
	public static Object invokeMethod(Method method, Object target, Object[] args) throws Throwable {
		try {
			return method.invoke(target, args);
		} catch (InvocationTargetException e) {
			throw e.getCause();
		}
	}
	
	public static <T> T newInstance(Class<T> type) {
		try {
			return type.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			throw new RuntimeException("Failed to instantiate class: " + type.getName(), e);
		}
	}
	
	public static Method getMethod(Class<? extends Object> type, String name, Class<?>... parameterTypes) {
		try {
			return type.getMethod(name, parameterTypes);
		} catch (NoSuchMethodException | SecurityException e) {
			throw new RuntimeException("Failed to get method from class", e);
		}
	}
	
	public static Class<?> classForName(String name) {
		try {
			return Class.forName(name);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException("Failed to load class: " + name, e);
		}
	}

}
