/*
 * Copyright 2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.nokee.internal.testing.reflect;

import java.lang.reflect.Method;

public interface MethodInformation<ReceiverType, ReturnType extends ReturnInformation> {
	Method resolve(Class<ReceiverType> type);

	interface WithArguments<ReceiverType, ReturnInfoType extends ReturnInformation, ArgumentInfoType extends ArgumentInformation> extends MethodInformation<ReceiverType, ReturnInfoType> {}

	@SuppressWarnings("overloads")
	static <ReceiverType, ExceptionType extends Throwable> MethodInformation<ReceiverType, ReturnInformation.None> method(MethodCallable.ForArg0<ReceiverType, ExceptionType> callable) {
		return new ByProxyMethodInformation<>(new InvokeMethodCallableConsumer<>(callable));
	}

	@SuppressWarnings("overloads")
	static <ReceiverType, A0, ExceptionType extends Throwable> MethodInformation.WithArguments<ReceiverType, ReturnInformation.None, ArgumentInformation.Arg1<A0>> method(MethodCallable.ForArg1<ReceiverType, A0, ExceptionType> callable) {
		return new ByProxyMethodInformation<>(new InvokeMethodCallableConsumer<>(callable));
	}

	@SuppressWarnings("overloads")
	static <ReceiverType, A0, A1, ExceptionType extends Throwable> MethodInformation.WithArguments<ReceiverType, ReturnInformation.None, ArgumentInformation.Arg2<A0, A1>> method(MethodCallable.ForArg2<ReceiverType, A0, A1, ExceptionType> callable) {
		return new ByProxyMethodInformation<>(new InvokeMethodCallableConsumer<>(callable));
	}

	@SuppressWarnings("overloads")
	static <ReceiverType, A0, A1, A2, ExceptionType extends Throwable> MethodInformation.WithArguments<ReceiverType, ReturnInformation.None, ArgumentInformation.Arg3<A0, A1, A2>> method(MethodCallable.ForArg3<ReceiverType, A0, A1, A2, ExceptionType> callable) {
		return new ByProxyMethodInformation<>(new InvokeMethodCallableConsumer<>(callable));
	}

	@SuppressWarnings("overloads")
	static <ReceiverType, ReturnType, ExceptionType extends Throwable> MethodInformation<ReceiverType, ReturnInformation.Return<ReturnType>> method(MethodCallable.WithReturn.ForArg0<ReceiverType, ExceptionType, ReturnType> callable) {
		return new ByProxyMethodInformation<>(new InvokeMethodCallableConsumer<>(callable));
	}

	@SuppressWarnings("overloads")
	static <ReceiverType, ReturnType, A0, ExceptionType extends Throwable> MethodInformation.WithArguments<ReceiverType, ReturnInformation.Return<ReturnType>, ArgumentInformation.Arg1<A0>> method(MethodCallable.WithReturn.ForArg1<ReceiverType, ReturnType, A0, ExceptionType> callable) {
		return new ByProxyMethodInformation<>(new InvokeMethodCallableConsumer<>(callable));
	}

	@SuppressWarnings("overloads")
	static <ReceiverType, ReturnType, A0, A1, ExceptionType extends Throwable> MethodInformation.WithArguments<ReceiverType, ReturnInformation.Return<ReturnType>, ArgumentInformation.Arg2<A0, A1>> method(MethodCallable.WithReturn.ForArg2<ReceiverType, ReturnType, A0, A1, ExceptionType> callable) {
		return new ByProxyMethodInformation<>(new InvokeMethodCallableConsumer<>(callable));
	}

	@SuppressWarnings("overloads")
	static <ReceiverType, ReturnType, A0, A1, A2, ExceptionType extends Throwable> MethodInformation.WithArguments<ReceiverType, ReturnInformation.Return<ReturnType>, ArgumentInformation.Arg3<A0, A1, A2>> method(MethodCallable.WithReturn.ForArg3<ReceiverType, ReturnType, A0, A1, A2, ExceptionType> callable) {
		return new ByProxyMethodInformation<>(new InvokeMethodCallableConsumer<>(callable));
	}

	static <ReceiverType, ReturnInfoType extends ReturnInformation> MethodInformation<ReceiverType, ReturnInfoType> method(String name) {
		return new ByNameMethodInformation<>(name);
	}
}
