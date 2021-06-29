package dev.nokee.runtime.nativebase.internal;

import dev.nokee.runtime.base.internal.IdentityTransform;
import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.transform.TransformParameters;
import org.gradle.api.artifacts.transform.TransformSpec;
import org.gradle.api.artifacts.type.ArtifactTypeDefinition;
import org.gradle.api.attributes.AttributesSchema;
import org.gradle.api.attributes.Category;
import org.gradle.api.attributes.LibraryElements;
import org.gradle.api.attributes.Usage;
import org.gradle.api.internal.artifacts.transform.UnzipTransform;
import org.gradle.api.model.ObjectFactory;

import javax.inject.Inject;

import static dev.nokee.runtime.nativebase.BinaryLinkage.BINARY_LINKAGE_ATTRIBUTE;
import static dev.nokee.runtime.nativebase.BuildType.BUILD_TYPE_ATTRIBUTE;
import static dev.nokee.runtime.nativebase.internal.NativeRuntimePlugin.DirectoryToHeaderSearchPath.directoryToHeaderSearchPath;
import static dev.nokee.utils.ConfigurationUtils.ARTIFACT_TYPE_ATTRIBUTE;
import static org.gradle.api.attributes.Category.CATEGORY_ATTRIBUTE;
import static org.gradle.api.attributes.LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE;
import static org.gradle.api.attributes.Usage.USAGE_ATTRIBUTE;

public /*final*/ abstract class NativeRuntimePlugin implements Plugin<Project> {
	@Inject
	protected abstract ObjectFactory getObjects();

	@Override
	public void apply(Project project) {
		project.getPluginManager().apply(NativeRuntimeBasePlugin.class);
		project.getDependencies().attributesSchema(this::configureAttributesSchema);
		project.getDependencies().registerTransform(UnzipTransform.class, this::unzipHeadersArtifactToSearchPaths);
		project.getDependencies().registerTransform(DirectoryToHeaderSearchPath.class, directoryToHeaderSearchPath(getObjects()));

		project.getDependencies().artifactTypes(it -> {
			it.create("so", this::nixSharedLibraryAttributes);
			it.create("dylib", this::nixSharedLibraryAttributes);
			it.create("dll", this::windowsSharedLibraryAttributes);
			it.create("a", this::staticLibraryAttributes);

			// Technically, there should be a differentiation between static and import library.
			// However, both have the same extension and usage so declaring them as static library is good enough.
			it.create("lib", this::staticLibraryAttributes);
		});
	}

	private void nixSharedLibraryAttributes(ArtifactTypeDefinition artifactType) {
		artifactType.getAttributes()
			.attribute(USAGE_ATTRIBUTE, getObjects().named(Usage.class, Usage.NATIVE_LINK + "+" + Usage.NATIVE_RUNTIME))
			.attribute(CATEGORY_ATTRIBUTE, getObjects().named(Category.class, Category.LIBRARY))
			.attribute(LIBRARY_ELEMENTS_ATTRIBUTE, getObjects().named(LibraryElements.class, LibraryElements.DYNAMIC_LIB));
	}

	private void windowsSharedLibraryAttributes(ArtifactTypeDefinition artifactType) {
		artifactType.getAttributes()
			.attribute(USAGE_ATTRIBUTE, getObjects().named(Usage.class, Usage.NATIVE_RUNTIME))
			.attribute(CATEGORY_ATTRIBUTE, getObjects().named(Category.class, Category.LIBRARY))
			.attribute(LIBRARY_ELEMENTS_ATTRIBUTE, getObjects().named(LibraryElements.class, LibraryElements.DYNAMIC_LIB));
	}

	private void staticLibraryAttributes(ArtifactTypeDefinition artifactType) {
		artifactType.getAttributes()
			.attribute(USAGE_ATTRIBUTE, getObjects().named(Usage.class, Usage.NATIVE_LINK))
			.attribute(CATEGORY_ATTRIBUTE, getObjects().named(Category.class, Category.LIBRARY))
			.attribute(LIBRARY_ELEMENTS_ATTRIBUTE, getObjects().named(LibraryElements.class, LibraryElements.LINK_ARCHIVE));
	}

	private void configureAttributesSchema(AttributesSchema schema) {
		schema.attribute(BINARY_LINKAGE_ATTRIBUTE, new BinaryLinkageAttributeSchema());
		schema.attribute(BUILD_TYPE_ATTRIBUTE, new BuildTypeAttributeSchema());
		schema.attribute(USAGE_ATTRIBUTE, new UsageAttributeSchema());
	}

	public static /*final*/ abstract class DirectoryToHeaderSearchPath extends IdentityTransform {
		public static Action<TransformSpec<TransformParameters.None>> directoryToHeaderSearchPath(ObjectFactory objects) {
			return spec -> {
				spec.getFrom()
					.attribute(USAGE_ATTRIBUTE, objects.named(Usage.class, Usage.C_PLUS_PLUS_API))
					.attribute(LIBRARY_ELEMENTS_ATTRIBUTE, objects.named(LibraryElements.class, LibraryElements.HEADERS_CPLUSPLUS))
					.attribute(ARTIFACT_TYPE_ATTRIBUTE, ArtifactTypeDefinition.DIRECTORY_TYPE);
				spec.getTo()
					.attribute(USAGE_ATTRIBUTE, objects.named(Usage.class, Usage.C_PLUS_PLUS_API))
					.attribute(LIBRARY_ELEMENTS_ATTRIBUTE, objects.named(LibraryElements.class, LibraryElements.HEADERS_CPLUSPLUS))
					.attribute(ARTIFACT_TYPE_ATTRIBUTE, NativeArtifactTypes.NATIVE_HEADERS_DIRECTORY);
			};
		}
	}

	private void unzipHeadersArtifactToSearchPaths(TransformSpec<TransformParameters.None> spec) {
		spec.getFrom()
			.attribute(USAGE_ATTRIBUTE, getObjects().named(Usage.class, Usage.C_PLUS_PLUS_API))
			.attribute(LIBRARY_ELEMENTS_ATTRIBUTE, getObjects().named(LibraryElements.class, LibraryElements.HEADERS_CPLUSPLUS))
			.attribute(ARTIFACT_TYPE_ATTRIBUTE, ArtifactTypeDefinition.ZIP_TYPE);
		spec.getTo()
			.attribute(USAGE_ATTRIBUTE, getObjects().named(Usage.class, Usage.C_PLUS_PLUS_API))
			.attribute(LIBRARY_ELEMENTS_ATTRIBUTE, getObjects().named(LibraryElements.class, LibraryElements.HEADERS_CPLUSPLUS))
			.attribute(ARTIFACT_TYPE_ATTRIBUTE, NativeArtifactTypes.NATIVE_HEADERS_DIRECTORY);
	}
}
