package dev.nokee.platform.base.internal.dependencies

import dev.nokee.model.AbstractDomainObjectFactoryRegistryTest
import dev.nokee.model.AbstractDomainObjectInstantiatorTest
import dev.nokee.model.AbstractPolymorphicDomainObjectInstantiatorTest
import dev.nokee.model.DomainObjectFactory
import dev.nokee.model.DomainObjectIdentifier
import dev.nokee.platform.base.DependencyBucket
import dev.nokee.platform.base.DependencyBucketName
import dev.nokee.platform.base.internal.ProjectIdentifier
import spock.lang.Subject

@Subject(DependencyBucketInstantiatorImpl)
class DependencyBucketInstantiatorImplTest extends AbstractPolymorphicDomainObjectInstantiatorTest {

	@Override
	protected newSubject() {
		return new DependencyBucketInstantiatorImpl()
	}

	@Override
	protected Set<Class<?>> registerKnownTypes(Object subject) {
		subject.registerFactory(MyKnownType1, {Stub(MyKnownType1)})
		subject.registerFactory(MyKnownType2, {Stub(MyKnownType2)})
		return [MyKnownType1, MyKnownType2]
	}

	interface MyKnownType1 extends DependencyBucket {}
	interface MyKnownType2 extends DependencyBucket {}
}

@Subject(DependencyBucketInstantiatorImpl)
class DependencyBucketInstantiatorImpl_FactoryTest extends AbstractDomainObjectFactoryRegistryTest<DependencyBucketInstantiatorImpl> {
	@Override
	protected DependencyBucketInstantiatorImpl newSubject() {
		return new DependencyBucketInstantiatorImpl()
	}

	@Override
	protected Class<?> getBaseType() {
		return DependencyBucket
	}

	@Override
	protected Class<?> getChildType() {
		return MyChildBucket
	}

	interface MyChildBucket extends DependencyBucket {}
}

@Subject(DependencyBucketInstantiatorImpl)
class DependencyBucketInstantiatorImpl_InstantiatorTest extends AbstractDomainObjectInstantiatorTest<DependencyBucketInstantiatorImpl> {
	@Override
	protected DependencyBucketInstantiatorImpl newSubject() {
		return new DependencyBucketInstantiatorImpl()
	}

//	@Override
//	protected Class<?> getBaseType() {
//		return DependencyBucket
//	}
//
	@Override
	protected Class<?> getChildType() {
		return MyChildBucket
	}

	@Override
	protected String getDisplayName() {
		return 'dependency bucket'
	}

	@Override
	protected Set<Class<?>> registerKnownTypes(DependencyBucketInstantiatorImpl subject) {
		subject.registerFactory(MyKnownType1, {Stub(MyKnownType1)})
		subject.registerFactory(MyKnownType2, {Stub(MyKnownType2)})
		return [MyKnownType1, MyKnownType2]
	}

	@Override
	protected void registerType(DependencyBucketInstantiatorImpl subject, Class<?> type, DomainObjectFactory<?> factory) {
		subject.registerFactory(type as Class<DependencyBucket>, factory as DomainObjectFactory<? extends DependencyBucket>)
	}

	@Override
	protected DomainObjectIdentifier identifier() {
		return DependencyIdentifier.of(DependencyBucketName.of('foo'), DependencyBucket, ProjectIdentifier.of('root'))
	}

	interface MyChildBucket extends DependencyBucket {}

	interface MyKnownType1 extends DependencyBucket {}
	interface MyKnownType2 extends DependencyBucket {}
}
