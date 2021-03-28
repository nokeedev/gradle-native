package dev.nokee.model;

import dev.nokee.model.internal.BaseDomainObjectView;
import dev.nokee.model.internal.registry.DefaultModelRegistry;
import dev.nokee.model.internal.registry.ModelLookup;
import dev.nokee.model.internal.registry.ModelRegistry;

import javax.inject.Inject;

import static dev.gradleplugins.grava.testing.util.ProjectTestUtils.objectFactory;
import static dev.nokee.model.internal.BaseDomainObjectView.view;
import static dev.nokee.model.internal.type.ModelType.of;

/**
 * It is possible to create custom type of a DomainObjectView.
 */
class CustomDomainObjectViewTypeIntegrationTest extends DomainObjectViewTester<CustomDomainObjectViewTypeIntegrationTest.MyType> {
	@Override
	protected TestViewGenerator<MyType> getSubjectGenerator() {
		return new TestViewGenerator<MyType>() {
			private final DefaultModelRegistry modelRegistry = new DefaultModelRegistry(objectFactory()::newInstance);

			@Override
			public DomainObjectView<MyType> create(String name) {
				return modelRegistry.register(view(name, of(CustomViewOfMyType.class))).get();
			}

			@Override
			public Class<? extends DomainObjectView<MyType>> getSubjectType() {
				return CustomViewOfMyType.class;
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
	static class CustomViewOfMyType extends BaseDomainObjectView<MyType> {
		@Inject
		public CustomViewOfMyType(Class<MyType> elementType) {
			super(elementType);
		}
	}
}
