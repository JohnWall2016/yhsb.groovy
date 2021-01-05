package yhsb.base.util

import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParseException
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import groovy.transform.PackageScope

import java.lang.reflect.ParameterizedType
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

class DataField<T extends Jsonable> implements Iterable<T> {
    final List<T> items = []

    @Override
    Iterator<T> iterator() {
        items.iterator()
    }

    void add(T e) {
        items.add(e)
    }

    T getAt(int index) {
        items[index]
    }

    int size() {
        items.size()
    }

    boolean isEmpty() {
        items.empty
    }
}

class DataFieldAdapter implements JsonAdapter<DataField> {
    @Override
    DataField deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        def paramType = typeOfT as ParameterizedType
        def rawType = paramType.rawType as Class<?>
        def argType =  paramType.actualTypeArguments[0] as Class<?>

        def field = rawType.getConstructor().newInstance() as DataField

        if (JsonArray.isInstance(json)) {
            def array = json as JsonArray
            array.each { elem ->
                if (JsonObject.isInstance(elem)) {
                    def obj = elem as JsonObject
                    if (obj.size() > 0) {
                        field.items.add(Json.fromJson(obj, argType))
                    }
                }
            }
        }
        field
    }

    @Override
    JsonElement serialize(DataField src, Type typeOfSrc, JsonSerializationContext context) {
        context.serialize(src, typeOfSrc)
    }
}

class Json {
    @Lazy private static final gson =
            new GsonBuilder()
                    .serializeNulls()
                    .registerTypeHierarchyAdapter(JsonField, new JsonFieldAdapter())
                    .registerTypeHierarchyAdapter(DataField, new DataFieldAdapter())
                    .create()

    static <T> String toJson(T obj) {
        gson.toJson(obj)
    }

    static <T> T fromJson(String json, Type typeOfT) {
        gson.fromJson(json, typeOfT)
    }

    static <T> T fromJson(JsonElement element, Type typeOfT) {
        gson.fromJson(element, typeOfT)
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
