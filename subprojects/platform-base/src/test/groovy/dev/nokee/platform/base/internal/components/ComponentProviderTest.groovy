package dev.nokee.platform.base.internal.components

import dev.nokee.model.DomainObjectFactory
import dev.nokee.model.DomainObjectIdentifier
import dev.nokee.model.DomainObjectProvider
import dev.nokee.model.DomainObjectProviderTest
import dev.nokee.model.internal.*
import dev.nokee.model.testers.SampleProvider
import dev.nokee.model.testers.SampleProviders
import dev.nokee.model.testers.TestProviderGenerator
import dev.nokee.platform.base.Component
import dev.nokee.platform.base.internal.ComponentIdentifier
import dev.nokee.platform.base.internal.ComponentName
import org.apache.commons.lang3.RandomStringUtils
import org.gradle.api.Project
import org.gradle.api.model.ObjectFactory
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.BeforeAll

class ComponentProviderTest extends DomainObjectProviderTest<Component> {
	private static TestProviderGenerator<Component> subjectGenerator;

	@BeforeAll
	static void setUp() {
		subjectGenerator = new TestComponentProviderGenerator();
	}

	@Override
	protected TestProviderGenerator<Component> getSubjectGenerator() {
		return subjectGenerator
	}

	private static class TestComponentGenerator {
		protected final Project project = ProjectBuilder.builder().build()
		protected final DomainObjectEventPublisher eventPublisher = new DomainObjectEventPublisherImpl()
		private final ComponentConfigurer configurer = new ComponentConfigurer(eventPublisher)
		private final RealizableDomainObjectRealizer realizer = new RealizableDomainObjectRealizerImpl(eventPublisher)
		private final ComponentRepository repository = new ComponentRepository(eventPublisher, realizer, project.getProviders())
		protected final ComponentProviderFactory providerFactory = new ComponentProviderFactory(repository, configurer)
		protected final ComponentInstantiator instantiator = new ComponentInstantiator("test component")

		protected <T extends Component> ComponentIdentifier<T> identifier(String name, Class<T> type) {
			return ComponentIdentifier.of(ComponentName.of(name), type, ProjectIdentifier.of(project))
		}
	}

	private static final class TestComponentProviderGenerator extends TestComponentGenerator implements TestProviderGenerator<Component> {
		private final SampleProviders<Component> samples

		TestComponentProviderGenerator() {
			this.samples = new SampleProviders<Component>(
				sample('main', Type0),
				sample('test', Type1),
				sample('common', Type2))
		}

		@Override
		SampleProviders<Component> samples() {
			return samples
		}

		private SampleProvider<Component> sample(String name, Class<? extends Component> type) {
			def entity = project.objects.newInstance(type)
			def identifier = identifier(name, type)
			eventPublisher.publish(new DomainObjectDiscovered<>(identifier))
			eventPublisher.publish(new RealizableDomainObjectDiscovered(identifier, {
				eventPublisher.publish(new DomainObjectCreated<>(identifier, entity))
			}))
			def provider = providerFactory.create(identifier)
			return new SampleProvider<Component>(provider, type, identifier)
		}

		@Override
		DomainObjectProvider<Component> create() {
			def identifier = identifier(newLowerCamelRandomString(), Type0)
			eventPublisher.publish(new DomainObjectDiscovered<>(identifier))
			eventPublisher.publish(new RealizableDomainObjectDiscovered(identifier, {
				eventPublisher.publish(new DomainObjectCreated<>(identifier, project.objects.newInstance(Type0)))
			}))
			return providerFactory.create(identifier)
		}

		Class<Component> getType() {
			return Type0
		}

		protected static String newLowerCamelRandomString() {
			return 'a' + RandomStringUtils.randomAlphanumeric(12)
		}

		private static final class ObjectFactoryFactory<T> implements DomainObjectFactory<T> {
			private final ObjectFactory objectFactory

			public ObjectFactoryFactory(ObjectFactory objectFactory) {
				this.objectFactory = objectFactory
			}

			@Override
			T create(DomainObjectIdentifier identifier) {
				return objectFactory.newInstance(((TypeAwareDomainObjectIdentifier<T>) identifier).getType());
			}
		}

		private interface Type0 extends Component {}
		private interface Type1 extends Component {}
		private interface Type2 extends Component {}
	}
}
