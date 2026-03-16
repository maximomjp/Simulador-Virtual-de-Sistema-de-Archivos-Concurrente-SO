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
public class Journal {
    
 
    // =======================================================
    // Estados de una transacción
    // =======================================================
    public enum TransactionStatus {
        PENDING,    // operación registrada, aún no ejecutada completamente
        COMMITTED,  // operación ejecutada y confirmada
        UNDONE      // operación deshecha tras fallo
    }
 
    // =======================================================
    // Registro de una transacción
    // =======================================================
    public static class TransactionEntry {
        private static int idCounter = 1;
 
        private int transactionId;
        private String operation;           // "CREATE", "DELETE"
        private String targetPath;          // ruta del archivo afectado
        private int blocks;                 // bloques involucrados (para CREATE)
        private int firstBlock;             // primer bloque asignado (para UNDO de CREATE)
        private String owner;               // dueño del archivo
        private TransactionStatus status;   // estado actual
        private long timestamp;             // momento del registro
 
        public TransactionEntry(String operation, String targetPath, String owner, int blocks) {
            this.transactionId = idCounter++;
            this.operation = operation;
            this.targetPath = targetPath;
            this.owner = owner;
            this.blocks = blocks;
            this.firstBlock = -1;
            this.status = TransactionStatus.PENDING;
            this.timestamp = System.currentTimeMillis();
        }
 
        // Getters
        public int getTransactionId()       { return transactionId; }
        public String getOperation()        { return operation; }
        public String getTargetPath()       { return targetPath; }
        public int getBlocks()              { return blocks; }
        public int getFirstBlock()          { return firstBlock; }
        public String getOwner()            { return owner; }
        public TransactionStatus getStatus(){ return status; }
        public long getTimestamp()           { return timestamp; }
 
        // Setters
        public void setStatus(TransactionStatus status) { this.status = status; }
        public void setFirstBlock(int firstBlock)       { this.firstBlock = firstBlock; }
 
        // Reset contador
        public static void resetIdCounter() { idCounter = 1; }
 
        @Override
        public String toString() {
            return String.format("[TX-%d] %s %s | Owner: %s | Blocks: %d | FirstBlock: %d | Status: %s",
                    transactionId, operation, targetPath, owner, blocks, firstBlock, status);
        }
    }
 
    // =======================================================
    // Atributos
    // =======================================================
    private LinkedList<TransactionEntry> entries;  // todas las transacciones
    private boolean crashSimulated;                // flag para simular fallo
 
    // =======================================================
    // Constructor
    // =======================================================
    public Journal() {
        entries = new LinkedList<>();
        crashSimulated = false;
    }
 
    // =======================================================
    // REGISTRAR operación como PENDING (antes de ejecutar)
    // =======================================================
    public TransactionEntry beginTransaction(String operation, String targetPath, String owner, int blocks) {
        TransactionEntry entry = new TransactionEntry(operation, targetPath, owner, blocks);
        entries.addLast(entry);
        log("BEGIN TX-" + entry.getTransactionId() + ": " + operation + " " + targetPath + " → PENDING");
        return entry;
    }
 
    // =======================================================
    // CONFIRMAR operación (después de ejecutar exitosamente)
    // =======================================================
    public void commitTransaction(TransactionEntry entry) {
        if (crashSimulated) {
            log("CRASH SIMULADO: TX-" + entry.getTransactionId() + " NO fue confirmada (commit abortado).");
            return; // no se hace el commit, queda PENDING
        }
        entry.setStatus(TransactionStatus.COMMITTED);
        log("COMMIT TX-" + entry.getTransactionId() + ": " + entry.getOperation() + " " + entry.getTargetPath() + " → COMMITTED");
    }
 
    // =======================================================
    // SIMULAR FALLO - activa el flag de crash
    // La próxima operación quedará PENDING sin commit
    // =======================================================
    public void simulateCrash() {
        crashSimulated = true;
        log("CRASH ACTIVADO: El próximo commit será ignorado.");
    }
 
    // Desactivar simulación de crash
    public void clearCrash() {
        crashSimulated = false;
        log("CRASH DESACTIVADO.");
    }
 
    public boolean isCrashSimulated() {
        return crashSimulated;
    }
 
    // =======================================================
    // RECOVERY - Revisar journal y deshacer operaciones PENDING
    // Retorna la lista de transacciones que fueron deshechas
    // =======================================================
    public LinkedList<TransactionEntry> recover(FileSystem fileSystem) {
        LinkedList<TransactionEntry> undoneTransactions = new LinkedList<>();
 
        Node<TransactionEntry> current = entries.getHead();
        while (current != null) {
            TransactionEntry entry = current.data;
 
            if (entry.getStatus() == TransactionStatus.PENDING) {
                // Deshacer la operación
                undoTransaction(entry, fileSystem);
                entry.setStatus(TransactionStatus.UNDONE);
                undoneTransactions.addLast(entry);
                log("UNDO TX-" + entry.getTransactionId() + ": " + entry.getOperation()
                        + " " + entry.getTargetPath() + " → UNDONE");
            }
 
            current = current.next;
        }
 
        // Limpiar el flag de crash después del recovery
        crashSimulated = false;
 
        if (undoneTransactions.isEmpty()) {
            log("RECOVERY: No hay transacciones pendientes. Sistema consistente.");
        } else {
            log("RECOVERY: " + undoneTransactions.size() + " transacción(es) deshecha(s).");
        }
 
        return undoneTransactions;
    }
 
    // =======================================================
    // UNDO de una transacción específica
    // =======================================================
    private void undoTransaction(TransactionEntry entry, FileSystem fileSystem) {
        switch (entry.getOperation()) {
            case "CREATE":
                // Deshacer CREATE: liberar bloques asignados
                if (entry.getFirstBlock() != -1) {
                    fileSystem.getDisk().freeBlocks(entry.getFirstBlock());
                    log("UNDO CREATE: Bloques liberados desde bloque " + entry.getFirstBlock());
                }
                // Eliminar el archivo del árbol si fue agregado
                FileEntry file = fileSystem.getEntryByPath(entry.getTargetPath());
                if (file != null) {
                    // Obtener el path del padre y eliminar el hijo
                    String parentPath = getParentPath(entry.getTargetPath());
                    FileEntry parent = fileSystem.getEntryByPath(parentPath);
                    if (parent != null) {
                        parent.removeChild(file);
                        fileSystem.getAllFiles().remove(file);
                        log("UNDO CREATE: Archivo '" + entry.getTargetPath() + "' removido del árbol.");
                    }
                }
                break;
 
            case "DELETE":
                // Deshacer DELETE es más complejo - en este simulador
                // el journal registra la info para poder restaurar
                log("UNDO DELETE: Restauración de '" + entry.getTargetPath() + "' requiere datos previos.");
                break;
 
            default:
                log("UNDO: Operación '" + entry.getOperation() + "' no tiene undo definido.");
                break;
        }
    }
 
    // Utilidad: obtener path del padre
    private String getParentPath(String path) {
        int lastSlash = path.lastIndexOf('/');
        if (lastSlash <= 0) return "/";
        return path.substring(0, lastSlash);
    }
 
    // =======================================================
    // CONSULTAS para la GUI
    // =======================================================
    public LinkedList<TransactionEntry> getAllEntries()  { return entries; }
 
    public LinkedList<TransactionEntry> getPendingEntries() {
        LinkedList<TransactionEntry> pending = new LinkedList<>();
        Node<TransactionEntry> current = entries.getHead();
        while (current != null) {
            if (current.data.getStatus() == TransactionStatus.PENDING) {
                pending.addLast(current.data);
            }
            current = current.next;
        }
        return pending;
    }
 
    public int getTotalEntries()    { return entries.size(); }
 
    public String getSummary() {
        int pending = 0, committed = 0, undone = 0;
        Node<TransactionEntry> current = entries.getHead();
        while (current != null) {
            switch (current.data.getStatus()) {
                case PENDING:   pending++;   break;
                case COMMITTED: committed++; break;
                case UNDONE:    undone++;     break;
            }
            current = current.next;
        }
        return String.format("Journal → Total: %d | Pending: %d | Committed: %d | Undone: %d",
                entries.size(), pending, committed, undone);
    }
 
    // =======================================================
    // RESET
    // =======================================================
    public void reset() {
        entries = new LinkedList<>();
        crashSimulated = false;
        TransactionEntry.resetIdCounter();
        log("RESET: Journal limpiado.");
    }
 
    // =======================================================
    // LOG
    // =======================================================
    private void log(String message) {
        System.out.println("[Journal] " + message);
    }
}
