/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package simuladorarchivosso.mujicamorabito;

import model.Diskscheduler;
import structures.LinkedList;

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
        
        Diskscheduler scheduler = new Diskscheduler(50, 200);
        scheduler.setDirectionUp(true);

        LinkedList<Integer> requests = new LinkedList<>();
        int[] positions = {95, 180, 34, 119, 11, 123, 62, 64};
        for (int p : positions) requests.addLast(p);

        // Probar cada política
        for (Diskscheduler.Policy policy : Diskscheduler.Policy.values()) {
            LinkedList<Integer> order = scheduler.schedule(requests, policy);
            System.out.println(policy + ": " + scheduler.orderToString(order)
                + " | Movimiento: " + scheduler.calculateTotalMovement(order));
        }
    }
    
}
