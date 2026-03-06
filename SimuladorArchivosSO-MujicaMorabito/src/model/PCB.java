package model;

public class PCB {

    // =======================================================
    // Enums de estado y operación
    // =======================================================
    public enum ProcessState {
        NEW,        // recién creado
        READY,      // listo para ejecutar
        RUNNING,    // ejecutándose
        BLOCKED,    // esperando un lock o recurso
        TERMINATED  // terminado
    }

    public enum IOOperation {
        CREATE,
        READ,
        UPDATE,
        DELETE
    }

    // =======================================================
    // Atributos del proceso
    // =======================================================
    private static int idCounter = 1; // generador de IDs únicos

    private int     pid;              // ID único del proceso
    private String  name;             // nombre descriptivo (ej: "P1")
    private String  owner;            // usuario que lanzó el proceso
    private ProcessState state;       // estado actual
    private IOOperation  operation;   // operación que quiere realizar
    private String  targetPath;       // ruta del archivo objetivo
    private int     diskPosition;     // posición en el disco (para planificación)
    private int     burstTime;        // tiempo de ráfaga estimado (opcional)
    private long    arrivalTime;      // momento en que entró al sistema
    private long    startTime;        // momento en que empezó a ejecutar
    private long    finishTime;       // momento en que terminó
    private String  statusMessage;    // mensaje de estado para la GUI

    // =======================================================
    // Constructor
    // =======================================================
    public PCB(String owner, IOOperation operation, String targetPath, int diskPosition) {
        this.pid          = idCounter++;
        this.name         = "P" + this.pid;
        this.owner        = owner;
        this.operation    = operation;
        this.targetPath   = targetPath;
        this.diskPosition = diskPosition;
        this.state        = ProcessState.NEW;
        this.arrivalTime  = System.currentTimeMillis();
        this.startTime    = -1;
        this.finishTime   = -1;
        this.statusMessage = "Proceso creado.";
    }

    // =======================================================
    // Transiciones de estado
    // =======================================================
    public void admit() {
        if (state == ProcessState.NEW) {
            state = ProcessState.READY;
            statusMessage = "En cola, listo para ejecutar.";
        }
    }

    public void start() {
        if (state == ProcessState.READY) {
            state = ProcessState.RUNNING;
            startTime = System.currentTimeMillis();
            statusMessage = "Ejecutando " + operation + " sobre '" + targetPath + "'.";
        }
    }

    public void block(String reason) {
        if (state == ProcessState.RUNNING || state == ProcessState.READY) {
            state = ProcessState.BLOCKED;
            statusMessage = "Bloqueado: " + reason;
        }
    }

    public void unblock() {
        if (state == ProcessState.BLOCKED) {
            state = ProcessState.READY;
            statusMessage = "Desbloqueado, listo para ejecutar.";
        }
    }

    public void terminate() {
        state = ProcessState.TERMINATED;
        finishTime = System.currentTimeMillis();
        statusMessage = "Proceso terminado.";
    }

    // =======================================================
    // Métricas de tiempo
    // =======================================================

    // Tiempo de espera (desde llegada hasta que empezó a ejecutar)
    public long getWaitingTime() {
        if (startTime == -1) return System.currentTimeMillis() - arrivalTime;
        return startTime - arrivalTime;
    }

    // Tiempo de retorno (desde llegada hasta que terminó)
    public long getTurnaroundTime() {
        if (finishTime == -1) return -1;
        return finishTime - arrivalTime;
    }

    // =======================================================
    // Getters
    // =======================================================
    public int          getPid()           { return pid; }
    public String       getName()          { return name; }
    public String       getOwner()         { return owner; }
    public ProcessState getState()         { return state; }
    public IOOperation  getOperation()     { return operation; }
    public String       getTargetPath()    { return targetPath; }
    public int          getDiskPosition()  { return diskPosition; }
    public int          getBurstTime()     { return burstTime; }
    public long         getArrivalTime()   { return arrivalTime; }
    public long         getStartTime()     { return startTime; }
    public long         getFinishTime()    { return finishTime; }
    public String       getStatusMessage() { return statusMessage; }

    // =======================================================
    // Setters
    // =======================================================
    public void setState(ProcessState state)       { this.state = state; }
    public void setDiskPosition(int diskPosition)  { this.diskPosition = diskPosition; }
    public void setBurstTime(int burstTime)        { this.burstTime = burstTime; }
    public void setStatusMessage(String msg)       { this.statusMessage = msg; }
    public void setTargetPath(String targetPath)   { this.targetPath = targetPath; }

    // Reset del contador (útil al reiniciar la simulación)
    public static void resetIdCounter() { idCounter = 1; }

    // =======================================================
    // toString para log y GUI
    // =======================================================
    @Override
    public String toString() {
        return String.format(
            "[PCB] PID=%d | %s | Owner=%s | Op=%s | Path=%s | Disk=%d | State=%s",
            pid, name, owner, operation, targetPath, diskPosition, state
        );
    }
}