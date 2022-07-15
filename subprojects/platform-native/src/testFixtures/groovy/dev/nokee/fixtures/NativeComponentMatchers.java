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
package dev.nokee.fixtures;

import dev.nokee.platform.base.BinaryAwareComponent;
import dev.nokee.platform.base.Component;
import dev.nokee.platform.base.HasBaseName;
import dev.nokee.platform.nativebase.internal.BaseNativeBinary;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

import java.util.stream.Collectors;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.everyItem;

public class NativeComponentMatchers {
	// TODO: Generalize this matcher when base name is better supported by every binaries/variants
	public static Matcher<Component> hasArtifactBaseNameOf(String name) {
		return new FeatureMatcher<Component, Iterable<String>>(everyItem(equalTo(name)), "has artifact base name of", "base name of") {
			@Override
			protected Iterable<String> featureValueOf(Component actual) {
				return ((BinaryAwareComponent) actual).getBinaries().filter(BaseNativeBinary.class::isInstance).get().stream().map(it -> ((HasBaseName) it).getBaseName().get()).collect(Collectors.toList());
			}
		};
	}
}
