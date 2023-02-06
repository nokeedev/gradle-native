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

/**
 * Note: Gentlemen agreement when implementing any of the following interface is to avoid any conditional calls.
 *
 * @param <ReceiverType>
 * @param <ReturnType>
 * @param <ExceptionType>
 */
public interface MethodCallable<ReceiverType, ReturnType, ExceptionType extends Throwable> extends Invokable<ReceiverType, ReturnType, ExceptionType> {
	@FunctionalInterface
	interface ForArg0<ReceiverType, ExceptionType extends Throwable> extends MethodCallable<ReceiverType, Void, ExceptionType> {
		@Override
		default Void invoke(ReceiverType self, Arguments args) throws ExceptionType {
			call(self);
			return null;
		}

		void call(ReceiverType self) throws ExceptionType;
	}

	@FunctionalInterface
	interface ForArg1<ReceiverType, A0, ExceptionType extends Throwable> extends MethodCallable<ReceiverType, Void, ExceptionType> {
		@Override
		default Void invoke(ReceiverType self, Arguments args) throws ExceptionType {
			call(self, args.getArgument(0));
			return null;
		}

		void call(ReceiverType self, A0 a0) throws ExceptionType;
	}

	@FunctionalInterface
	interface ForArg2<ReceiverType, A0, A1, ExceptionType extends Throwable> extends MethodCallable<ReceiverType, Void, ExceptionType> {
		@Override
		default Void invoke(ReceiverType self, Arguments args) throws ExceptionType {
			call(self, args.getArgument(0), args.getArgument(1));
			return null;
		}

		void call(ReceiverType self, A0 a0, A1 a1) throws ExceptionType;
	}

	@FunctionalInterface
	interface ForArg3<ReceiverType, A0, A1, A2, ExceptionType extends Throwable> extends MethodCallable<ReceiverType, Void, ExceptionType> {
		@Override
		default Void invoke(ReceiverType self, Arguments args) throws ExceptionType {
			call(self, args.getArgument(0), args.getArgument(1), args.getArgument(2));
			return null;
		}

		void call(ReceiverType self, A0 a0, A1 a1, A2 a2) throws ExceptionType;
	}

	interface WithReturn {
		@FunctionalInterface
		interface ForArg0<ReceiverType, ExceptionType extends Throwable, ReturnType> extends MethodCallable<ReceiverType, ReturnType, ExceptionType> {
			@Override
			default ReturnType invoke(ReceiverType self, Arguments args) throws ExceptionType {
				return call(self);
			}

			ReturnType call(ReceiverType self) throws ExceptionType;
		}

		@FunctionalInterface
		interface ForArg1<ReceiverType, ReturnType, A0, ExceptionType extends Throwable> extends MethodCallable<ReceiverType, ReturnType, ExceptionType> {
			@Override
			default ReturnType invoke(ReceiverType self, Arguments args) throws ExceptionType {
				return call(self, args.getArgument(0));
			}

			ReturnType call(ReceiverType self, A0 a0) throws ExceptionType;
		}

		@FunctionalInterface
		interface ForArg2<ReceiverType, ReturnType, A0, A1, ExceptionType extends Throwable> extends MethodCallable<ReceiverType, ReturnType, ExceptionType> {
			@Override
			default ReturnType invoke(ReceiverType self, Arguments args) throws ExceptionType {
				return call(self, args.getArgument(0), args.getArgument(1));
			}

			ReturnType call(ReceiverType self, A0 a0, A1 a1) throws ExceptionType;
		}

		@FunctionalInterface
		interface ForArg3<ReceiverType, ReturnType, A0, A1, A2, ExceptionType extends Throwable> extends MethodCallable<ReceiverType, ReturnType, ExceptionType> {
			@Override
			default ReturnType invoke(ReceiverType self, Arguments args) throws ExceptionType {
				return call(self, args.getArgument(0), args.getArgument(1), args.getArgument(2));
			}

			ReturnType call(ReceiverType self, A0 a0, A1 a1, A2 a2) throws ExceptionType;
		}
	}
}
