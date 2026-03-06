package model;

import structures.Queue;
import structures.LinkedList;
import structures.Node;

public class ProcessQueue {

    // =======================================================
    // Colas por estado
    // =======================================================
    private Queue<PCB>      readyQueue;      // procesos listos
    private Queue<PCB>      blockedQueue;    // procesos bloqueados por lock
    private LinkedList<PCB> runningList;     // procesos ejecutándose (puede ser más de uno)
    private LinkedList<PCB> terminatedList;  // procesos terminados (historial)
    private LinkedList<PCB> allProcesses;    // registro de todos los procesos creados

    // =======================================================
    // Constructor
    // =======================================================
    public ProcessQueue() {
        readyQueue     = new Queue<>();
        blockedQueue   = new Queue<>();
        runningList    = new LinkedList<>();
        terminatedList = new LinkedList<>();
        allProcesses   = new LinkedList<>();
    }

    // =======================================================
    // ADMITIR proceso (NEW → READY)
    // =======================================================
    public void admitProcess(PCB process) {
        process.admit();
        readyQueue.enqueue(process);
        allProcesses.addLast(process);
        log("ADMIT: " + process.getName() + " admitido en cola de listos.");
    }

    // =======================================================
    // DESPACHAR siguiente proceso de la cola de listos (READY → RUNNING)
    // =======================================================
    public PCB dispatchNext() {
        if (readyQueue.isEmpty()) {
            log("DISPATCH: Cola de listos vacía, no hay proceso para despachar.");
            return null;
        }
        PCB process = readyQueue.dequeue();
        process.start();
        runningList.addLast(process);
        log("DISPATCH: " + process.getName() + " pasó a RUNNING.");
        return process;
    }

    // =======================================================
    // BLOQUEAR proceso (RUNNING → BLOCKED)
    // =======================================================
    public void blockProcess(PCB process, String reason) {
        if (runningList.remove(process)) {
            process.block(reason);
            blockedQueue.enqueue(process);
            log("BLOCK: " + process.getName() + " bloqueado. Razón: " + reason);
        } else {
            log("BLOCK ERROR: " + process.getName() + " no estaba en RUNNING.");
        }
    }

    // =======================================================
    // DESBLOQUEAR proceso (BLOCKED → READY)
    // =======================================================
    public void unblockProcess(PCB process) {
        // Buscar y quitar de blockedQueue reconstruyendo la cola
        Queue<PCB> temp = new Queue<>();
        boolean found = false;

        while (!blockedQueue.isEmpty()) {
            PCB p = blockedQueue.dequeue();
            if (p.getPid() == process.getPid()) {
                found = true;
            } else {
                temp.enqueue(p);
            }
        }

        // Restaurar los que no eran el buscado
        while (!temp.isEmpty()) {
            blockedQueue.enqueue(temp.dequeue());
        }

        if (found) {
            process.unblock();
            readyQueue.enqueue(process);
            log("UNBLOCK: " + process.getName() + " pasó a READY.");
        } else {
            log("UNBLOCK ERROR: " + process.getName() + " no estaba en BLOCKED.");
        }
    }

    // =======================================================
    // TERMINAR proceso (RUNNING → TERMINATED)
    // =======================================================
    public void terminateProcess(PCB process) {
        if (runningList.remove(process)) {
            process.terminate();
            terminatedList.addLast(process);
            log("TERMINATE: " + process.getName() + " terminado."
                + " | Turnaround: " + process.getTurnaroundTime() + "ms");
        } else {
            log("TERMINATE ERROR: " + process.getName() + " no estaba en RUNNING.");
        }
    }

    // =======================================================
    // BUSCAR proceso por PID en todas las colas
    // =======================================================
    public PCB findByPid(int pid) {
        Node<PCB> current = allProcesses.getHead();
        while (current != null) {
            if (current.data.getPid() == pid) return current.data;
            current = current.next;
        }
        return null;
    }

    // =======================================================
    // ESTADO GENERAL
    // =======================================================
    public boolean hasReadyProcesses()   { return !readyQueue.isEmpty(); }
    public boolean hasBlockedProcesses() { return !blockedQueue.isEmpty(); }
    public boolean hasRunningProcesses() { return !runningList.isEmpty(); }

    public int getReadyCount()      { return readyQueue.size(); }
    public int getBlockedCount()    { return blockedQueue.size(); }
    public int getRunningCount()    { return runningList.size(); }
    public int getTerminatedCount() { return terminatedList.size(); }
    public int getTotalCount()      { return allProcesses.size(); }

    // =======================================================
    // GETTERS para la GUI (JTable de procesos)
    // =======================================================
    public Queue<PCB>      getReadyQueue()     { return readyQueue; }
    public Queue<PCB>      getBlockedQueue()   { return blockedQueue; }
    public LinkedList<PCB> getRunningList()    { return runningList; }
    public LinkedList<PCB> getTerminatedList() { return terminatedList; }
    public LinkedList<PCB> getAllProcesses()   { return allProcesses; }

    // =======================================================
    // RESUMEN para log y GUI
    // =======================================================
    public String getSummary() {
        return String.format(
            "Procesos → Ready: %d | Running: %d | Blocked: %d | Terminated: %d | Total: %d",
            getReadyCount(), getRunningCount(),
            getBlockedCount(), getTerminatedCount(), getTotalCount()
        );
    }

    // =======================================================
    // RESET (para reiniciar la simulación)
    // =======================================================
    public void reset() {
        readyQueue     = new Queue<>();
        blockedQueue   = new Queue<>();
        runningList    = new LinkedList<>();
        terminatedList = new LinkedList<>();
        allProcesses   = new LinkedList<>();
        PCB.resetIdCounter();
        log("RESET: Cola de procesos reiniciada.");
    }

    // =======================================================
    // LOG interno
    // =======================================================
    private void log(String message) {
        System.out.println("[ProcessQueue] " + message);
    }
}