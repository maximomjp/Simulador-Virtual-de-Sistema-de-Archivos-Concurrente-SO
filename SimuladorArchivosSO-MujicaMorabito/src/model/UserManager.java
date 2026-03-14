/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

/**
 *
 * @author alemo
 */
public class UserManager {
    
 
    // =======================================================
    // Roles del sistema
    // =======================================================
    public enum Role {
        ADMIN,  // puede hacer todo: crear, leer, actualizar, eliminar
        USER    // solo lectura de archivos propios o públicos
    }
 
    // =======================================================
    // Atributos
    // =======================================================
    private String currentUser;  // nombre del usuario actual
    private Role currentRole;    // rol actual
 
    // =======================================================
    // Constructor (por defecto arranca como admin)
    // =======================================================
    public UserManager() {
        this.currentUser = "admin";
        this.currentRole = Role.ADMIN;
    }
 
    // =======================================================
    // CAMBIAR MODO
    // =======================================================
    public void switchToAdmin() {
        this.currentUser = "admin";
        this.currentRole = Role.ADMIN;
        log("Modo cambiado a ADMINISTRADOR.");
    }
 
    public void switchToUser(String username) {
        if (username == null || username.trim().isEmpty()) {
            log("ERROR: Nombre de usuario inválido.");
            return;
        }
        this.currentUser = username.trim();
        this.currentRole = Role.USER;
        log("Modo cambiado a USUARIO: " + this.currentUser);
    }
 
    // =======================================================
    // VERIFICACIONES DE PERMISOS
    // =======================================================
 
    // ¿Puede crear archivos/directorios?
    public boolean canCreate() {
        return currentRole == Role.ADMIN;
    }
 
    // ¿Puede eliminar archivos/directorios?
    public boolean canDelete() {
        return currentRole == Role.ADMIN;
    }
 
    // ¿Puede renombrar (actualizar)?
    public boolean canUpdate() {
        return currentRole == Role.ADMIN;
    }
 
    // ¿Puede leer este archivo?
    // Admin puede leer todo. Usuario solo sus archivos o los públicos.
    public boolean canRead(FileEntry file) {
        if (currentRole == Role.ADMIN) return true;
        if (file == null) return false;
        // El usuario puede leer si es dueño o si el archivo es público
        return file.getOwner().equals(currentUser) || file.isPublic();
    }
 
    // ¿Puede cambiar políticas de planificación?
    public boolean canChangePolicy() {
        return currentRole == Role.ADMIN;
    }
 
    // ¿Puede ver información completa del disco?
    public boolean canViewFullDiskInfo() {
        return currentRole == Role.ADMIN;
    }
 
    // =======================================================
    // GETTERS
    // =======================================================
    public String getCurrentUser() { return currentUser; }
    public Role getCurrentRole()   { return currentRole; }
    public boolean isAdmin()       { return currentRole == Role.ADMIN; }
 
    // =======================================================
    // RESUMEN
    // =======================================================
    public String getSummary() {
        return "Usuario: " + currentUser + " | Rol: " + currentRole;
    }
 
    // =======================================================
    // LOG
    // =======================================================
    private void log(String message) {
        System.out.println("[UserManager] " + message);
    }
}
