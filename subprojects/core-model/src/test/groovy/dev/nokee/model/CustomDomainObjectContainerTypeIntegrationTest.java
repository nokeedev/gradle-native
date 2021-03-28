package dev.nokee.model;

import dev.nokee.model.internal.BaseNamedDomainObjectContainer;
import dev.nokee.model.internal.registry.DefaultModelRegistry;
import dev.nokee.model.internal.registry.ModelLookup;
import dev.nokee.model.internal.registry.ModelRegistry;

import javax.inject.Inject;

import static dev.gradleplugins.grava.testing.util.ProjectTestUtils.objectFactory;
import static dev.nokee.model.internal.BaseNamedDomainObjectContainer.namedContainer;
import static dev.nokee.model.internal.type.ModelType.of;

class CustomDomainObjectContainerTypeIntegrationTest extends NamedDomainObjectViewTester<CustomDomainObjectContainerTypeIntegrationTest.MyType> {
	@Override
	protected TestContainerGenerator<MyType> getSubjectGenerator() {
		return new TestContainerGenerator<MyType>() {
			private final DefaultModelRegistry modelRegistry = new DefaultModelRegistry(objectFactory()::newInstance);

			@Override
			public DomainObjectContainer<MyType> create(String name) {
				return modelRegistry.register(namedContainer(name, of(CustomContainerOfMyType.class))).get();
			}

			@Override
			public Class<? extends DomainObjectView<MyType>> getSubjectType() {
				return CustomContainerOfMyType.class;
			}

			@Override
			public Class<MyType> getElementType() {
				return MyType.class;
			}

			@Override
			@SuppressWarnings("unchecked")
			public <S extends MyType> Class<S> getSubElementType() {
				return (Class<S>) MyChildType.class;
			}

			@Override
			public ModelRegistry getModelRegistry() {
				return modelRegistry;
			}

			@Override
			public ModelLookup getModelLookup() {
				return modelRegistry;
			}
		};
	}

	interface MyType {}
	interface MyChildType extends MyType {}
	static class CustomContainerOfMyType extends BaseNamedDomainObjectContainer<MyType> {
		@Inject
		public CustomContainerOfMyType(Class<MyType> elementType) {
			super(elementType);
		}
	}
}
