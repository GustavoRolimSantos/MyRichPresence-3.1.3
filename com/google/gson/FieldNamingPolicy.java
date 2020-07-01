package com.google.gson;

import java.lang.reflect.*;
import java.util.*;

public enum FieldNamingPolicy implements FieldNamingStrategy
{
    IDENTITY {
        @Override
        public String translateName(final Field f) {
            return f.getName();
        }
    }, 
    UPPER_CAMEL_CASE {
        @Override
        public String translateName(final Field f) {
            return FieldNamingPolicy.upperCaseFirstLetter(f.getName());
        }
    }, 
    UPPER_CAMEL_CASE_WITH_SPACES {
        @Override
        public String translateName(final Field f) {
            return FieldNamingPolicy.upperCaseFirstLetter(FieldNamingPolicy.separateCamelCase(f.getName(), " "));
        }
    }, 
    LOWER_CASE_WITH_UNDERSCORES {
        @Override
        public String translateName(final Field f) {
            return FieldNamingPolicy.separateCamelCase(f.getName(), "_").toLowerCase(Locale.ENGLISH);
        }
    }, 
    LOWER_CASE_WITH_DASHES {
        @Override
        public String translateName(final Field f) {
            return FieldNamingPolicy.separateCamelCase(f.getName(), "-").toLowerCase(Locale.ENGLISH);
        }
    }, 
    LOWER_CASE_WITH_DOTS {
        @Override
        public String translateName(final Field f) {
            return FieldNamingPolicy.separateCamelCase(f.getName(), ".").toLowerCase(Locale.ENGLISH);
        }
    };
    
    static String separateCamelCase(final String name, final String separator) {
        final StringBuilder translation = new StringBuilder();
        for (int i = 0, length = name.length(); i < length; ++i) {
            final char character = name.charAt(i);
            if (Character.isUpperCase(character) && translation.length() != 0) {
                translation.append(separator);
            }
            translation.append(character);
        }
        return translation.toString();
    }
    
    static String upperCaseFirstLetter(final String name) {
        final StringBuilder fieldNameBuilder = new StringBuilder();
        int index = 0;
        char firstCharacter = name.charAt(index);
        for (int length = name.length(); index < length - 1 && !Character.isLetter(firstCharacter); firstCharacter = name.charAt(++index)) {
            fieldNameBuilder.append(firstCharacter);
        }
        if (!Character.isUpperCase(firstCharacter)) {
            final String modifiedTarget = modifyString(Character.toUpperCase(firstCharacter), name, ++index);
            return fieldNameBuilder.append(modifiedTarget).toString();
        }
        return name;
    }
    
    private static String modifyString(final char firstCharacter, final String srcString, final int indexOfSubstring) {
        return (indexOfSubstring < srcString.length()) ? (firstCharacter + srcString.substring(indexOfSubstring)) : String.valueOf(firstCharacter);
    }
}
