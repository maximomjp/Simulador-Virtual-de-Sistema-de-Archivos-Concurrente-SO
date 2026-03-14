/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package simuladorarchivosso.mujicamorabito;

import model.Diskscheduler;
import structures.LinkedList;
import model.LockManager;
import model.UserManager;
import model.FileEntry;
import java.awt.Color;

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
        UserManager um = new UserManager();

// Arranca como admin
System.out.println(um.getSummary());          // admin | ADMIN
System.out.println("Puede crear: " + um.canCreate());   // true
System.out.println("Puede eliminar: " + um.canDelete()); // true

// Cambiar a usuario
um.switchToUser("juan");
System.out.println(um.getSummary());          // juan | USER
System.out.println("Puede crear: " + um.canCreate());   // false
System.out.println("Puede eliminar: " + um.canDelete()); // false

// Probar lectura
FileEntry archivoDeJuan = new FileEntry("notas.txt", "juan", 3, false, Color.BLUE);
FileEntry archivoDePedro = new FileEntry("secreto.txt", "pedro", 2, false, Color.RED);
FileEntry archivoPublico = new FileEntry("readme.txt", "pedro", 1, false, Color.GREEN);
archivoPublico.setPublic(true);

System.out.println("Leer propio: " + um.canRead(archivoDeJuan));      // true
System.out.println("Leer de otro: " + um.canRead(archivoDePedro));    // false
System.out.println("Leer público: " + um.canRead(archivoPublico));    // true
    }
    
}
