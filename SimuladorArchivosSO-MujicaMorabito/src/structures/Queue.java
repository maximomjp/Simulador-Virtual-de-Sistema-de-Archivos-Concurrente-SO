package structures;

public class Queue<T> {
    private Node<T> head; // frente (se desencola aquí)
    private Node<T> tail; // fondo  (se encola aquí)
    private int size;

    public Queue() {
        head = null;
        tail = null;
        size = 0;
    }

    // Encolar
    public void enqueue(T data) {
        Node<T> newNode = new Node<>(data);
        if (tail == null) {
            head = newNode;
            tail = newNode;
        } else {
            tail.next = newNode;
            tail = newNode;
        }
        size++;
    }

    // Desencolar
    public T dequeue() {
        if (isEmpty()) throw new RuntimeException("La cola está vacía");
        T data = head.data;
        head = head.next;
        if (head == null) tail = null; // la cola quedó vacía
        size--;
        return data;
    }

    // Ver el frente sin eliminar
    public T peek() {
        if (isEmpty()) throw new RuntimeException("La cola está vacía");
        return head.data;
    }

    public int size()        { return size; }
    public boolean isEmpty() { return size == 0; }
    public Node<T> getHead() { return head; } // para iterar sin desencolar
}