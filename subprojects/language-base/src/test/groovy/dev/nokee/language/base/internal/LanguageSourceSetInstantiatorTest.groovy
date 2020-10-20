package dev.nokee.language.base.internal

import dev.nokee.language.base.LanguageSourceSet
import dev.nokee.model.internal.AbstractPolymorphicDomainObjectInstantiatorTest
import dev.nokee.model.internal.PolymorphicDomainObjectInstantiator
import spock.lang.Subject

@Subject(LanguageSourceSetInstantiatorImpl)
class LanguageSourceSetInstantiatorTest extends AbstractPolymorphicDomainObjectInstantiatorTest<LanguageSourceSet> {
	@Override
	protected PolymorphicDomainObjectInstantiator<LanguageSourceSet> newSubject(String displayName) {
		return new LanguageSourceSetInstantiatorImpl(displayName)
	}

	@Override
	protected Class<LanguageSourceSet> getBaseType() {
		return LanguageSourceSet
	}

	@Override
	protected Class<? extends LanguageSourceSet> getChildType() {
		return MyLanguageSourceSet
	}

	@Override
	protected List<Class<? extends LanguageSourceSet>> registerKnownTypes(PolymorphicDomainObjectInstantiator<LanguageSourceSet> subject) {
		subject.registerFactory(MyKnownLanguageSourceSet1, { Stub(MyKnownLanguageSourceSet1) })
		subject.registerFactoryIfAbsent(MyKnownLanguageSourceSet2, { Stub(MyKnownLanguageSourceSet2) })
		return [MyKnownLanguageSourceSet1, MyKnownLanguageSourceSet2]
	}

	interface MyLanguageSourceSet extends LanguageSourceSet {}
	interface MyKnownLanguageSourceSet1 extends LanguageSourceSet {}
	interface MyKnownLanguageSourceSet2 extends LanguageSourceSet {}
}
