package dev.nokee.language.cpp.internal;

import dagger.Module;

@Module(includes = {CppHeaderSetModule.class, CppSourceSetModule.class})
public interface CppLanguageModule {
}
