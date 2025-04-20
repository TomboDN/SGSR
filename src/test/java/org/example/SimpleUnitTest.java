package org.example;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SimpleUnitTest {

    @Test
    void testStringConcatenation() {
        String str1 = "Hello";
        String str2 = "World";
        String result = str1 + " " + str2;

        assertEquals("Hello World", result);
        assertNotEquals("HelloWorld", result);
    }

    @Test
    void testStringLength() {
        String text = "JUnit";
        assertEquals(5, text.length());
    }

    @Test
    void testStringContains() {
        String message = "Welcome to Java";
        assertTrue(message.contains("Java"));
        assertFalse(message.contains("Python"));
    }

    @Test
    void testStringSubstring() {
        String text = "Hello World";
        assertEquals("World", text.substring(6));
        assertEquals("Hello", text.substring(0, 5));
    }

}