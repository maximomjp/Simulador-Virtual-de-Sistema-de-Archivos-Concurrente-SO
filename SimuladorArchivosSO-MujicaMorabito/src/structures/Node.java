package structures;

public class Node<T> {
    public T data;
    public Node<T> next;
    public Node<T> prev; // útil para LinkedList doblemente enlazada

    public Node(T data) {
        this.data = data;
        this.next = null;
        this.prev = null;
    }
}