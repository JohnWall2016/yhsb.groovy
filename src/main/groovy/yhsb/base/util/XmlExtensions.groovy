package yhsb.base.util

import groovy.transform.MapConstructor
import groovy.transform.PackageScope
import groovy.xml.MarkupBuilder
import groovy.xml.slurpersupport.GPathResult
import yhsb.base.util.reflect.GenericClass

import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target
import java.lang.reflect.Field
import java.lang.reflect.Modifier
import java.lang.reflect.ParameterizedType
import java.lang.reflect.TypeVariable

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
@Target(ElementType.FIELD)
@interface Attribute {
    String value() default ''
}

@Retention(RetentionPolicy.RUNTIME)
@Target([ElementType.FIELD])
@interface AttrNode {
    String name()

    String attr() default ''
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
}

class SingleNode implements ToXml {
    String name
    Map attributes

    SingleNode(String name, Map attributes) {
        this.name = name
        this.attributes = attributes
    }

    void toXml(MarkupBuilder markup,
               String nodeName,
               Map<String, String> namespaces
    ) {
        nodeName ?= name
        markup."$nodeName"(attributes)
    }
}

trait ToXml {
    String toXml() {
        def writer = new StringWriter()
        def markup = new MarkupBuilder(writer)

        toXml(markup, null, null)
        writer.toString()
    }

    void toXml(
            MarkupBuilder markup,
            String nodeName,
            Map<String, String> namespaces
    ) {
        this.class.with {
            // println it
            nodeName ?= getAnnotation(Node)?.value() ?: it.name
            namespaces ?= getAnnotation(Namespaces)?.toMap() ?: [:]

            String text = null

            def attrs = [:]
            namespaces.each {
                def v = it.key.isEmpty() ? 'xmlns' : "xmlns:${it.key}"
                attrs[v] = it.value
            }

            List<NodeStruct> nodes = []

            xmlFields.each { field ->
                // println field
                if (Modifier.isTransient(field.modifiers)) return
                def anno = field.getAnnotation(Text)
                if (anno) {
                    text = this[field.name]
                } else if (anno = field.getAnnotation(Attribute)) {
                    def name = anno.value() ?: field.name
                    attrs[name] = this[field.name]
                } else if (anno = field.getAnnotation(Node)) {
                    def name = anno.value() ?: field.name
                    def node = this[field.name]
                    if (node) {
                        if (field.type == List) {
                            def list = node as List<?>
                            list.each {
                                if (ToXml.isInstance(it)) {
                                    nodes.add(
                                            new NodeStruct(
                                                    name: name,
                                                    node: it as ToXml,
                                                    namespaces: field.getAnnotation(Namespaces)?.toMap()
                                            )
                                    )
                                }
                            }
                        } else if (ToXml.isInstance(node)) {
                            nodes.add(
                                    new NodeStruct(
                                            name: name,
                                            node: node as ToXml,
                                            namespaces: field.getAnnotation(Namespaces)?.toMap()
                                    )
                            )
                        }
                    }
                } else if (anno = field.getAnnotation(AttrNode)) {
                    def name = anno.name()
                    def attr = anno.attr() ?: field.name
                    // println "AttrNode $name $attr ${this[field.name]}"
                    nodes.add(
                            new NodeStruct(
                                    name: name,
                                    node: new SingleNode(name, Map.of(attr, this[field.name])),
                                    namespaces: field.getAnnotation(Namespaces)?.toMap()
                            )
                    )
                }
            }

            if (attrs || text || nodes) {
                markup."$nodeName"(attrs, text) {
                    nodes.each {
                        it.node.toXml(markup, it.name, it.namespaces)
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

    private static <T> void updateField(T object, Field field, String value) {
        def type = field.type
        if (Class<?>.isInstance(type)
                && (MapField.isAssignableFrom(type as Class<?>))) {
            def val = (type as Class<?>).getConstructor().newInstance() as MapField
            val.@value = value
            object[field.name] = val
        } else if (type == int || type == Integer) {
            if (value) {
                object[field.name] = value.toInteger()
            }
        } else {
            object[field.name] = value
        }
    }

    private static <T> void updateField(T object, Field field, GPathResult rs, GenericClass<T> genericClass) {
        if (field.type == List) {
            def list = []
            def genericType = field.genericType
            if (genericType && ParameterizedType.isInstance(genericType)) {
                def paramType = genericType as ParameterizedType
                if (paramType.actualTypeArguments.size() > 0) {
                    def actualClass = null
                    def actualType = paramType.actualTypeArguments[0]
                    if (Class.isInstance(actualType)) {
                        actualClass = actualType as Class
                    } else if (TypeVariable.isInstance(actualType)) {
                        actualClass = genericClass.getActualType(actualType as TypeVariable)
                    }
                    if (actualClass) {
                        def childrenClass = genericClass.createGenericClass(actualClass)
                        rs.each {
                            if (GPathResult.isInstance(it)) {
                                list.add((it as GPathResult).toObject(childrenClass))
                            }
                        }
                    }
                }
            }
            object[field.name] = list
        } else {
            object[field.name] = rs.toObject(
                    genericClass.createGenericClass(field.genericType)
            )
        }
    }

    static <T> T toObject(GPathResult rs, GenericClass<T> genericClass, T object = null) {
        if (!object) object = genericClass.newInstance()

        object.class.with {
            rs.declareNamespace(getAnnotation(Namespaces)?.toMap() ?: [:])
            declaredFields.each { field ->
                rs.declareNamespace(field.getAnnotation(Namespaces)?.toMap() ?: [:])
                def anno = field.getAnnotation(Text)
                if (anno) {
                    object[field.name] = rs.text()
                } else if (anno = field.getAnnotation(Attribute)) {
                    def name = anno.value() ?: field.name
                    updateField(object, field, rs["@$name"] as String)
                } else if (anno = field.getAnnotation(AttrNode)) {
                    def name = anno.name()
                    def attr = anno.attr() ?: field.name
                    updateField(object, field, rs[name]["@$attr"] as String)
                } else if (anno = field.getAnnotation(Node)) {
                    def name = anno.value() ?: field.name
                    def node = rs[name]
                    if (node && GPathResult.isInstance(node)) {
                        updateField(object, field, (node as GPathResult), genericClass)
                    }
                }
            }
        }

        object
    }

    static <T> List<Field> getXmlFields(Class<T> self) {
        def list = self.superclass ? self.superclass.xmlFields : []

        self.declaredFields.each { field ->
            if (Modifier.isTransient(field.modifiers)) return
            if (field.getAnnotation(Text) ||
                    field.getAnnotation(Attribute) ||
                    field.getAnnotation(Node) ||
                    field.getAnnotation(AttrNode))
                list.add(field)
        }

        list
    }
}
