package dev.nokee.platform.nativebase.internal.rules;

import dev.nokee.model.internal.ProjectIdentifier;
import dev.nokee.platform.base.Component;
import dev.nokee.platform.base.Variant;
import dev.nokee.platform.base.internal.ComponentIdentifier;
import dev.nokee.platform.base.internal.VariantIdentifier;
import dev.nokee.platform.base.internal.variants.KnownVariant;
import dev.nokee.platform.base.internal.variants.KnownVariantFactory;
import dev.nokee.platform.base.internal.variants.VariantConfigurer;
import dev.nokee.platform.base.internal.variants.VariantRepository;
import org.gradle.api.Project;

import java.util.Arrays;

public interface VariantEntityFixture extends NokeeEntitiesFixture {
	Project getProject();

	default VariantConfigurer getVariantConfigurer() {
		return new VariantConfigurer(getEventPublisher());
	}

	default VariantRepository getVariantRepository() {
		return new VariantRepository(getEventPublisher(), getEntityRealizer(), getProject().getProviders());
	}

	default KnownVariantFactory getKnownVariantFactory() {
		return new KnownVariantFactory(this::getVariantRepository, this::getVariantConfigurer);
	}

	static VariantIdentifier<Variant> onlyVariantOfMainComponent() {
		return onlyVariantOfMainComponent(ProjectIdentifier.of("root"));
	}

	static VariantIdentifier<Variant> onlyVariantOfMainComponent(ProjectIdentifier projectOwner) {
		VariantIdentifier.Builder<Variant> builder = VariantIdentifier.builder().withType(Variant.class).withComponentIdentifier(ComponentIdentifier.ofMain(Component.class, projectOwner));
		return builder.build();
	}

	static VariantIdentifier<Variant> aVariantOfMainComponent(String... ambiguousDimensionName) {
		return aVariantOfMainComponent(ProjectIdentifier.of("root"), ambiguousDimensionName);
	}

	static VariantIdentifier<Variant> aVariantOfMainComponent(ProjectIdentifier projectOwner, String... ambiguousDimensionName) {
		VariantIdentifier.Builder<Variant> builder = VariantIdentifier.builder().withType(Variant.class).withComponentIdentifier(ComponentIdentifier.ofMain(Component.class, projectOwner));
		for (String it : ambiguousDimensionName) {
			builder.withVariantDimension(it, Arrays.asList(it, "dummy"));
		}
		return builder.build();
	}

	default <S extends Variant> KnownVariant<S> known(VariantIdentifier<S> identifier) {
		return getKnownVariantFactory().create(identifier);
	}
}
