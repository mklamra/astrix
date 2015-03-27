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
package com.avanza.astrix.gs.remoting;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import org.openspaces.core.GigaSpace;

import rx.Observable;
import rx.Subscriber;

import com.avanza.astrix.ft.Command;
import com.avanza.astrix.ft.CommandSettings;
import com.avanza.astrix.ft.ObservableCommandSettings;
import com.avanza.astrix.ft.plugin.AstrixFaultTolerance;
import com.avanza.astrix.remoting.client.AstrixRemotingTransport;
import com.avanza.astrix.remoting.client.AstrixServiceInvocationRequest;
import com.avanza.astrix.remoting.client.AstrixServiceInvocationRequestHeaders;
import com.avanza.astrix.remoting.client.AstrixServiceInvocationResponse;
import com.avanza.astrix.remoting.client.RemotingTransportSpi;
import com.avanza.astrix.remoting.client.RoutingKey;
import com.gigaspaces.async.AsyncFuture;
import com.gigaspaces.async.AsyncFutureListener;
import com.gigaspaces.async.AsyncResult;
/**
 * RemotingTransport implementation based on GigaSpaces task execution. <p> 
 * 
 * @author Elias Lindholm
 *
 */
public class GsRemotingTransport implements RemotingTransportSpi {

	private final GigaSpace gigaSpace;
	private final AstrixFaultTolerance faultTolerance;
	
	public GsRemotingTransport(GigaSpace gigaSpace, AstrixFaultTolerance faultTolerance) {
		this.gigaSpace = gigaSpace;
		this.faultTolerance = faultTolerance;
	}

	@Override
	public Observable<AstrixServiceInvocationResponse> processRoutedRequest(final AstrixServiceInvocationRequest request, final RoutingKey routingKey) {
		final AsyncFuture<AstrixServiceInvocationResponse> response = 
				this.faultTolerance.execute(new Command<AsyncFuture<AstrixServiceInvocationResponse>>() {
					@Override
					public AsyncFuture<AstrixServiceInvocationResponse> call() {
						return gigaSpace.execute(new AstrixServiceInvocationTask(request), routingKey);
					}
				}, new CommandSettings(gigaSpace.getName() + "_RemotingDispatcher", gigaSpace.getName()));
		Observable<AstrixServiceInvocationResponse> observable = Observable.create(new Observable.OnSubscribe<AstrixServiceInvocationResponse>() {
			@Override
			public void call(final Subscriber<? super AstrixServiceInvocationResponse> t1) {
				response.setListener(new AsyncFutureListener<AstrixServiceInvocationResponse>() {
					@Override
					public void onResult(AsyncResult<AstrixServiceInvocationResponse> result) {
						if (result.getException() == null) {
							t1.onNext(result.getResult());
							t1.onCompleted();
						} else {
							t1.onError(result.getException());
						}
					}
				});
			}
		});
		return faultTolerance.observe(observable, getServiceCommandSettings(request));
	}

	@Override
	public Observable<List<AstrixServiceInvocationResponse>> processBroadcastRequest(final AstrixServiceInvocationRequest request) {
		final AsyncFuture<List<AsyncResult<AstrixServiceInvocationResponse>>> responses = 
				faultTolerance.execute(new Command<AsyncFuture<List<AsyncResult<AstrixServiceInvocationResponse>>>>() {
						@Override
						public AsyncFuture<List<AsyncResult<AstrixServiceInvocationResponse>>> call() {
							return gigaSpace.execute(new AstrixDistributedServiceInvocationTask(request));
						}
					}, new CommandSettings(gigaSpace.getName() + "_RemotingDispatcher", gigaSpace.getName()));
		Observable<List<AstrixServiceInvocationResponse>> observable = Observable.create(new Observable.OnSubscribe<List<AstrixServiceInvocationResponse>>() {
			@Override
			public void call(final Subscriber<? super List<AstrixServiceInvocationResponse>> subscriber) {
				responses.setListener(new AsyncFutureListener<List<AsyncResult<AstrixServiceInvocationResponse>>>() {
					@Override
					public void onResult(AsyncResult<List<AsyncResult<AstrixServiceInvocationResponse>>> asyncRresult) {
						if (asyncRresult.getException() == null) {
							List<AstrixServiceInvocationResponse> result = new ArrayList<>();
							for (AsyncResult<AstrixServiceInvocationResponse> asyncInvocationResponse : asyncRresult.getResult()) {
								if (asyncInvocationResponse.getException() != null) {
									subscriber.onError(asyncInvocationResponse.getException());
									return;
								}
								result.add(asyncInvocationResponse.getResult());
							}
							subscriber.onNext(result);
							subscriber.onCompleted();
						} else {
							subscriber.onError(asyncRresult.getException());
						}
					}
				});
			}
		});
		return faultTolerance.observe(observable, getServiceCommandSettings(request));
	}
	
	private ObservableCommandSettings getServiceCommandSettings(AstrixServiceInvocationRequest request) {
		String api = request.getHeader(AstrixServiceInvocationRequestHeaders.SERVICE_API);
		String spaceName = gigaSpace.getName();
		String[] subPackagesAndClassName = api.split("[.]");
		if (subPackagesAndClassName.length > 0) {
			api = subPackagesAndClassName[subPackagesAndClassName.length - 1]; // Use simple class name without package name
		}
		String commandKey = spaceName + "_" + api;
		ObservableCommandSettings commandSettings = new ObservableCommandSettings(commandKey, gigaSpace.getName());
		return commandSettings;
	}

	public static AstrixRemotingTransport remoteSpace(GigaSpace gigaSpace, AstrixFaultTolerance faultTolerance) {
		return AstrixRemotingTransport.create(new GsRemotingTransport(gigaSpace, faultTolerance));
	}
	
}