#include "com_example_greeter_Greeter.h"

#include <string.h>
#include <stdlib.h>

#include "greeter.h"

static const char * ERROR_STRING = "name cannot be null";

JNIEXPORT jstring JNICALL Java_com_example_greeter_Greeter_sayHello(JNIEnv * env, jobject self, jstring name_from_java) {
	// Ensure parameter isn't null
	if (name_from_java == NULL) {
		return (*env)->NewStringUTF(env, ERROR_STRING);
	}

	// Convert jstring to std::string
	const char *name_as_c_str = (*env)->GetStringUTFChars(env, name_from_java, NULL);
	char * result_from_c = say_hello(name_as_c_str);  // Call native library
	(*env)->ReleaseStringUTFChars(env, name_from_java, name_as_c_str);

	// Convert std::string to jstring
	jstring result = (*env)->NewStringUTF(env, result_from_c);

	// Return result back to Java
	return result;
}
