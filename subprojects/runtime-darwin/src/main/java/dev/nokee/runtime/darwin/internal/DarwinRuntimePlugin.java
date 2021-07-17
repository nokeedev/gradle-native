package dev.nokee.runtime.darwin.internal;

import dev.nokee.runtime.base.internal.UnzipTransform;
import dev.nokee.runtime.nativebase.internal.NativeRuntimePlugin;
import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.transform.TransformParameters;
import org.gradle.api.artifacts.transform.TransformSpec;
import org.gradle.api.attributes.AttributesSchema;
import org.gradle.api.attributes.LibraryElements;
import org.gradle.api.model.ObjectFactory;

import javax.inject.Inject;

import static dev.nokee.runtime.darwin.internal.DarwinRuntimePlugin.FrameworkArchiveToFramework.unzipFrameworkArtifact;
import static dev.nokee.runtime.nativebase.internal.ArtifactCompressionState.*;
import static dev.nokee.utils.ConfigurationUtils.ARTIFACT_TYPE_ATTRIBUTE;
import static org.gradle.api.attributes.LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE;

public /*final*/ abstract class DarwinRuntimePlugin implements Plugin<Project> {
	@Inject
	protected abstract ObjectFactory getObjects();

	@Override
	public void apply(Project project) {
		project.getPluginManager().apply(NativeRuntimePlugin.class);
		project.getDependencies().attributesSchema(this::configureAttributesSchema);
		project.getDependencies().registerTransform(FrameworkArchiveToFramework.class, unzipFrameworkArtifact(getObjects()));
	}

	private void configureAttributesSchema(AttributesSchema schema) {
		schema.attribute(LIBRARY_ELEMENTS_ATTRIBUTE, new FrameworkElementAttributeSchema(getObjects()));
	}

	public static /*final*/ abstract class FrameworkArchiveToFramework extends UnzipTransform {
		public static Action<TransformSpec<TransformParameters.None>> unzipFrameworkArtifact(ObjectFactory objects) {
			return spec -> {
				spec.getFrom()
					.attribute(LIBRARY_ELEMENTS_ATTRIBUTE, objects.named(LibraryElements.class, DarwinLibraryElements.FRAMEWORK_BUNDLE))
					.attribute(ARTIFACT_TYPE_ATTRIBUTE, DarwinArtifactTypes.FRAMEWORK_ZIP)
					.attribute(ARTIFACT_COMPRESSION_STATE_ATTRIBUTE, COMPRESSED);
				spec.getTo()
					.attribute(LIBRARY_ELEMENTS_ATTRIBUTE, objects.named(LibraryElements.class, DarwinLibraryElements.FRAMEWORK_BUNDLE))
					.attribute(ARTIFACT_TYPE_ATTRIBUTE, DarwinArtifactTypes.FRAMEWORK_TYPE)
					.attribute(ARTIFACT_COMPRESSION_STATE_ATTRIBUTE, UNCOMPRESSED);
			};
		}
	}
}
