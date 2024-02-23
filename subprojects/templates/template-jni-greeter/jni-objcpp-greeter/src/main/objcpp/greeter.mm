#include "com_example_greeter_Greeter.h"

#include <string.h>
#include <stdlib.h>

#import "greeter.h"

static const char * ERROR_STRING = "name cannot be null";

JNIEXPORT jstring JNICALL Java_com_example_greeter_Greeter_sayHello(JNIEnv * env, jobject self, jstring name_from_java) {
    // Ensure parameter isn't null
    if (name_from_java == nullptr) {
        return env->NewStringUTF(ERROR_STRING);
    }

    // Convert jstring to std::string
    const char *name_as_c_str = env->GetStringUTFChars(name_from_java, nullptr);
    Greeter * greeter = [Greeter new];
    auto result_from_cpp = [greeter sayHello:name_as_c_str];  // Call native library
    env->ReleaseStringUTFChars(name_from_java, name_as_c_str);

    // Convert std::string to jstring
    jstring result = env->NewStringUTF(result_from_cpp.c_str());

    // Return result back to Java
    return result;
}
