package structures;

public class LinkedList<T> {
    private Node<T> head;
    private Node<T> tail;
    private int size;

    public LinkedList() {
        head = null;
        tail = null;
        size = 0;
    }

    // Agregar al final
    public void addLast(T data) {
        Node<T> newNode = new Node<>(data);
        if (tail == null) {
            head = newNode;
            tail = newNode;
        } else {
            newNode.prev = tail;
            tail.next = newNode;
            tail = newNode;
        }
        size++;
    }

    // Agregar al inicio
    public void addFirst(T data) {
        Node<T> newNode = new Node<>(data);
        if (head == null) {
            head = newNode;
            tail = newNode;
        } else {
            newNode.next = head;
            head.prev = newNode;
            head = newNode;
        }
        size++;
    }

    // Eliminar por valor
    public boolean remove(T data) {
        Node<T> current = head;
        while (current != null) {
            if (current.data.equals(data)) {
                if (current.prev != null) current.prev.next = current.next;
                else head = current.next; // era el head

                if (current.next != null) current.next.prev = current.prev;
                else tail = current.prev; // era el tail

                size--;
                return true;
            }
            current = current.next;
        }
        return false; // no encontrado
    }

    // Obtener elemento por índice
    public T get(int index) {
        if (index < 0 || index >= size) throw new IndexOutOfBoundsException("Índice inválido: " + index);
        Node<T> current = head;
        for (int i = 0; i < index; i++) {
            current = current.next;
        }
        return current.data;
    }

    // Buscar si existe
    public boolean contains(T data) {
        Node<T> current = head;
        while (current != null) {
            if (current.data.equals(data)) return true;
            current = current.next;
        }
        return false;
    }

    public int size()       { return size; }
    public boolean isEmpty(){ return size == 0; }
    public T getFirst()     { return head != null ? head.data : null; }
    public T getLast()      { return tail != null ? tail.data : null; }
    public Node<T> getHead(){ return head; } // para iterar externamente
}