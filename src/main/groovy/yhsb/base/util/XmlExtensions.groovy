package yhsb.base.util

import groovy.transform.MapConstructor
import groovy.transform.PackageScope
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
@Target([ElementType.TYPE, ElementType.FIELD])
@interface Namespaces {
    NS[] value() default []
}

@Retention(RetentionPolicy.RUNTIME)
@Target([ElementType.TYPE, ElementType.FIELD])
@interface Node {
    String value() default ''
}

@Retention(RetentionPolicy.RUNTIME)
@Target([ElementType.FIELD])
@interface Spread {}

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

@PackageScope
class NodeStruct {
    String name
    ToXml node
    Map<String, String> namespaces
    boolean spread
}

class SpreadNode implements ToXml {
    String name
    Map attributes

    SpreadNode(String name, Map attributes) {
        this.name = name
        this.attributes = attributes
    }

    void toXml(MarkupBuilder markup,
               String nodeName,
               Map<String, String> namespaces,
               boolean spread
    ) {
        nodeName ?= name
        markup."$nodeName"(attributes)
    }
}

trait ToXml {
    String toXml() {
        def writer = new StringWriter()
        def markup = new MarkupBuilder(writer)

        toXml(markup, null, null, false)

        writer.toString()
    }

    void toXml(
            MarkupBuilder markup,
            String nodeName,
            Map<String, String> namespaces,
            boolean spread
    ) {
        this.class.with {
            nodeName ?= getAnnotation(Node)?.value() ?: it.name
            namespaces ?= getAnnotation(Namespaces)?.toMap() ?: [:]

            String text = null

            def attrs = [:]
            namespaces.each {
                def v = it.key.isEmpty() ? 'xmlns' : "xmlns:${it.key}"
                attrs[v] = it.value
            }

            List<SpreadNode> spreadNodes = []
            List<NodeStruct> nodes = []

            declaredFields.each { field ->
                if (Modifier.isTransient(field.modifiers)) return
                def anno = field.getAnnotation(Text)
                if (anno) {
                    println "Text: $field"
                    text = this[field.name]
                } else if (anno = field.getAnnotation(Attribute)) {
                    def name = anno.value() ?: field.name
                    if (!spread) {
                        attrs[name] = this[field.name]
                    } else {
                        spreadNodes.add(new SpreadNode(nodeName, Map.of(name, this[field.name])))
                    }
                } else if (anno = field.getAnnotation(Node)) {
                    println "Tag: ${field.type}"
                    def name = anno.value() ?: field.name
                    def node = this[field.name]
                    if (node && ToXml.isInstance(node)) {
                        nodes.add(
                                new NodeStruct(
                                        name: name,
                                        node: node as ToXml,
                                        namespaces: field.getAnnotation(Namespaces)?.toMap(),
                                        spread: field.getAnnotation(Spread) ? true : false
                                )
                        )
                    }
                }
            }

            if (spread) {
                spreadNodes.each {
                    it.toXml(markup, null, null, false)
                }
            }

            if (attrs || text || nodes) {
                markup."$nodeName"(attrs, text) {
                    nodes.each {
                        it.node.toXml(markup, it.name, it.namespaces, it.spread)
                    }
                }
            }

            null
        }
    }
}

@MapConstructor
class XmlExtensions {
    static Map<String, String> toMap(Namespaces nss) {
        if (nss.value().size() <= 0) return null
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
                rs.declareNamespace(field.getAnnotation(Namespaces)?.toMap() ?: [:])
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
