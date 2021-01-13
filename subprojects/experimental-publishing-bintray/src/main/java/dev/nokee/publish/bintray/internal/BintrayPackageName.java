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
