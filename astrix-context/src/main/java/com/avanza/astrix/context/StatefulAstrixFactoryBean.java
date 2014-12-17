/*
 * Copyright 2014-2015 Avanza Bank AB
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
package com.avanza.astrix.context;

import java.lang.reflect.Proxy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Elias Lindholm (elilin)
 *
 * @param <T>
 */
final class StatefulAstrixFactoryBean<T> implements AstrixFactoryBeanPlugin<T>, AstrixDecorator {

	private static final Logger log = LoggerFactory.getLogger(StatefulAstrixFactoryBean.class);
	private final AstrixFactoryBeanPlugin<T> targetFactory;
	private EventBus eventBus;
	private AstrixBeanStateWorker beanStateWorker;
	
	public StatefulAstrixFactoryBean(AstrixFactoryBeanPlugin<T> targetFactory) {
		if (!targetFactory.getBeanKey().getBeanType().isInterface()) {
			throw new IllegalArgumentException("Can only create stateful Astrix beans if bean is exported using an interface." +
											   " targetBean=" + targetFactory.getBeanKey() + 
											   " beanFactoryType=" + targetFactory.getClass().getName());
		}
		this.targetFactory = targetFactory;
	}

	@Override
	public T create(String optionalQualifier) {
		StatefulAstrixBean<T> handler = new StatefulAstrixBean<>(targetFactory, optionalQualifier, eventBus);
		try {
			handler.bind();
		} catch (Exception e) {
			log.info("Failed to bind to " + handler.getBeanFactory().getBeanKey() + " astrixBeanId=" + handler.getId(), e);
		}
		beanStateWorker.add(handler);
		return targetFactory.getBeanKey().getBeanType().cast(
				Proxy.newProxyInstance(targetFactory.getBeanKey().getBeanType().getClassLoader(), new Class<?>[]{targetFactory.getBeanKey().getBeanType()}, handler));
	}

	@Override
	public AstrixBeanKey<T> getBeanKey() {
		return targetFactory.getBeanKey();
	}

	@Override
	public Object getTarget() {
		return targetFactory;
	}

	@AstrixInject
	public void setEventBus(EventBus eventBus) {
		this.eventBus = eventBus;
	}

	@AstrixInject
	public void setBeanStateWorker(AstrixBeanStateWorker worker) {
		this.beanStateWorker = worker;
	}
	
	@Override
	public boolean isStateful() {
		return true;
	}

}
