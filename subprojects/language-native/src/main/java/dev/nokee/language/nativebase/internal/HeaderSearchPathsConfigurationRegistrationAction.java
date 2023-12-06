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
package dev.nokee.language.nativebase.internal;

import dev.nokee.utils.ProviderUtils;
import lombok.val;
import org.gradle.api.Action;
import org.gradle.api.Transformer;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ResolvableDependencies;
import org.gradle.api.attributes.Usage;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Provider;

import java.nio.file.Path;
import java.util.Set;

import static dev.nokee.language.nativebase.internal.FrameworkAwareIncomingArtifacts.frameworks;
import static dev.nokee.utils.ConfigurationUtils.configureAttributes;
import static dev.nokee.utils.TransformerUtils.toSetTransformer;
import static dev.nokee.utils.TransformerUtils.transformEach;

public final class HeaderSearchPathsConfigurationRegistrationAction<T> implements Action<T> {
	private final ObjectFactory objects;

	public HeaderSearchPathsConfigurationRegistrationAction(ObjectFactory objects) {
		this.objects = objects;
	}

	@Override
	public void execute(T t) {
		if (t instanceof HasHeaderSearchPaths) {
			final Configuration headerSearchPaths = ((HasHeaderSearchPaths) t).getHeaderSearchPaths().getAsConfiguration();

			forCPlusPlusApiUsage().execute(headerSearchPaths);
			val incomingArtifacts = FrameworkAwareIncomingArtifacts.from(incomingArtifactsOf(headerSearchPaths));
			((HasHeaderSearchPaths) t).getDependentFrameworkSearchPaths().from(incomingArtifacts.getAs(frameworks()).map(parentFiles()));
			((HasHeaderSearchPaths) t).getDependentHeaderSearchPaths().from(incomingArtifacts.getAs(frameworks().negate()));
		}
	}

	private Action<Configuration> forCPlusPlusApiUsage() {
		return configureAttributes(builder -> builder.usage(objects.named(Usage.class, Usage.C_PLUS_PLUS_API)));
	}

	private static Transformer<Set<Path>, Iterable<? extends Path>> parentFiles() {
		return transformEach(Path::getParent).andThen(toSetTransformer(Path.class));
	}

	private Provider<ResolvableDependencies> incomingArtifactsOf(Configuration config) {
		return ProviderUtils.fixed(config.getIncoming());
	}
}
