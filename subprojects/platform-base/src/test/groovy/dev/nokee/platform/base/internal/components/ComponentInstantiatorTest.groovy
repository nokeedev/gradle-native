package dev.nokee.platform.base.internal.components

import dev.nokee.model.internal.AbstractPolymorphicDomainObjectInstantiatorTest
import dev.nokee.model.internal.PolymorphicDomainObjectInstantiator
import dev.nokee.platform.base.Component
import spock.lang.Subject

@Subject(ComponentInstantiator)
class ComponentInstantiatorTest extends AbstractPolymorphicDomainObjectInstantiatorTest<Component> {
	@Override
	protected PolymorphicDomainObjectInstantiator<Component> newSubject(String displayName) {
		return new ComponentInstantiator(displayName)
	}

	@Override
	protected Class<Component> getBaseType() {
		return Component
	}

	@Override
	protected Class<? extends Component> getChildType() {
		return MyComponent
	}

	@Override
	protected List<Class<? extends Component>> registerKnownTypes(PolymorphicDomainObjectInstantiator<Component> subject) {
		subject.registerFactory(MyKnownComponent1, { Stub(MyKnownComponent1) })
		subject.registerFactoryIfAbsent(MyKnownComponent2, { Stub(MyKnownComponent2) })
		return [MyKnownComponent1, MyKnownComponent2]
	}

	interface MyComponent extends Component {}
	interface MyKnownComponent1 extends Component {}
	interface MyKnownComponent2 extends Component {}
}
