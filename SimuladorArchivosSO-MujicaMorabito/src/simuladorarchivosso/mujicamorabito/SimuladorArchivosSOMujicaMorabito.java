/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package simuladorarchivosso.mujicamorabito;

import model.Diskscheduler;
import structures.LinkedList;
import model.LockManager;

/**
 *
 * @author MaximoDev
 */
public class SimuladorArchivosSOMujicaMorabito {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        LockManager lm = new LockManager();

// P1 pide leer archivo → SHARED OK
System.out.println(lm.acquireLock("/docs/a.txt", 1, LockManager.LockType.SHARED));   // true

// P2 también quiere leer → SHARED OK (múltiples lectores)
System.out.println(lm.acquireLock("/docs/a.txt", 2, LockManager.LockType.SHARED));   // true

// P3 quiere escribir → EXCLUSIVE DENIED (hay lectores)
System.out.println(lm.acquireLock("/docs/a.txt", 3, LockManager.LockType.EXCLUSIVE)); // false

// P1 libera su lock
lm.releaseLock("/docs/a.txt", 1);

// P2 libera su lock
lm.releaseLock("/docs/a.txt", 2);

// P3 intenta de nuevo → EXCLUSIVE OK (ya no hay nadie)
System.out.println(lm.acquireLock("/docs/a.txt", 3, LockManager.LockType.EXCLUSIVE)); // true

// P4 quiere leer → SHARED DENIED (hay escritor exclusivo)
System.out.println(lm.acquireLock("/docs/a.txt", 4, LockManager.LockType.SHARED));    // false
    }
    
}
