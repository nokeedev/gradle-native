/*
 * Copyright 2021 the original author or authors.
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
package dev.nokee.utils;

import lombok.EqualsAndHashCode;
import lombok.val;

import java.util.Arrays;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

@EqualsAndHashCode
final class StringFormatSupplier implements Supplier<String> {
	private final String format;
	private final Object[] args;

	StringFormatSupplier(String format, Object[] args) {
		this.format = requireNonNull(format);
		this.args = requireNonNull(args);
	}

	public Object[] getUnpackedArgs() {
		return Arrays.stream(args).map(DeferredUtils::unpack).toArray();
	}

	@Override
	public String get() {
		return String.format(format, getUnpackedArgs());
	}

	@Override
	public String toString() {
		val builder = new StringBuilder();
		builder.append("String.format(").append(format);
		val args = getUnpackedArgs();
		if (args.length > 0) {
			for (Object arg : args) {
				builder.append(", ").append(arg);
			}
		}
		builder.append(")");
		return builder.toString();
	}
}
