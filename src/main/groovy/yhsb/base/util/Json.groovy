package yhsb.base.util

import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import groovy.transform.PackageScope

import java.lang.reflect.Type

abstract class JsonField {
    @PackageScope
    protected String value

    String getValue() {
        value
    }

    abstract Map<String, String> getValueMap()

    String getName() {
        if (valueMap.containsKey(value)) {
            valueMap[value]
        } else {
            "未知值: $value"
        }
    }

    @Override
    String toString() {
        name
    }
}

interface JsonAdapter<T> extends JsonSerializer<T>, JsonDeserializer<T> {}

class JsonFieldAdapter implements JsonAdapter<JsonField> {
    @Override
    JsonField deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        def classOfT = typeOfT as Class<?>
        def field = classOfT.getConstructor().newInstance() as JsonField
        field.@value = json.asString
        field
    }

    @Override
    JsonElement serialize(JsonField src, Type typeOfSrc, JsonSerializationContext context) {
        new JsonPrimitive(src.@value)
    }
}

class Json {
    @Lazy private static final gson =
            new GsonBuilder()
                    .serializeNulls()
                    .registerTypeHierarchyAdapter(JsonField, new JsonFieldAdapter())
                    .create()

    static <T> String toJson(T obj) {
        gson.toJson(obj)
    }

    static <T> T fromJson(String json, Type typeOfT) {
        gson.fromJson(json, typeOfT)
    }
}

trait Jsonable {
    String toJson() {
        Json.toJson(this)
    }

    @Override
    String toString() {
        toJson()
    }
}
