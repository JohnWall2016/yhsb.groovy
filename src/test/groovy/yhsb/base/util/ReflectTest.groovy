package yhsb.base.util

import yhsb.base.util.reflect.GenericClass
import yhsb.qb.net.OutEnvelope
import yhsb.qb.net.Sncbry

import java.lang.reflect.ParameterizedType
import java.lang.reflect.TypeVariable

public <T> void testGenericClass(Class<T> classOfT) {
    def clazz = new GenericClass(OutEnvelope<T>, classOfT)
    println clazz

    clazz.rawClass.declaredFields.each {field ->
        println "$field|${field.type}|${field.genericType}|${field.genericType.class}"
        if (ParameterizedType.isInstance(field.genericType)) {
            def paramType = field.genericType as ParameterizedType
            paramType.actualTypeArguments.each {
                println "  $it ${it.typeName} ${it.class}"
                if (TypeVariable.isInstance(it)) {
                    println "    ${clazz.getActualType(it as TypeVariable)}"
                }
            }
        }
    }
}

testGenericClass(Sncbry)



