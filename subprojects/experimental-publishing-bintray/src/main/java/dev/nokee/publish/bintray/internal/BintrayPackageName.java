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
package dev.nokee.publish.bintray.internal;

import com.google.common.base.Suppliers;
import lombok.EqualsAndHashCode;
import lombok.val;
import org.gradle.api.artifacts.repositories.MavenArtifactRepository;
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.api.tasks.Input;

import java.util.Optional;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

@EqualsAndHashCode
public final class BintrayPackageName {
	private final Supplier<String> packageNameSupplier;

	private BintrayPackageName(Supplier<String> packageNameSupplier) {
		this.packageNameSupplier = packageNameSupplier;
	}

	public static BintrayPackageName fromRepositoryDeclaration(MavenArtifactRepository repository) {
		requireNonNull(repository);
		return new BintrayPackageName(() -> {
			val repositoryExt = ((ExtensionAware) repository).getExtensions().getExtraProperties();
			if (repositoryExt.has("packageName")) {
				return Optional.ofNullable(repositoryExt.get("packageName"))
					.map(Object::toString)
					.orElse(null);
			}
			return null;
		});
	}

	public static BintrayPackageName of(String name) {
		return new BintrayPackageName(Suppliers.ofInstance(requireNonNull(name)));
	}

	@Input
	public String get() {
		val packageName = packageNameSupplier.get();
		if (packageName == null) {
			throw new IllegalStateException("When publishing to Bintray repositories, please specify the package name using an extra property on the repository, e.g. ext.packageName = 'foo'.");
		}
		return packageName;
	}
}
