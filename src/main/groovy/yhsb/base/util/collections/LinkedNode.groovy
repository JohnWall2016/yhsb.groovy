package yhsb.base.util.collections

class LinkedNode<T> implements Iterable<T> {

    private T data

    private LinkedNode<T> next

    private LinkedNode(T data, LinkedNode<T> next = null) {
        this.data = data
        this.next = next
    }

    LinkedNode<T> getNext() {
        next
    }

    T getData() {
        data
    }

    @Override
    Iterator<T> iterator() {
        new LinkedNodeIter<T>(this)
    }

    private static class LinkedNodeIter<T> implements Iterator<T> {
        private LinkedNode<T> cursor

        LinkedNodeIter(LinkedNode<T> current) {
            cursor = current
        }

        @Override
        boolean hasNext() {
            cursor != null
        }

        @Override
        T next() {
            if (cursor == null) {
                throw new NoSuchElementException('No element exists')
            }
            T data = cursor.data
            cursor = cursor.next
            data
        }
    }

    static <T> LinkedNode<T> of(T e1) {
        new LinkedNode<T>(e1)
    }

    static <T> LinkedNode<T> of(T e1, T e2) {
        new LinkedNode<T>(e1, new LinkedNode<T>(e2))
    }

    static <T> LinkedNode<T> cons(T data, LinkedNode<T> next) {
        new LinkedNode<T>(data, next)
    }

    protected LinkedNode() {
        this.data = null
        this.next = null
    }

    private static class EmptyLinkedNode<T> extends LinkedNode<T> {
        @Override
        Iterator<T> iterator() {
            new LinkedNodeIter<T>(null)
        }
    }

    private static final LinkedNode<?> EMPTY = new EmptyLinkedNode<>()

    static <T> LinkedNode<T> empty() {
        LinkedNode<T> t = (LinkedNode<T>)EMPTY
        return t
    }

    static <T> LinkedNode<T> flatten(Collection<LinkedNode<T>> col) {
        if (!col) return empty()

        var iter = col.iterator()
        var first = iter.next()
        var cur = first
        while (iter.hasNext()) {
            while (cur.next) {
                cur = cur.next
            }
            var next = iter.next()
            cur.next = next
            cur = next
        }

        first
    }

    @Override
    String toString() {
        '[' + join(', ') + ']'
    }
}
