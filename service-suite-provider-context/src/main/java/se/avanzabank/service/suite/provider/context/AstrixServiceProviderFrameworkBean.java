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
package se.avanzabank.service.suite.provider.context;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.AnnotatedGenericBeanDefinition;
import org.springframework.beans.factory.annotation.Autowire;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;

import se.avanzabank.service.suite.bus.client.AstrixServiceBus;
import se.avanzabank.service.suite.core.AstrixObjectSerializer;

/**
 * @author Elias Lindholm (elilin)
 */
public class AstrixServiceProviderFrameworkBean implements BeanDefinitionRegistryPostProcessor {

	@Override
	public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
		AnnotatedGenericBeanDefinition beanDefinition = new AnnotatedGenericBeanDefinition(AstrixServiceBusExporter.class);
		beanDefinition.setAutowireMode(Autowire.BY_TYPE.value());
		registry.registerBeanDefinition("_astrixServiceBusExporter", beanDefinition);

		beanDefinition = new AnnotatedGenericBeanDefinition(AstrixServiceBus.class);
		beanDefinition.setAutowireMode(Autowire.BY_TYPE.value());
		registry.registerBeanDefinition("_astrixServiceExporterBean", beanDefinition);
//		
//		beanDefinition = new AnnotatedGenericBeanDefinition(AstrixRemoteServiceProviderFactory.class);
//		beanDefinition.setAutowireMode(Autowire.BY_TYPE.value());
//		registry.registerBeanDefinition("_astrixRemoteServiceProviderFactory", beanDefinition);
//		
//		beanDefinition = new AnnotatedGenericBeanDefinition(AstrixRemoteServiceProvider.class);
//		beanDefinition.setAutowireMode(Autowire.BY_TYPE.value());
//		beanDefinition.setFactoryBeanName("_astrixRemoteServiceProviderFactory");
//		beanDefinition.setFactoryMethodName("create");
//		registry.registerBeanDefinition("_astrixRemoteServiceProvider", beanDefinition);
//		
//		beanDefinition = new AnnotatedGenericBeanDefinition(AstrixRemotingArgumentSerializerFactory.class);
//		beanDefinition.setAutowireMode(Autowire.BY_TYPE.value());
//		registry.registerBeanDefinition("_astrixRemotingArgumentSerializerFactory", beanDefinition);
//		
//		beanDefinition = new AnnotatedGenericBeanDefinition(AstrixObjectSerializer.class);
//		beanDefinition.setAutowireMode(Autowire.BY_TYPE.value());
//		beanDefinition.setScope(AbstractBeanDefinition.SCOPE_SINGLETON);
//		beanDefinition.setFactoryBeanName("_astrixRemotingArgumentSerializerFactory");
//		beanDefinition.setFactoryMethodName("create");
//		registry.registerBeanDefinition("_astrixRemotingArgumentSerializer", beanDefinition);
	}

	@Override
	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
		// intentionally empty, inherited from BeanDefinitionRegistryPostProcessor
	}
}