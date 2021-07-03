package dev.nokee.runtime.nativebase.internal;

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
import static dev.nokee.runtime.nativebase.internal.ArtifactCompressionState.*;
import static dev.nokee.runtime.nativebase.internal.NativeArtifactTypes.*;
import static dev.nokee.runtime.nativebase.internal.NativeRuntimePlugin.HeadersArchiveToHeaderSearchPath.unzipHeadersArtifactToSearchPath;
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
		project.getDependencies().registerTransform(UnzipTransform.class, this::unzipArtifactToDirectory);
		project.getDependencies().registerTransform(HeadersArchiveToHeaderSearchPath.class, unzipHeadersArtifactToSearchPath(getObjects()));

		project.getDependencies().artifactTypes(ArtifactCompressionState::configureArtifactsCompressionState);

		project.getDependencies().artifactTypes(it -> {
			it.create(SHARED_OBJECTS_LIBRARY, this::nixSharedLibraryAttributes);
			it.create(MACH_OBJECT_DYNAMIC_LIBRARY, this::nixSharedLibraryAttributes);
			it.create(DYNAMIC_LINK_LIBRARY, this::windowsSharedLibraryAttributes);
			it.create(STATIC_LIBRARY_ARCHIVE, this::staticLibraryAttributes);

			// Technically, there should be a differentiation between static and import library.
			// However, both have the same extension and usage so declaring them as static library is good enough.
			it.create(STATIC_OR_IMPORT_LIBRARY, this::staticLibraryAttributes);
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

	public static /*final*/ abstract class HeadersArchiveToHeaderSearchPath implements UnzipTransform {
		public static Action<TransformSpec<TransformParameters.None>> unzipHeadersArtifactToSearchPath(ObjectFactory objects) {
			return spec -> {
				spec.getFrom()
					.attribute(USAGE_ATTRIBUTE, objects.named(Usage.class, Usage.C_PLUS_PLUS_API))
					.attribute(LIBRARY_ELEMENTS_ATTRIBUTE, objects.named(LibraryElements.class, LibraryElements.HEADERS_CPLUSPLUS))
					.attribute(ARTIFACT_TYPE_ATTRIBUTE, NativeArtifactTypes.NATIVE_HEADERS_ZIP)
					.attribute(ARTIFACT_COMPRESSION_STATE_ATTRIBUTE, COMPRESSED);
				spec.getTo()
					.attribute(USAGE_ATTRIBUTE, objects.named(Usage.class, Usage.C_PLUS_PLUS_API))
					.attribute(LIBRARY_ELEMENTS_ATTRIBUTE, objects.named(LibraryElements.class, LibraryElements.HEADERS_CPLUSPLUS))
					.attribute(ARTIFACT_TYPE_ATTRIBUTE, NativeArtifactTypes.NATIVE_HEADERS_DIRECTORY)
					.attribute(ARTIFACT_COMPRESSION_STATE_ATTRIBUTE, UNCOMPRESSED);
			};
		}
	}

	private void unzipArtifactToDirectory(TransformSpec<TransformParameters.None> spec) {
		spec.getFrom()
			.attribute(ARTIFACT_TYPE_ATTRIBUTE, ArtifactTypeDefinition.ZIP_TYPE)
			.attribute(ARTIFACT_COMPRESSION_STATE_ATTRIBUTE, COMPRESSED);
		spec.getTo()
			.attribute(ARTIFACT_TYPE_ATTRIBUTE, ArtifactTypeDefinition.DIRECTORY_TYPE)
			.attribute(ARTIFACT_COMPRESSION_STATE_ATTRIBUTE, UNCOMPRESSED);
	}
}
