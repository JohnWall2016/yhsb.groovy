package yhsb.base.util.reflect

import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.lang.reflect.TypeVariable

class GenericClass<T> implements ParameterizedType {

    Class<T> rawClass
    Class<?>[] actualClassArguments

    LinkedHashMap<TypeVariable, Class<?>> typeParametersMapping = [:]

    GenericClass(Class<T> rawClass, Class<?>... actualClassArguments) {
        if (rawClass.typeParameters.length != actualClassArguments.length) {
            throw new IllegalArgumentException(
                    "the length of type parameters isn't equal to the length of type arguments: " +
                    "$rawClass $actualClassArguments"
            )
        }

        this.rawClass = rawClass
        this.actualClassArguments = actualClassArguments

        rawClass.typeParameters?.eachWithIndex { type, i ->
            //println type
            typeParametersMapping[type] = actualClassArguments[i]
        }
    }

    Class<?> getActualType(TypeVariable typeVariable) {
        typeParametersMapping.get(typeVariable)
    }

    T newInstance() {
        rawClass.getConstructor().newInstance()
    }

    public <T> GenericClass<T> createGenericClass(Type type) {
        if (Class<T>.isInstance(type)) {
            new GenericClass<T>(type as Class<T>)
        } else if (ParameterizedType.isInstance(type)) {
            type = type as ParameterizedType
            def rawClass = type.rawType as Class<T>
            List<Class<?>> actualClassArguments = []
            type.actualTypeArguments.each {
                Class<?> actualClass
                if (TypeVariable.isInstance(it)) {
                    actualClass = getActualType(it as TypeVariable)
                } else {
                    actualClass = it as Class<?>
                }
                actualClassArguments.add(actualClass)
            }
            new GenericClass<T>(rawClass, *actualClassArguments)
        } else {
            null
        }
    }

    @Override
    Type[] getActualTypeArguments() {
        return actualClassArguments
    }

    @Override
    Type getRawType() {
        return rawClass
    }

    @Override
    Type getOwnerType() {
        return null
    }

    @Override
    String toString() {
        List<String> params = []
        typeParametersMapping.each {
            params.add("${it.key}=${it.value.name}")
        }
        def typeParams = ""
        if (params) {
            typeParams = "<" + params.join(',') + ">"
        }
        "$rawClass$typeParams"
    }
}