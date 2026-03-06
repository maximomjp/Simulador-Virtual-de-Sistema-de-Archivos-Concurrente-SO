package structures;

public class Stack<T> {
    private Node<T> top;
    private int size;

    public Stack() {
        top = null;
        size = 0;
    }

    // Apilar
    public void push(T data) {
        Node<T> newNode = new Node<>(data);
        newNode.next = top;
        top = newNode;
        size++;
    }

    // Desapilar
    public T pop() {
        if (isEmpty()) throw new RuntimeException("La pila está vacía");
        T data = top.data;
        top = top.next;
        size--;
        return data;
    }

    // Ver tope sin eliminar
    public T peek() {
        if (isEmpty()) throw new RuntimeException("La pila está vacía");
        return top.data;
    }

    public int size()        { return size; }
    public boolean isEmpty() { return size == 0; }
}