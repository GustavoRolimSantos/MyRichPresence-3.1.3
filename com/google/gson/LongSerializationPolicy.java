package com.google.gson;

public enum LongSerializationPolicy
{
    DEFAULT {
        @Override
        public JsonElement serialize(final Long value) {
            return new JsonPrimitive(value);
        }
    }, 
    STRING {
        @Override
        public JsonElement serialize(final Long value) {
            return new JsonPrimitive(String.valueOf(value));
        }
    };
    
    public abstract JsonElement serialize(final Long p0);
}
