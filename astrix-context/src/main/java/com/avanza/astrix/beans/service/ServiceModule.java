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
package com.avanza.astrix.beans.service;

import com.avanza.astrix.context.module.ModuleContext;
import com.avanza.astrix.context.module.NamedModule;

public class ServiceModule implements NamedModule {

	@Override
	public void prepare(ModuleContext moduleContext) {
		moduleContext.bind(ServiceComponentRegistry.class, ServiceComponents.class);
		moduleContext.bind(ObjectSerializerFactory.class, ObjectSerializerFactoryImpl.class);
		
		moduleContext.importType(AstrixVersioningPlugin.class);
		moduleContext.importType(ServiceComponent.class);
		
		moduleContext.export(ServiceComponentRegistry.class);
		moduleContext.export(ObjectSerializerFactory.class);
	}

	@Override
	public String name() {
		return "service";
	}

}
