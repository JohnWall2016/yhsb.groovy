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

import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

interface JsonAdapter<T> extends JsonSerializer<T>, JsonDeserializer<T> {}

class JsonFieldAdapter implements JsonAdapter<MapField> {
    @Override
    MapField deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        def classOfT = typeOfT as Class<?>
        def field = classOfT.getConstructor().newInstance() as MapField
        field.@value = json.asString
        field
    }

    @Override
    JsonElement serialize(MapField src, Type typeOfSrc, JsonSerializationContext context) {
        new JsonPrimitive(src.@value)
    }
}

class DataFieldAdapter implements JsonAdapter<ListField> {
    @Override
    ListField deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        def paramType = typeOfT as ParameterizedType
        def rawType = paramType.rawType as Class<?>
        def argType =  paramType.actualTypeArguments[0] as Class<?>

        def field = rawType.getConstructor().newInstance() as ListField

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
    JsonElement serialize(ListField src, Type typeOfSrc, JsonSerializationContext context) {
        context.serialize(src, typeOfSrc)
    }
}

class Json {
    @Lazy private static final gson =
            new GsonBuilder()
                    .serializeNulls()
                    .registerTypeHierarchyAdapter(MapField, new JsonFieldAdapter())
                    .registerTypeHierarchyAdapter(ListField, new DataFieldAdapter())
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
