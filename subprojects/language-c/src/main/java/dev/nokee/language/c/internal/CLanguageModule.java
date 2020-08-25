package dev.nokee.language.c.internal;

import dagger.Module;

@Module(includes = {CHeaderSetModule.class, CSourceSetModule.class})
public interface CLanguageModule {
}
