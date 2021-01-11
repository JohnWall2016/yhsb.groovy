package yhsb.base.util

import groovy.transform.MapConstructor
import groovy.xml.MarkupBuilder
import groovy.xml.slurpersupport.GPathResult

import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target
import java.lang.reflect.Modifier

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@interface NS {
    String name()
    String value()
}

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@interface Namespaces {
    NS[] value() default []
}

@Retention(RetentionPolicy.RUNTIME)
@Target([ElementType.TYPE, ElementType.FIELD])
@interface Node {
    String value() default ''
}

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@interface Attribute {
    String value() default ''
}

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@interface Text {
    String value() default ''
}

trait toXml {
    String toXml() {
        def writer = new StringWriter()
        def markup = new MarkupBuilder(writer)

        toXml(markup)

        writer.toString()
    }

    void toXml(MarkupBuilder markup) {
        this.class.with {
            def node = getAnnotation(Node)?.value() ?: it.name
            def nss = getAnnotation(Namespaces)?.toMap() ?: [:]

            def attrs = [:]
            nss.each {
                def v = it.key.isEmpty() ? 'xmlns' : "xmlns:${it.key}"
                attrs[v] = it.value
            }

            declaredFields.each {field ->
                if (Modifier.isTransient(field.modifiers)) return

            }
        }
        null
    }
}

@MapConstructor
class XmlExtensions {
    static Map<String, String> toMap(Namespaces nss) {
        Map<String, String> nsMap = [:]
        nss.value().each {
            nsMap[it.name()] = it.value()
        }
        nsMap
    }

    static <T> void toObject(GPathResult rs, T obj) {
        obj.class.with {
            rs.declareNamespace(getAnnotation(Namespaces)?.toMap() ?: [:])
            declaredFields.each { field ->
                def anno = field.getAnnotation(Text)
                if (anno) {
                    println "Text: $field"
                    obj[field.name] = rs.text()
                } else if (anno = field.getAnnotation(Attribute)) {
                    def name = anno.value() ?: field.name
                    println "Attr: $name ${rs.getClass()} ${rs["@$name"]}"
                    obj[field.name] = rs["@$name"]
                } else if (anno = field.getAnnotation(Node)) {
                    println "Tag: ${field.type}"
                    def name = anno.value() ?: field.name
                    def items = rs[name]
                    if (items && GPathResult.isInstance(items)) {
                        obj[field.name] = (items as GPathResult).toObject(field.type)
                    }
                }
            }
        }
    }

    static <T> T toObject(GPathResult rs, Class<T> clazz) {
        def obj = clazz.getConstructor().newInstance()
        rs.toObject(obj)
        obj
    }
}
