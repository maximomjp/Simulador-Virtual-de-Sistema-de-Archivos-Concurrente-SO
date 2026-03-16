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
public class LockManager {
    
 
    // =======================================================
    // Tipos de lock
    // =======================================================
    public enum LockType {
        SHARED,     // lectura - múltiples procesos pueden tener este lock
        EXCLUSIVE   // escritura - solo un proceso puede tener este lock
    }
 
    // =======================================================
    // Registro de un lock activo
    // =======================================================
    public static class LockEntry {
        private String filePath;    // ruta del archivo bloqueado
        private int pid;            // PID del proceso que tiene el lock
        private LockType type;      // tipo de lock
 
        public LockEntry(String filePath, int pid, LockType type) {
            this.filePath = filePath;
            this.pid = pid;
            this.type = type;
        }
 
        public String getFilePath() { return filePath; }
        public int getPid()         { return pid; }
        public LockType getType()   { return type; }
 
        @Override
        public String toString() {
            return "[Lock] " + filePath + " | PID=" + pid + " | " + type;
        }
    }
 
    // =======================================================
    // Atributos
    // =======================================================
    private LinkedList<LockEntry> activeLocks; // locks activos en el sistema
 
    // =======================================================
    // Constructor
    // =======================================================
    public LockManager() {
        activeLocks = new LinkedList<>();
    }
 
    // =======================================================
    // ADQUIRIR LOCK
    // Retorna true si se pudo adquirir, false si está bloqueado
    // =======================================================
    public boolean acquireLock(String filePath, int pid, LockType requestedType) {
 
        if (requestedType == LockType.SHARED) {
            // SHARED: se puede adquirir si NO hay un lock EXCLUSIVE de otro proceso
            if (hasExclusiveLock(filePath, pid)) {
                log("LOCK DENIED: " + filePath + " tiene lock exclusivo. PID " + pid + " no puede leer.");
                return false;
            }
            // Conceder lock compartido
            activeLocks.addLast(new LockEntry(filePath, pid, LockType.SHARED));
            log("LOCK ACQUIRED: PID " + pid + " obtuvo lock SHARED sobre " + filePath);
            return true;
 
        } else {
            // EXCLUSIVE: se puede adquirir si NO hay NINGÚN lock de otro proceso
            if (hasAnyLock(filePath, pid)) {
                log("LOCK DENIED: " + filePath + " tiene locks activos. PID " + pid + " no puede escribir.");
                return false;
            }
            // Conceder lock exclusivo
            activeLocks.addLast(new LockEntry(filePath, pid, LockType.EXCLUSIVE));
            log("LOCK ACQUIRED: PID " + pid + " obtuvo lock EXCLUSIVE sobre " + filePath);
            return true;
        }
    }
 
    // =======================================================
    // LIBERAR LOCK de un proceso sobre un archivo
    // =======================================================
    public boolean releaseLock(String filePath, int pid) {
        Node<LockEntry> current = activeLocks.getHead();
        while (current != null) {
            LockEntry entry = current.data;
            if (entry.getFilePath().equals(filePath) && entry.getPid() == pid) {
                activeLocks.remove(entry);
                log("LOCK RELEASED: PID " + pid + " liberó lock sobre " + filePath);
                return true;
            }
            current = current.next;
        }
        log("LOCK RELEASE ERROR: PID " + pid + " no tenía lock sobre " + filePath);
        return false;
    }
 
    // =======================================================
    // LIBERAR TODOS los locks de un proceso (al terminar)
    // =======================================================
    public void releaseAllLocks(int pid) {
        LinkedList<LockEntry> toRemove = new LinkedList<>();
 
        Node<LockEntry> current = activeLocks.getHead();
        while (current != null) {
            if (current.data.getPid() == pid) {
                toRemove.addLast(current.data);
            }
            current = current.next;
        }
 
        Node<LockEntry> rem = toRemove.getHead();
        while (rem != null) {
            activeLocks.remove(rem.data);
            rem = rem.next;
        }
 
        log("LOCK RELEASE ALL: Todos los locks de PID " + pid + " liberados.");
    }
 
    // =======================================================
    // CONSULTAS
    // =======================================================
 
    // ¿Hay un lock EXCLUSIVE de OTRO proceso sobre este archivo?
    private boolean hasExclusiveLock(String filePath, int requestingPid) {
        Node<LockEntry> current = activeLocks.getHead();
        while (current != null) {
            LockEntry entry = current.data;
            if (entry.getFilePath().equals(filePath)
                    && entry.getPid() != requestingPid
                    && entry.getType() == LockType.EXCLUSIVE) {
                return true;
            }
            current = current.next;
        }
        return false;
    }
 
    // ¿Hay CUALQUIER lock de OTRO proceso sobre este archivo?
    private boolean hasAnyLock(String filePath, int requestingPid) {
        Node<LockEntry> current = activeLocks.getHead();
        while (current != null) {
            LockEntry entry = current.data;
            if (entry.getFilePath().equals(filePath)
                    && entry.getPid() != requestingPid) {
                return true;
            }
            current = current.next;
        }
        return false;
    }
 
    // ¿Este archivo está bloqueado para escritura?
    public boolean isLockedForWrite(String filePath, int requestingPid) {
        return hasAnyLock(filePath, requestingPid);
    }
 
    // ¿Este archivo está bloqueado para lectura?
    public boolean isLockedForRead(String filePath, int requestingPid) {
        return hasExclusiveLock(filePath, requestingPid);
    }
 
    // Obtener todos los locks activos sobre un archivo
    public LinkedList<LockEntry> getLocksForFile(String filePath) {
        LinkedList<LockEntry> result = new LinkedList<>();
        Node<LockEntry> current = activeLocks.getHead();
        while (current != null) {
            if (current.data.getFilePath().equals(filePath)) {
                result.addLast(current.data);
            }
            current = current.next;
        }
        return result;
    }
 
    // =======================================================
    // GETTERS para la GUI
    // =======================================================
    public LinkedList<LockEntry> getActiveLocks() { return activeLocks; }
 
    public int getActiveLocksCount() { return activeLocks.size(); }
 
    // Resumen para log
    public String getSummary() {
        return "Locks activos: " + activeLocks.size();
    }
 
    // =======================================================
    // RESET
    // =======================================================
    public void reset() {
        activeLocks = new LinkedList<>();
        log("RESET: Todos los locks eliminados.");
    }
 
    // =======================================================
    // LOG
    // =======================================================
    private void log(String message) {
        System.out.println("[LockManager] " + message);
    }
}
