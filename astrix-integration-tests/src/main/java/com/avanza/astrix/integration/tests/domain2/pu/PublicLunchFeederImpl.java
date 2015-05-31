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
package com.avanza.astrix.integration.tests.domain2.pu;

import org.springframework.beans.factory.annotation.Autowired;

import com.avanza.astrix.integration.tests.domain.api.LunchRestaurant;
import com.avanza.astrix.integration.tests.domain.apiruntime.feeder.InternalLunchFeeder;
import com.avanza.astrix.integration.tests.domain2.apiruntime.PublicLunchFeeder;
import com.avanza.astrix.provider.core.AstrixServiceExport;

@AstrixServiceExport(PublicLunchFeeder.class)
public class PublicLunchFeederImpl implements PublicLunchFeeder {

	// Since this is part of the same subsystem, this app is allowed to invoke it
	private InternalLunchFeeder internalLunchFeeder;

	@Autowired
	public PublicLunchFeederImpl(InternalLunchFeeder internalLunchFeeder) {
		this.internalLunchFeeder = internalLunchFeeder;
	}

	@Override
	public void addLunchRestaurant(LunchRestaurant restaurant) {
		this.internalLunchFeeder.addLunchRestaurant(restaurant);
	}

}
