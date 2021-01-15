package dev.nokee.publish.bintray.internal;

import lombok.EqualsAndHashCode;
import org.gradle.api.tasks.Input;

import java.util.function.Supplier;

import static com.google.common.base.Suppliers.ofInstance;
import static java.util.Objects.requireNonNull;

public abstract class BintrayCredentials {
	@Input
	public abstract String getBintrayUser();

	@Input
	public abstract String getBintrayKey();

	@EqualsAndHashCode(callSuper = false)
	private static final class DefaultBintrayCredentials extends BintrayCredentials {
		private final Supplier<String> userSupplier;
		private final Supplier<String> keySupplier;
		private final BintrayCredentials parent;

		private DefaultBintrayCredentials(Supplier<String> userSupplier, Supplier<String> keySupplier, BintrayCredentials parent) {
			this.userSupplier = userSupplier;
			this.keySupplier = keySupplier;
			this.parent = parent;
		}

		@Override
		public String getBintrayUser() {
			if (isCredentialsAvailable()) {
				return userSupplier.get();
			}
			return parent.getBintrayUser();
		}

		@Override
		public String getBintrayKey() {
			if (isCredentialsAvailable()) {
				return keySupplier.get();
			}
			return parent.getBintrayKey();
		}

		private boolean isCredentialsAvailable() {
			return userSupplier.get() != null && keySupplier.get() != null;
		}
	}

	@EqualsAndHashCode(callSuper = false)
	private static final class InvalidBintrayCredentials extends BintrayCredentials {
		@Override
		public String getBintrayUser() {
			throw invalidCredentials();
		}

		@Override
		public String getBintrayKey() {
			throw invalidCredentials();
		}

		private static RuntimeException invalidCredentials() {
			return new IllegalStateException("When publishing to Bintray repositories, please specify the credentials using the credentials DSL on the repository, e.g. credentials { }.");
		}
	}

	public static BintrayCredentials invalidBintrayCredentials() {
		return new InvalidBintrayCredentials();
	}

	public static BintrayCredentials of(String user, String key) {
		return BintrayCredentials.builder()
			.withUser(ofInstance(requireNonNull(user)))
			.withKey(ofInstance(requireNonNull(key)))
			.build();
	}

	public static Builder builder() {
		return new Builder();
	}

	public static final class Builder {
		private Supplier<String> userSupplier;
		private Supplier<String> keySupplier;
		private BintrayCredentials parentCredentials = new InvalidBintrayCredentials();

		public Builder withUser(Supplier<String> userSupplier) {
			this.userSupplier = requireNonNull(userSupplier);
			return this;
		}

		public Builder withKey(Supplier<String> keySupplier) {
			this.keySupplier = requireNonNull(keySupplier);
			return this;
		}

		public Builder withParent(BintrayCredentials parentCredentials) {
			this.parentCredentials = requireNonNull(parentCredentials);
			return this;
		}

		public BintrayCredentials build() {
			return new DefaultBintrayCredentials(requireNonNull(userSupplier), requireNonNull(keySupplier), parentCredentials);
		}
	}
}
