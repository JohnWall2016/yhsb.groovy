package yhsb.base.util

import groovy.transform.PackageScope

abstract class MapField {
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

class ListField<T extends Jsonable> implements Iterable<T> {
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
