package dev.nokee.runtime.darwin.internal;

import dev.nokee.runtime.base.internal.IdentityTransform;
import dev.nokee.runtime.nativebase.internal.NativeArtifactTypes;
import dev.nokee.runtime.nativebase.internal.NativeRuntimePlugin;
import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.transform.TransformParameters;
import org.gradle.api.artifacts.transform.TransformSpec;
import org.gradle.api.artifacts.type.ArtifactTypeDefinition;
import org.gradle.api.attributes.AttributesSchema;
import org.gradle.api.attributes.LibraryElements;
import org.gradle.api.attributes.Usage;
import org.gradle.api.internal.artifacts.transform.UnzipTransform;
import org.gradle.api.model.ObjectFactory;

import javax.inject.Inject;

import static dev.nokee.runtime.darwin.internal.DarwinRuntimePlugin.DirectoryToFramework.directoryToFramework;
import static dev.nokee.runtime.darwin.internal.DarwinRuntimePlugin.FrameworkToCompilerReady.frameworkToCompilerReady;
import static dev.nokee.runtime.darwin.internal.DarwinRuntimePlugin.FrameworkToLinkerReady.frameworkToLinkerReady;
import static dev.nokee.runtime.darwin.internal.DarwinRuntimePlugin.HeaderSearchPathToCompilerReady.headerSearchPathToCompilerReady;
import static dev.nokee.utils.ConfigurationUtils.ARTIFACT_TYPE_ATTRIBUTE;
import static org.gradle.api.attributes.LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE;
import static org.gradle.api.attributes.Usage.*;

public /*final*/ abstract class DarwinRuntimePlugin implements Plugin<Project> {
	@Inject
	protected abstract ObjectFactory getObjects();

	@Override
	public void apply(Project project) {
		project.getPluginManager().apply(NativeRuntimePlugin.class);
		project.getDependencies().attributesSchema(this::configureAttributesSchema);
		project.getDependencies().registerTransform(UnzipTransform.class, this::unzipFrameworkArtifact);
		project.getDependencies().registerTransform(DirectoryToFramework.class, directoryToFramework(getObjects()));
		project.getDependencies().registerTransform(FrameworkToCompilerReady.class, frameworkToCompilerReady(getObjects()));
		project.getDependencies().registerTransform(HeaderSearchPathToCompilerReady.class, headerSearchPathToCompilerReady(getObjects()));
		project.getDependencies().registerTransform(FrameworkToLinkerReady.class, frameworkToLinkerReady(getObjects()));
	}

	private void configureAttributesSchema(AttributesSchema schema) {
		schema.attribute(LIBRARY_ELEMENTS_ATTRIBUTE, new FrameworkElementAttributeSchema(getObjects()));
	}

	public static /*final*/ abstract class FrameworkToCompilerReady extends IdentityTransform {
		public static Action<TransformSpec<TransformParameters.None>> frameworkToCompilerReady(ObjectFactory objects) {
			return spec -> {
				spec.getFrom()
					.attribute(USAGE_ATTRIBUTE, objects.named(Usage.class, C_PLUS_PLUS_API))
					.attribute(LIBRARY_ELEMENTS_ATTRIBUTE, objects.named(LibraryElements.class, DarwinLibraryElements.FRAMEWORK_BUNDLE))
					.attribute(ARTIFACT_TYPE_ATTRIBUTE, DarwinArtifactTypes.FRAMEWORK_TYPE);
				spec.getTo()
					.attribute(USAGE_ATTRIBUTE, objects.named(Usage.class, C_PLUS_PLUS_API))
					.attribute(LIBRARY_ELEMENTS_ATTRIBUTE, objects.named(LibraryElements.class, DarwinLibraryElements.FRAMEWORK_BUNDLE))
					.attribute(ARTIFACT_TYPE_ATTRIBUTE, DarwinArtifactTypes.NATIVE_HEADERS_DIRECTORY_OR_FRAMEWORK_TYPE);
			};
		}
	}

	public static /*final*/ abstract class FrameworkToLinkerReady extends IdentityTransform {
		public static Action<TransformSpec<TransformParameters.None>> frameworkToLinkerReady(ObjectFactory objects) {
			return spec -> {
				spec.getFrom()
					.attribute(USAGE_ATTRIBUTE, objects.named(Usage.class, Usage.NATIVE_LINK))
					.attribute(LIBRARY_ELEMENTS_ATTRIBUTE, objects.named(LibraryElements.class, DarwinLibraryElements.FRAMEWORK_BUNDLE))
					.attribute(ARTIFACT_TYPE_ATTRIBUTE, DarwinArtifactTypes.FRAMEWORK_TYPE);
				spec.getTo()
					.attribute(USAGE_ATTRIBUTE, objects.named(Usage.class, Usage.NATIVE_LINK))
					.attribute(LIBRARY_ELEMENTS_ATTRIBUTE, objects.named(LibraryElements.class, DarwinLibraryElements.FRAMEWORK_BUNDLE))
					.attribute(ARTIFACT_TYPE_ATTRIBUTE, DarwinArtifactTypes.LINKABLE_ELEMENT_OR_FRAMEWORK_TYPE);
			};
		}
	}

	public static /*final*/ abstract class DirectoryToFramework extends IdentityTransform {
		public static Action<TransformSpec<TransformParameters.None>> directoryToFramework(ObjectFactory objects) {
			return spec -> {
				spec.getFrom()
					.attribute(USAGE_ATTRIBUTE, objects.named(Usage.class, C_PLUS_PLUS_API))
					.attribute(LIBRARY_ELEMENTS_ATTRIBUTE, objects.named(LibraryElements.class, DarwinLibraryElements.FRAMEWORK_BUNDLE))
					.attribute(ARTIFACT_TYPE_ATTRIBUTE, ArtifactTypeDefinition.DIRECTORY_TYPE);
				spec.getTo()
					.attribute(USAGE_ATTRIBUTE, objects.named(Usage.class, C_PLUS_PLUS_API))
					.attribute(LIBRARY_ELEMENTS_ATTRIBUTE, objects.named(LibraryElements.class, DarwinLibraryElements.FRAMEWORK_BUNDLE))
					.attribute(ARTIFACT_TYPE_ATTRIBUTE, DarwinArtifactTypes.FRAMEWORK_TYPE);
			};
		}
	}

	public static /*final*/ abstract class HeaderSearchPathToCompilerReady extends IdentityTransform {
		public static Action<TransformSpec<TransformParameters.None>> headerSearchPathToCompilerReady(ObjectFactory objects) {
			return spec -> {
				spec.getFrom()
					.attribute(USAGE_ATTRIBUTE, objects.named(Usage.class, C_PLUS_PLUS_API))
					.attribute(LIBRARY_ELEMENTS_ATTRIBUTE, objects.named(LibraryElements.class, LibraryElements.HEADERS_CPLUSPLUS))
					.attribute(ARTIFACT_TYPE_ATTRIBUTE, NativeArtifactTypes.NATIVE_HEADERS_DIRECTORY);
				spec.getTo()
					.attribute(USAGE_ATTRIBUTE, objects.named(Usage.class, C_PLUS_PLUS_API))
					.attribute(LIBRARY_ELEMENTS_ATTRIBUTE, objects.named(LibraryElements.class, LibraryElements.HEADERS_CPLUSPLUS))
					.attribute(ARTIFACT_TYPE_ATTRIBUTE, DarwinArtifactTypes.NATIVE_HEADERS_DIRECTORY_OR_FRAMEWORK_TYPE);
			};
		}
	}

	private void unzipFrameworkArtifact(TransformSpec<TransformParameters.None> spec) {
		spec.getFrom()
			.attribute(LIBRARY_ELEMENTS_ATTRIBUTE, getObjects().named(LibraryElements.class, DarwinLibraryElements.FRAMEWORK_BUNDLE))
			.attribute(ARTIFACT_TYPE_ATTRIBUTE, ArtifactTypeDefinition.ZIP_TYPE);
		spec.getTo()
			.attribute(LIBRARY_ELEMENTS_ATTRIBUTE, getObjects().named(LibraryElements.class, DarwinLibraryElements.FRAMEWORK_BUNDLE))
			.attribute(ARTIFACT_TYPE_ATTRIBUTE, DarwinArtifactTypes.FRAMEWORK_TYPE);
	}
}
