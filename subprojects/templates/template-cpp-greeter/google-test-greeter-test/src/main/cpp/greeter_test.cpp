#include "gtest/gtest.h"

#include "greeter.h"

TEST(GreeterTest, can_greet_Alice) {
  EXPECT_EQ(say_hello("Alice"), "Bonjour, Alice!");
}
