package dev.nokee.language.base.internal

import dev.nokee.language.base.LanguageSourceSet
import dev.nokee.model.DomainObjectIdentifier
import dev.nokee.model.internal.*
import groovy.transform.EqualsAndHashCode
import org.apache.commons.lang3.RandomStringUtils
import org.gradle.testfixtures.ProjectBuilder

trait LanguageSourceSetFixture {
	RealizableDomainObjectRepository<LanguageSourceSet> newEntityRepository() {
		def realizer = new RealizableDomainObjectRealizerImpl(eventPublisher)
		return new LanguageSourceSetRepository(eventPublisher, realizer, providerFactory)
	}

    DomainObjectConfigurer<LanguageSourceSet> newEntityConfigurer() {
		return new LanguageSourceSetConfigurer(eventPublisher)
	}

    KnownDomainObjectFactory<LanguageSourceSet> newEntityFactory() {
		return new KnownLanguageSourceSetFactory({ entityRepository }, { entityConfigurer })
	}

    DomainObjectViewFactory<LanguageSourceSet> newEntityViewFactory() {
		return new LanguageSourceSetViewFactory(entityRepository, entityConfigurer, newEntityFactory())
	}

	DomainObjectProviderFactory<LanguageSourceSet> newEntityProviderFactory() {
		throw new UnsupportedOperationException()
	}

	Class<LanguageSourceSet> getEntityType() {
		return LanguageSourceSet
	}

	Class<? extends LanguageSourceSet> getEntityImplementationType() {
		return LanguageSourceSetImpl
	}

	def <S extends LanguageSourceSet> LanguageSourceSetIdentifier<S> entityIdentifier(Class<S> type, DomainObjectIdentifier owner) {
		return LanguageSourceSetIdentifier.of(LanguageSourceSetName.of('a' + RandomStringUtils.randomAlphanumeric(12)), type, owner)
	}

    DomainObjectIdentifier ownerIdentifier(String name) {
		return new MyOwnerIdentifier(name)
	}

	@EqualsAndHashCode(includeFields = true)
	static class MyOwnerIdentifier implements DomainObjectIdentifierInternal {
		private final String name

		MyOwnerIdentifier(String name) {
			this.name = name
		}

		String getName() {
			return name
		}

		@Override
		Optional<? extends DomainObjectIdentifierInternal> getParentIdentifier() {
			Optional.empty()
		}

		@Override
		String getDisplayName() {
			throw new UnsupportedOperationException()
		}
	}

	Class<? extends LanguageSourceSet> getMyEntityType() {
		return MyLanguageSourceSet
	}

	static class MyLanguageSourceSet extends AbstractLanguageSourceSet<MyLanguageSourceSet> {
		MyLanguageSourceSet() {
			super(LanguageSourceSetIdentifier.of(LanguageSourceSetName.of('a' + RandomStringUtils.randomAlphanumeric(12)), MyLanguageSourceSet, new MyOwnerIdentifier()), MyLanguageSourceSet, ProjectBuilder.builder().build().objects)
		}
	}

	Class<? extends LanguageSourceSet> getMyEntityChildType() {
		return MyLanguageSourceSetChild
	}

	static class MyLanguageSourceSetChild extends MyLanguageSourceSet {}

	static class LanguageSourceSetImpl extends AbstractLanguageSourceSet<LanguageSourceSetImpl> {
		LanguageSourceSetImpl() {
			super(LanguageSourceSetIdentifier.of(LanguageSourceSetName.of('a' + RandomStringUtils.randomAlphanumeric(12)), LanguageSourceSetImpl, new MyOwnerIdentifier()), LanguageSourceSetImpl, ProjectBuilder.builder().build().objects)
		}
	}
}
