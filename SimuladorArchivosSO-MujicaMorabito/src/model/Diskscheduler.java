package model;

import structures.LinkedList;
import structures.Node;

public class DiskScheduler {

    // =======================================================
    // Enum para las políticas de planificación
    // =======================================================
    public enum Policy {
        FIFO,
        SSTF,
        SCAN,
        CSCAN
    }

    // =======================================================
    // Atributos
    // =======================================================
    private int currentHeadPosition;
    private boolean directionUp;
    private int totalCylinders;

    // =======================================================
    // Constructor
    // =======================================================
    public DiskScheduler(int initialHead, int totalCylinders) {
        this.currentHeadPosition = initialHead;
        this.directionUp = true;
        this.totalCylinders = totalCylinders;
    }

    // =======================================================
    // MÉTODO PRINCIPAL
    // =======================================================
    public LinkedList<Integer> schedule(LinkedList<Integer> requests, Policy policy) {
        switch (policy) {
            case FIFO:  return fifo(requests);
            case SSTF:  return sstf(requests);
            case SCAN:  return scan(requests);
            case CSCAN: return cscan(requests);
            default:    return fifo(requests);
        }
    }

    // =======================================================
    // FIFO
    // =======================================================
    private LinkedList<Integer> fifo(LinkedList<Integer> requests) {
        LinkedList<Integer> result = new LinkedList<>();
        Node<Integer> current = requests.getHead();
        while (current != null) {
            result.addLast(current.data);
            current = current.next;
        }
        return result;
    }

    // =======================================================
    // SSTF - Shortest Seek Time First
    // =======================================================
    private LinkedList<Integer> sstf(LinkedList<Integer> requests) {
        LinkedList<Integer> result  = new LinkedList<>();
        LinkedList<Integer> pending = copyList(requests);
        int head = currentHeadPosition;

        while (!pending.isEmpty()) {
            int closest = findClosest(pending, head);
            result.addLast(closest);
            pending.remove(closest);
            head = closest;
        }

        return result;
    }

    // =======================================================
    // SCAN (Elevator)
    // =======================================================
    private LinkedList<Integer> scan(LinkedList<Integer> requests) {
        LinkedList<Integer> result = new LinkedList<>();
        LinkedList<Integer> left  = new LinkedList<>();
        LinkedList<Integer> right = new LinkedList<>();

        Node<Integer> current = requests.getHead();
        while (current != null) {
            if (current.data < currentHeadPosition) {
                left.addLast(current.data);
            } else {
                right.addLast(current.data);
            }
            current = current.next;
        }

        LinkedList<Integer> sortedLeft  = sortDescending(left);
        LinkedList<Integer> sortedRight = sortAscending(right);

        if (directionUp) {
            appendAll(result, sortedRight);
            // Si hay peticiones abajo, DEBE rebotar en la pared superior
            if (!sortedLeft.isEmpty()) {
                result.addLast(totalCylinders - 1);
            }
            appendAll(result, sortedLeft);
        } else {
            appendAll(result, sortedLeft);
            // Si hay peticiones arriba, DEBE rebotar en la pared inferior
            if (!sortedRight.isEmpty()) {
                result.addLast(0);
            }
            appendAll(result, sortedRight);
        }

        return result;
    }

    // =======================================================
    // C-SCAN (Circular SCAN)
    // =======================================================
    private LinkedList<Integer> cscan(LinkedList<Integer> requests) {
        LinkedList<Integer> result = new LinkedList<>();
        LinkedList<Integer> left  = new LinkedList<>();
        LinkedList<Integer> right = new LinkedList<>();

        Node<Integer> current = requests.getHead();
        while (current != null) {
            if (current.data < currentHeadPosition) {
                left.addLast(current.data);
            } else {
                right.addLast(current.data);
            }
            current = current.next;
        }

        if (directionUp) {
            appendAll(result, sortAscending(right));
            if (!left.isEmpty()) {
                result.addLast(totalCylinders - 1); // Toca la pared superior
                result.addLast(0);                  // Salto circular al inicio
            }
            appendAll(result, sortAscending(left));
        } else {
            appendAll(result, sortDescending(left));
            if (!right.isEmpty()) {
                result.addLast(0);                  // Toca la pared inferior
                result.addLast(totalCylinders - 1); // Salto circular al final
            }
            appendAll(result, sortDescending(right));
        }

        return result;
    }

    // =======================================================
    // CALCULAR MOVIMIENTO TOTAL (Único método necesario)
    // =======================================================
    public int calculateTotalMovement(LinkedList<Integer> order) {
        if (order.isEmpty()) return 0;

        int total = 0;
        int head  = currentHeadPosition;

        Node<Integer> current = order.getHead();
        while (current != null) {
            total += Math.abs(current.data - head);
            head   = current.data;
            current = current.next;
        }

        return total;
    }

    // =======================================================
    // UTILIDADES PRIVADAS
    // =======================================================
    private int findClosest(LinkedList<Integer> list, int head) {
        Node<Integer> current = list.getHead();
        int closest     = current.data;
        int minDistance = Math.abs(current.data - head);
        current = current.next;

        while (current != null) {
            int distance = Math.abs(current.data - head);
            if (distance < minDistance) {
                minDistance = distance;
                closest     = current.data;
            }
            current = current.next;
        }
        return closest;
    }

    private LinkedList<Integer> copyList(LinkedList<Integer> original) {
        LinkedList<Integer> copy = new LinkedList<>();
        Node<Integer> current = original.getHead();
        while (current != null) {
            copy.addLast(current.data);
            current = current.next;
        }
        return copy;
    }

    private LinkedList<Integer> sortAscending(LinkedList<Integer> list) {
        LinkedList<Integer> sorted = new LinkedList<>();
        LinkedList<Integer> temp   = copyList(list);

        while (!temp.isEmpty()) {
            int min = findMin(temp);
            sorted.addLast(min);
            temp.remove(min);
        }
        return sorted;
    }

    private LinkedList<Integer> sortDescending(LinkedList<Integer> list) {
        LinkedList<Integer> sorted = new LinkedList<>();
        LinkedList<Integer> temp   = copyList(list);

        while (!temp.isEmpty()) {
            int max = findMax(temp);
            sorted.addLast(max);
            temp.remove(max);
        }
        return sorted;
    }

    private int findMin(LinkedList<Integer> list) {
        Node<Integer> current = list.getHead();
        int min = current.data;
        current = current.next;

        while (current != null) {
            if (current.data < min) min = current.data;
            current = current.next;
        }
        return min;
    }

    private int findMax(LinkedList<Integer> list) {
        Node<Integer> current = list.getHead();
        int max = current.data;
        current = current.next;

        while (current != null) {
            if (current.data > max) max = current.data;
            current = current.next;
        }
        return max;
    }

    private void appendAll(LinkedList<Integer> dest, LinkedList<Integer> source) {
        Node<Integer> current = source.getHead();
        while (current != null) {
            dest.addLast(current.data);
            current = current.next;
        }
    }

    // =======================================================
    // GETTERS y SETTERS
    // =======================================================
    public int  getCurrentHeadPosition()         { return currentHeadPosition; }
    public void setCurrentHeadPosition(int pos)  { this.currentHeadPosition = pos; }
    public boolean isDirectionUp()               { return directionUp; }
    public void setDirectionUp(boolean up)       { this.directionUp = up; }
    public int  getTotalCylinders()              { return totalCylinders; }
    public void setTotalCylinders(int total)     { this.totalCylinders = total; }

    // =======================================================
    // toString para el Log de Eventos
    // =======================================================
    public String orderToString(LinkedList<Integer> order) {
        StringBuilder sb = new StringBuilder();
        Node<Integer> current = order.getHead();
        while (current != null) {
            sb.append(current.data);
            if (current.next != null) sb.append(" → ");
            current = current.next;
        }
        return sb.toString();
    }
}