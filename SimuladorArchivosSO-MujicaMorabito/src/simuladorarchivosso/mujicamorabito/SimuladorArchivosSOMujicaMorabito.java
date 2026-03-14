/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package simuladorarchivosso.mujicamorabito;

import model.DiskScheduler;
import structures.LinkedList;
import model.LockManager;
import model.UserManager;
import model.FileEntry;
import java.awt.Color;
import model.Journal;
import model.FileSystem;
import model.JsonManager;
import view.MainFrame;

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
        javax.swing.SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}
    

