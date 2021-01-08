package yhsb.base.util

import org.w3c.dom.Element
import org.xml.sax.InputSource

import javax.xml.parsers.DocumentBuilderFactory
import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@interface Attribute {
    String value() default ''

    String namespace() default ''
}

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@interface Text {
    String value() default ''

    String namespace() default ''
}

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@interface Tag {
    String value() default ''

    String namespace() default ''
}

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@interface Tags {
    String value() default ''

    String namespace() default ''
}

class XmlExtensions {
    static <T> void toObject(Element e, T obj) {
        obj.class.declaredFields.each { field ->
            def anno = field.getAnnotation(Text)
            if (anno) {
                println "Text: $field"
                obj[field.name] = e.textContent
            } else if (anno = field.getAnnotation(Attribute)) {
                def name = anno.value() ?: field.name
                def ns = anno.namespace()
                println "Attr: $name $ns"
                def value = ns ? e.getAttributeNS(ns, name)
                        : e.getAttribute(name)
                obj[field.name] ?= value
            } else if (anno = field.getAnnotation(Tag)) {
                println "Tag: ${field.type}"
                def name = anno.value() ?: field.name
                def ns = anno.namespace()
                def items = ns ? e.getElementsByTagNameNS(ns, name) :
                        e.getElementsByTagName(name)
                if (items.length > 0) {
                    def item =  items.item(0)
                    if (Element.isInstance(item)) {
                        obj[field.name] = (item as Element).toObject(field.type)
                    }
                }
            } else if (anno = field.getAnnotation(Tags)) {
                println "Tags: ${field.type}"
                def name = anno.value() ?: field.name
                def ns = anno.namespace()
                def items = ns ? e.getElementsByTagNameNS(ns, name) :
                        e.getElementsByTagName(name)
                if (items.length > 0) {
                    def fieldObj = field.type.getConstructor().newInstance()
                    items.each {item ->
                        if (Element.isInstance(item)) {
                            (item as Element).toObject(fieldObj)
                        }
                    }
                    obj[field.name] = fieldObj
                }
            }
        }
    }

    static <T> T toObject(Element e, Class<T> clazz) {
        def obj = clazz.getConstructor().newInstance()
        e.toObject(obj)
        obj
    }
}

class Xml {
    static Element rootElement(String xml) {
        DocumentBuilderFactory.newInstance()
                .with {
                    namespaceAware = true
                    newDocumentBuilder()
                            .parse(new InputSource(new StringReader(xml)))
                            .documentElement
                }
    }
}
