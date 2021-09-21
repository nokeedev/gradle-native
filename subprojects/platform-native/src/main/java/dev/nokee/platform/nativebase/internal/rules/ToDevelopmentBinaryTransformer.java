/*
 * Copyright 2020 the original author or authors.
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
package dev.nokee.platform.nativebase.internal.rules;

import dev.nokee.platform.base.Binary;
import dev.nokee.platform.base.Variant;
import org.gradle.api.Transformer;
import org.gradle.api.provider.Provider;

enum ToDevelopmentBinaryTransformer implements Transformer<Provider<Binary>, Variant> {
	TO_DEVELOPMENT_BINARY;

	@Override
	public Provider<Binary> transform(Variant variant) {
		return variant.getDevelopmentBinary();
	}
}
