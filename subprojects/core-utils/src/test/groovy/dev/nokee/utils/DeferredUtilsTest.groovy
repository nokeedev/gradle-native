package dev.nokee.utils

import kotlin.jvm.functions.Function0
import org.gradle.api.Named
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Unroll

import java.util.concurrent.Callable
import java.util.function.Supplier

import static dev.nokee.utils.DeferredUtils.isDeferred
import static dev.nokee.utils.DeferredUtils.isNestableDeferred
import static dev.nokee.utils.DeferredUtils.realize
import static dev.nokee.utils.DeferredUtils.unpack

@Subject(DeferredUtils)
class DeferredUtilsTest extends Specification {
	def project = ProjectBuilder.builder().build()
	def objects = project.objects
	def providers = project.providers

	def "can unpack provider types"() {
		expect:
		unpack(propertyOf('foo')) == 'foo'
		unpack(providerOf('foo')) == 'foo'
		unpack(closureOf('foo')) == 'foo'
		unpack(supplierOf('foo')) == 'foo'
		unpack(kotlinFunctionOf('foo')) == 'foo'
		unpack(callableOf('foo')) == 'foo'
	}

	def "can detect deferrable types"() {
		expect:
		// are
		isDeferred(propertyOf(Object, throwIfResolved()))
		isDeferred(providerOf(throwIfResolved()))
		isDeferred(closureOf(throwIfResolved()))
		isDeferred(supplierOf(throwIfResolved()))
		isDeferred(kotlinFunctionOf(throwIfResolved()))
		isDeferred(callableOf(throwIfResolved()))

		// aren't
		!isDeferred('foo')
	}

	def "can detect nestable deferrable types"() {
		expect:
		// are
		isNestableDeferred(closureOf(throwIfResolved()))
		isNestableDeferred(supplierOf(throwIfResolved()))
		isNestableDeferred(kotlinFunctionOf(throwIfResolved()))
		isNestableDeferred(callableOf(throwIfResolved()))

		// aren't
		!isNestableDeferred(propertyOf(Object, throwIfResolved()))
		!isNestableDeferred(providerOf(throwIfResolved()))
		!isDeferred('foo')
	}

	@Unroll
	def "can realize deferred collection"(collectionFactoryMethod) {
		given:
		def collection = objects."${collectionFactoryMethod}"(String)

		and:
		def realizedElements = []
		collection.configureEach {
			realizedElements << it
		}

		and:
		collection.addLater(providerOf(namedElement('foo')))
		collection.addLater(providerOf(namedElement('bar')))

		when:
		assert realizedElements == []
		realize(collection)

		then:
		realizedElements*.name as Set == ['foo', 'bar'] as Set

		where:
		collectionFactoryMethod << ['domainObjectContainer', 'domainObjectSet', 'namedDomainObjectList', 'namedDomainObjectSet']
	}

	@Unroll
	def "can realize empty deferred collection"(collectionFactoryMethod) {
		given:
		def collection = objects."${collectionFactoryMethod}"(String)

		when:
		realize(collection)

		then:
		noExceptionThrown()

		where:
		collectionFactoryMethod << ['domainObjectContainer', 'domainObjectSet', 'namedDomainObjectList', 'namedDomainObjectSet']
	}

//	def "can unpack list to File"() {
//		given:
//		def deferred = []
//
//		expect:
//
//	}

	protected Property<String> propertyOf(String value) {
		return propertyOf(String, value)
	}

	protected <T> Property<T> propertyOf(Class<T> type, T value) {
		return objects.property(type).value(providerOf(value))
	}

	protected <T> Provider<T> providerOf(T value) {
		return providers.provider { ThrowIfResolvedGuard.resolve(value) }
	}

	protected <T> Supplier<T> supplierOf(T value) {
		return new Supplier<T>() {
			@Override
			T get() {
				return ThrowIfResolvedGuard.resolve(value)
			}
		}
	}

	protected <T> Closure<T> closureOf(T value) {
		return { ThrowIfResolvedGuard.resolve(value) }
	}

	protected <T> Function0<T> kotlinFunctionOf(T value) {
		return new Function0<T>() {
			@Override
			T invoke() {
				return ThrowIfResolvedGuard.resolve(value)
			}
		}
	}

	protected <T> Callable<T> callableOf(T value) {
		return new Callable<T>() {
			@Override
			T call() throws Exception {
				return ThrowIfResolvedGuard.resolve(value)
			}
		}
	}

	protected static Named namedElement(String name) {
		return new Named() {
			@Override
			String getName() {
				return name
			}
		}
	}

	protected static ThrowIfResolvedGuard throwIfResolved() {
		return new ThrowIfResolvedGuard()
	}

	private static class ThrowIfResolvedGuard {
		static <T> T resolve(T obj) {
			if (obj instanceof ThrowIfResolvedGuard) {
				throw new AssertionError((Object)"Should not resolve")
			}
			return obj;
		}
	}
}
