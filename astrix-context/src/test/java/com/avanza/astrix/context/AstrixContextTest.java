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

import static junit.framework.Assert.assertSame;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.junit.Test;

import com.avanza.astrix.provider.library.AstrixExport;
import com.avanza.astrix.provider.library.AstrixLibraryProvider;

public class AstrixContextTest {
	
	@Test
	public void astrixContextShouldInstantiateAndInjectRequiredClasses() throws Exception {
		TestAstrixConfigurer AstrixConfigurer = new TestAstrixConfigurer();
		AstrixContext AstrixContext = AstrixConfigurer.configure();
		MultipleDependentClass dependentClass = AstrixContext.getInstance(MultipleDependentClass.class);
		
		assertNotNull(dependentClass.dep);
		assertNotNull(dependentClass.dep2);
		assertNotNull(dependentClass.dep3);
	}
	
	@Test(expected = MissingBeanProviderException.class)
	public void detectsMissingBeans() throws Exception {
		TestAstrixConfigurer AstrixConfigurer = new TestAstrixConfigurer();
		AstrixContext AstrixContext = AstrixConfigurer.configure();

		AstrixContext.getBean(HelloBean.class);
	}
	
	@Test(expected = MissingBeanDependencyException.class)
	public void detectsMissingBeanDependencies() throws Exception {
		TestAstrixConfigurer AstrixConfigurer = new TestAstrixConfigurer();
		AstrixConfigurer.registerApiProvider(DependentApi.class);
		AstrixContext AstrixContext = AstrixConfigurer.configure();

		AstrixContext.getBean(DependentBean.class);
	}
	
	@Test
	public void asterixBeansAreDestroyedUponContextDestroy() throws Exception {
		TestAstrixConfigurer AstrixConfigurer = new TestAstrixConfigurer();
		AstrixConfigurer.registerApiProvider(HelloBeanLibrary.class);
		AstrixContext astrixContext = AstrixConfigurer.configure();

		HelloBean helloBean = astrixContext.getBean(HelloBean.class);
		assertFalse(helloBean.destroyed);
		
		astrixContext.destroy();
		
		assertTrue(helloBean.destroyed);
	}
	
	@Test
	public void asterixBeansAreInitializedWhenFirstCreated() throws Exception {
		TestAstrixConfigurer AstrixConfigurer = new TestAstrixConfigurer();
		AstrixConfigurer.registerApiProvider(HelloBeanLibrary.class);
		AstrixContext astrixContext = AstrixConfigurer.configure();

		HelloBean helloBean = astrixContext.getBean(HelloBean.class);
		assertTrue(helloBean.initialized);
	}
	
	@Test
	public void cachesCreatedApis() throws Exception {
		TestAstrixConfigurer AstrixConfigurer = new TestAstrixConfigurer();
		AstrixConfigurer.registerApiProvider(HelloBeanLibrary.class);
		AstrixContext AstrixContext = AstrixConfigurer.configure();

		HelloBean beanA = AstrixContext.getBean(HelloBean.class);
		HelloBean beanB = AstrixContext.getBean(HelloBean.class);
		assertSame(beanA, beanB);
	}
	
	@Test
	public void canInitRegularClasses() throws Exception {
		TestAstrixConfigurer AstrixConfigurer = new TestAstrixConfigurer();
		AstrixContext AstrixContext = AstrixConfigurer.configure();
		InternalFrameworkClass simple = AstrixContext.getInstance(InternalFrameworkClass.class);
		
		assertNotNull(simple.settings);
	}
	
	@Test
	public void cachesCreatedInstancesOfRegularClasses() throws Exception {
		TestAstrixConfigurer astrixConfigurer = new TestAstrixConfigurer();
		AstrixContext astrixContext = astrixConfigurer.configure();
		InternalFrameworkClass simple1 = astrixContext.getInstance(InternalFrameworkClass.class);
		InternalFrameworkClass simple2 = astrixContext.getInstance(InternalFrameworkClass.class);
		
		assertSame(simple1, simple2);
	}
	
	@Test
	public void preDestroyAnnotatedMethodsOnInstancesCreatedByAstrixAreInvokedWhenAstrixContextIsDestroyed() throws Exception {
		TestAstrixConfigurer astrixConfigurer = new TestAstrixConfigurer();
		AstrixContext astrixContext = astrixConfigurer.configure();
		HelloBean simpleClass = astrixContext.getInstance(HelloBean.class); // treat HelloBean as internal class
		assertFalse(simpleClass.destroyed);
		
		astrixContext.destroy();
		assertTrue(simpleClass.destroyed);
	}

	@Test
	public void manyAstrixContextShouldBeAbleToRunInSameProcessWithoutInterferenceInShutdown() throws Exception {
		TestAstrixConfigurer AstrixConfigurer = new TestAstrixConfigurer();
		AstrixConfigurer.registerApiProvider(HelloBeanLibrary.class);
		AstrixContext context = AstrixConfigurer.configure();
		
		TestAstrixConfigurer AstrixConfigurer2 = new TestAstrixConfigurer();
		AstrixConfigurer2.registerApiProvider(HelloBeanLibrary.class);
		AstrixContext context2 = AstrixConfigurer2.configure();
		context.destroy();
		
		HelloBean helloBean2 = (HelloBean) context2.getBean(HelloBean.class);
		assertFalse(helloBean2.destroyed);
		context2.destroy();
		assertTrue(helloBean2.destroyed);
	}
	
	@AstrixLibraryProvider
	static class DependentApi {
		
		@AstrixExport
		public DependentBean create(GoodbyeBean bean) {
			return new DependentBean();
		}
	}
	
	static class DependentBean {
		public String chat(String msg) {
			return "yada yada yada: " + msg;
		}
	}
	
	static class GoodbyeBean {
		public String goodbye() {
			return "goodbye";
		}
	}
	
	@AstrixLibraryProvider
	static class HelloBeanLibrary {
		@AstrixExport
		public HelloBean create() {
			return new HelloBean();
		}
	}
	
	static class InternalFrameworkClass implements AstrixSettingsAware {
		private AstrixSettingsReader settings;

		@Override
		public void setSettings(AstrixSettingsReader settings) {
			this.settings = settings;
		}
	}
	
	static class HelloBean {
		
		boolean initialized = false;
		boolean destroyed = false;
		public String chat(String msg) {
			return "yada yada yada: " + msg;
		}
		
		@PostConstruct
		public void init() {
			this.initialized = true;
		}
		
		@PreDestroy
		public void destroyed() {
			this.destroyed = true;
		}
	}

	static class MultipleDependentClass {
		
		private GoodbyeBean dep;
		private HelloBean dep2;
		private GoodbyeBean dep3;

		@AstrixInject
		public void setVersioningPlugin(GoodbyeBean dep, HelloBean dep2) {
			this.dep = dep;
			this.dep2 = dep2;
		}
		
		@AstrixInject
		public void setVersioningPlugin(GoodbyeBean dep3) {
			this.dep3 = dep3;
		}
	}
	
}
