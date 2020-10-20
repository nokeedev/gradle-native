package dev.nokee.language.base.internal;

import dev.nokee.language.base.LanguageSourceSet;
import dev.nokee.model.DomainObjectIdentifier;
import dev.nokee.model.internal.DomainObjectIdentifierInternal;
import dev.nokee.model.internal.NameAwareDomainObjectIdentifier;
import dev.nokee.model.internal.TypeAwareDomainObjectIdentifier;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.Optional;

@ToString
@EqualsAndHashCode
public final class LanguageSourceSetIdentifier<T extends LanguageSourceSet> implements TypeAwareDomainObjectIdentifier<T>, DomainObjectIdentifierInternal, NameAwareDomainObjectIdentifier {
	@Getter private final LanguageSourceSetName name;
	@Getter private final Class<T> type;
	@Getter private final DomainObjectIdentifierInternal ownerIdentifier;

	private LanguageSourceSetIdentifier(LanguageSourceSetName name, Class<T> type, DomainObjectIdentifierInternal ownerIdentifier) {
		assert name != null;
		assert type != null;
		assert ownerIdentifier != null;
		this.name = name;
		this.type = type;
		this.ownerIdentifier = ownerIdentifier;
	}

	public static <S extends LanguageSourceSet> LanguageSourceSetIdentifier<S> of(LanguageSourceSetName name, Class<S> type, DomainObjectIdentifier ownerIdentifier) {
		return new LanguageSourceSetIdentifier<>(name, type, (DomainObjectIdentifierInternal)ownerIdentifier);
	}

	@Override
	public Optional<? extends DomainObjectIdentifierInternal> getParentIdentifier() {
		return Optional.of(ownerIdentifier);
	}

	@Override
	public String getDisplayName() {
		throw new UnsupportedOperationException();
	}
}
