/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

import structures.LinkedList;
import structures.Node;

/**
 *
 * @author alemo
 */
public class Diskscheduler {
    
 
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
    private boolean directionUp; // true = hacia arriba (ascendente), false = descendente
    private int totalCylinders;  // número total de cilindros del disco
 
    // =======================================================
    // Constructor
    // =======================================================
    public Diskscheduler(int initialHead, int totalCylinders) {
        this.currentHeadPosition = initialHead;
        this.directionUp = true; // por defecto hacia arriba
        this.totalCylinders = totalCylinders;
    }
 
    // =======================================================
    // MÉTODO PRINCIPAL: Ejecutar planificación
    // Recibe la cola de solicitudes (posiciones de disco)
    // Retorna una LinkedList con el orden de atención
    // =======================================================
    public LinkedList<Integer> schedule(LinkedList<Integer> requests, Policy policy) {
        switch (policy) {
            case FIFO:
                return fifo(requests);
            case SSTF:
                return sstf(requests);
            case SCAN:
                return scan(requests);
            case CSCAN:
                return cscan(requests);
            default:
                return fifo(requests);
        }
    }
 
    // =======================================================
    // FIFO - First In, First Out
    // Las solicitudes se atienden en el orden en que llegaron
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
    // Se atiende la solicitud más cercana al cabezal actual
    // =======================================================
    private LinkedList<Integer> sstf(LinkedList<Integer> requests) {
        LinkedList<Integer> result = new LinkedList<>();
 
        // Copiar las solicitudes para no modificar la original
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
    // SCAN (Elevator) - El cabezal se mueve en una dirección
    // atendiendo solicitudes, y al llegar al extremo cambia
    // de dirección
    // =======================================================
    private LinkedList<Integer> scan(LinkedList<Integer> requests) {
        LinkedList<Integer> result = new LinkedList<>();
 
        // Separar solicitudes en dos grupos según la posición del cabezal
        LinkedList<Integer> left = new LinkedList<>();   // menores que head
        LinkedList<Integer> right = new LinkedList<>();  // mayores o iguales que head
 
        Node<Integer> current = requests.getHead();
        while (current != null) {
            if (current.data < currentHeadPosition) {
                left.addLast(current.data);
            } else {
                right.addLast(current.data);
            }
            current = current.next;
        }
 
        // Ordenar ambas listas
        LinkedList<Integer> sortedLeft = sortDescending(left);   // de mayor a menor
        LinkedList<Integer> sortedRight = sortAscending(right);  // de menor a mayor
 
        if (directionUp) {
            // Primero atender las de la derecha (ascendente)
            appendAll(result, sortedRight);
            // Luego las de la izquierda (descendente)
            appendAll(result, sortedLeft);
        } else {
            // Primero atender las de la izquierda (descendente)
            appendAll(result, sortedLeft);
            // Luego las de la derecha (ascendente)
            appendAll(result, sortedRight);
        }
 
        return result;
    }
 
    // =======================================================
    // C-SCAN (Circular SCAN) - El cabezal se mueve en una
    // dirección, y al llegar al extremo salta al otro extremo
    // y continúa en la misma dirección
    // =======================================================
    private LinkedList<Integer> cscan(LinkedList<Integer> requests) {
        LinkedList<Integer> result = new LinkedList<>();
 
        // Separar solicitudes
        LinkedList<Integer> left = new LinkedList<>();
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
            // Atender las de la derecha en orden ascendente
            LinkedList<Integer> sortedRight = sortAscending(right);
            appendAll(result, sortedRight);
            // Saltar al inicio: atender las de la izquierda en orden ascendente
            LinkedList<Integer> sortedLeft = sortAscending(left);
            appendAll(result, sortedLeft);
        } else {
            // Atender las de la izquierda en orden descendente
            LinkedList<Integer> sortedLeft = sortDescending(left);
            appendAll(result, sortedLeft);
            // Saltar al final: atender las de la derecha en orden descendente
            LinkedList<Integer> sortedRight = sortDescending(right);
            appendAll(result, sortedRight);
        }
 
        return result;
    }
 
    // =======================================================
    // UTILIDADES PRIVADAS
    // =======================================================
 
    // Encontrar el valor más cercano al cabezal en la lista
    private int findClosest(LinkedList<Integer> list, int head) {
        Node<Integer> current = list.getHead();
        int closest = current.data;
        int minDistance = Math.abs(current.data - head);
        current = current.next;
 
        while (current != null) {
            int distance = Math.abs(current.data - head);
            if (distance < minDistance) {
                minDistance = distance;
                closest = current.data;
            }
            current = current.next;
        }
 
        return closest;
    }
 
    // Copiar una LinkedList de enteros
    private LinkedList<Integer> copyList(LinkedList<Integer> original) {
        LinkedList<Integer> copy = new LinkedList<>();
        Node<Integer> current = original.getHead();
        while (current != null) {
            copy.addLast(current.data);
            current = current.next;
        }
        return copy;
    }
 
    // Ordenar ascendente (Selection Sort sobre LinkedList)
    private LinkedList<Integer> sortAscending(LinkedList<Integer> list) {
        LinkedList<Integer> sorted = new LinkedList<>();
        LinkedList<Integer> temp = copyList(list);
 
        while (!temp.isEmpty()) {
            int min = findMin(temp);
            sorted.addLast(min);
            temp.remove(min);
        }
 
        return sorted;
    }
 
    // Ordenar descendente
    private LinkedList<Integer> sortDescending(LinkedList<Integer> list) {
        LinkedList<Integer> sorted = new LinkedList<>();
        LinkedList<Integer> temp = copyList(list);
 
        while (!temp.isEmpty()) {
            int max = findMax(temp);
            sorted.addLast(max);
            temp.remove(max);
        }
 
        return sorted;
    }
 
    // Encontrar el mínimo en la lista
    private int findMin(LinkedList<Integer> list) {
        Node<Integer> current = list.getHead();
        int min = current.data;
        current = current.next;
 
        while (current != null) {
            if (current.data < min) {
                min = current.data;
            }
            current = current.next;
        }
 
        return min;
    }
 
    // Encontrar el máximo en la lista
    private int findMax(LinkedList<Integer> list) {
        Node<Integer> current = list.getHead();
        int max = current.data;
        current = current.next;
 
        while (current != null) {
            if (current.data > max) {
                max = current.data;
            }
            current = current.next;
        }
 
        return max;
    }
 
    // Agregar todos los elementos de source al final de dest
    private void appendAll(LinkedList<Integer> dest, LinkedList<Integer> source) {
        Node<Integer> current = source.getHead();
        while (current != null) {
            dest.addLast(current.data);
            current = current.next;
        }
    }
 
    // =======================================================
    // Calcular el movimiento total del cabezal
    // =======================================================
    public int calculateTotalMovement(LinkedList<Integer> order) {
        if (order.isEmpty()) return 0;
 
        int total = 0;
        int head = currentHeadPosition;
 
        Node<Integer> current = order.getHead();
        while (current != null) {
            total += Math.abs(current.data - head);
            head = current.data;
            current = current.next;
        }
 
        return total;
    }
 
    // =======================================================
    // GETTERS y SETTERS
    // =======================================================
    public int getCurrentHeadPosition()            { return currentHeadPosition; }
    public void setCurrentHeadPosition(int pos)    { this.currentHeadPosition = pos; }
    public boolean isDirectionUp()                 { return directionUp; }
    public void setDirectionUp(boolean up)         { this.directionUp = up; }
    public int getTotalCylinders()                 { return totalCylinders; }
    public void setTotalCylinders(int total)       { this.totalCylinders = total; }
 
    // =======================================================
    // toString para debug
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
