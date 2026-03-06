package model;

import structures.LinkedList;
import structures.Node;
import java.awt.Color;

public class FileSystem {

    private VirtualDisk disk;
    private FileEntry root;           // directorio raíz "/"
    private LinkedList<FileEntry> allFiles; // registro plano de todos los archivos (para la JTable)

    public FileSystem(int totalDiskBlocks) {
        this.disk = new VirtualDisk(totalDiskBlocks);
        this.root = new FileEntry("/", "admin", 0, true, Color.LIGHT_GRAY);
        this.allFiles = new LinkedList<>();
    }

    // =======================================================
    // CRUD
    // =======================================================

    // -------------------------------------------------------
    // CREATE - Crear archivo
    // -------------------------------------------------------
    public boolean createFile(String name, String owner, int numBlocks, String parentPath) {
        // Validaciones
        if (name == null || name.trim().isEmpty()) {
            log("ERROR: Nombre de archivo inválido.");
            return false;
        }
        if (numBlocks <= 0) {
            log("ERROR: El número de bloques debe ser mayor a 0.");
            return false;
        }
        if (!disk.hasSpace(numBlocks)) {
            log("ERROR: No hay espacio suficiente en el disco para '" + name + "'.");
            return false;
        }

        FileEntry parent = getEntryByPath(parentPath);
        if (parent == null || !parent.isDirectory()) {
            log("ERROR: Directorio padre '" + parentPath + "' no encontrado.");
            return false;
        }
        if (parent.findChild(name) != null) {
            log("ERROR: Ya existe un archivo llamado '" + name + "' en '" + parentPath + "'.");
            return false;
        }

        // Asignar bloques en el disco
        int firstBlock = disk.allocateBlocks(name, numBlocks);
        if (firstBlock == -1) {
            log("ERROR: Fallo al asignar bloques para '" + name + "'.");
            return false;
        }

        // Obtener el color asignado por el disco al primer bloque
        Color color = disk.getBlock(firstBlock).getColor();

        // Crear entrada del archivo
        FileEntry newFile = new FileEntry(name, owner, numBlocks, false, color);
        newFile.setFirstBlock(firstBlock);

        parent.addChild(newFile);
        allFiles.addLast(newFile);

        log("CREATE: Archivo '" + name + "' creado en '" + parentPath
                + "' | Bloques: " + numBlocks + " | Primer bloque: " + firstBlock);
        return true;
    }

    // -------------------------------------------------------
    // CREATE - Crear directorio
    // -------------------------------------------------------
    public boolean createDirectory(String name, String owner, String parentPath) {
        if (name == null || name.trim().isEmpty()) {
            log("ERROR: Nombre de directorio inválido.");
            return false;
        }

        FileEntry parent = getEntryByPath(parentPath);
        if (parent == null || !parent.isDirectory()) {
            log("ERROR: Directorio padre '" + parentPath + "' no encontrado.");
            return false;
        }
        if (parent.findChild(name) != null) {
            log("ERROR: Ya existe una entrada llamada '" + name + "' en '" + parentPath + "'.");
            return false;
        }

        FileEntry newDir = new FileEntry(name, owner, 0, true, Color.LIGHT_GRAY);
        parent.addChild(newDir);

        log("CREATE: Directorio '" + name + "' creado en '" + parentPath + "'.");
        return true;
    }

    // -------------------------------------------------------
    // READ - Obtener entrada por ruta
    // -------------------------------------------------------
    public FileEntry readEntry(String path) {
        FileEntry entry = getEntryByPath(path);
        if (entry == null) {
            log("READ ERROR: '" + path + "' no encontrado.");
            return null;
        }
        log("READ: '" + path + "' | " + entry.toString());
        return entry;
    }

    // -------------------------------------------------------
    // UPDATE - Renombrar archivo o directorio (solo admin)
    // -------------------------------------------------------
    public boolean renameEntry(String path, String newName, String requestingUser) {
        if (!"admin".equals(requestingUser)) {
            log("UPDATE ERROR: Solo el administrador puede renombrar entradas.");
            return false;
        }
        if (newName == null || newName.trim().isEmpty()) {
            log("UPDATE ERROR: Nuevo nombre inválido.");
            return false;
        }

        FileEntry entry = getEntryByPath(path);
        if (entry == null) {
            log("UPDATE ERROR: '" + path + "' no encontrado.");
            return false;
        }

        String oldName = entry.getName();
        entry.setName(newName);
        log("UPDATE: '" + oldName + "' renombrado a '" + newName + "'.");
        return true;
    }

    // -------------------------------------------------------
    // DELETE - Eliminar archivo o directorio
    // -------------------------------------------------------
    public boolean deleteEntry(String path, String requestingUser) {
        if (!"admin".equals(requestingUser)) {
            log("DELETE ERROR: Solo el administrador puede eliminar entradas.");
            return false;
        }

        // Separar nombre del padre
        String parentPath = getParentPath(path);
        String entryName  = getEntryName(path);

        FileEntry parent = getEntryByPath(parentPath);
        if (parent == null) {
            log("DELETE ERROR: Directorio padre no encontrado para '" + path + "'.");
            return false;
        }

        FileEntry target = parent.findChild(entryName);
        if (target == null) {
            log("DELETE ERROR: '" + path + "' no encontrado.");
            return false;
        }

        // Eliminar recursivamente
        deleteRecursive(target);
        parent.removeChild(target);

        log("DELETE: '" + path + "' eliminado correctamente.");
        return true;
    }

    // Elimina recursivamente todos los hijos y libera sus bloques
    private void deleteRecursive(FileEntry entry) {
        if (entry.isDirectory()) {
            Node<FileEntry> current = entry.getChildren().getHead();
            while (current != null) {
                deleteRecursive(current.data);
                current = current.next;
            }
        } else {
            // Liberar bloques del disco
            if (entry.getFirstBlock() != -1) {
                disk.freeBlocks(entry.getFirstBlock());
            }
            // Quitar del registro plano
            allFiles.remove(entry);
        }
    }

    // =======================================================
    // NAVEGACIÓN / UTILIDADES
    // =======================================================

    // Recorre la ruta y devuelve la FileEntry correspondiente
    // Ejemplo: "/documentos/notas" → busca "documentos" en root, luego "notas"
    public FileEntry getEntryByPath(String path) {
        if (path == null) return null;
        if (path.equals("/")) return root;

        // Limpiar slashes duplicados y dividir manualmente
        String clean = path.startsWith("/") ? path.substring(1) : path;
        LinkedList<String> parts = splitPath(clean);

        FileEntry current = root;
        Node<String> node = parts.getHead();

        while (node != null) {
            if (!current.isDirectory()) return null;
            current = current.findChild(node.data);
            if (current == null) return null;
            node = node.next;
        }

        return current;
    }

    // Divide una ruta en partes sin usar String.split()
    private LinkedList<String> splitPath(String path) {
        LinkedList<String> parts = new LinkedList<>();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < path.length(); i++) {
            char c = path.charAt(i);
            if (c == '/') {
                if (sb.length() > 0) {
                    parts.addLast(sb.toString());
                    sb = new StringBuilder();
                }
            } else {
                sb.append(c);
            }
        }
        if (sb.length() > 0) parts.addLast(sb.toString());
        return parts;
    }

    // "/documentos/notas.txt" → "/documentos"
    private String getParentPath(String path) {
        int lastSlash = path.lastIndexOf('/');
        if (lastSlash <= 0) return "/";
        return path.substring(0, lastSlash);
    }

    // "/documentos/notas.txt" → "notas.txt"
    private String getEntryName(String path) {
        int lastSlash = path.lastIndexOf('/');
        return path.substring(lastSlash + 1);
    }

    // =======================================================
    // GETTERS para la GUI
    // =======================================================
    public VirtualDisk getDisk()              { return disk; }
    public FileEntry getRoot()                { return root; }
    public LinkedList<FileEntry> getAllFiles() { return allFiles; }

    // =======================================================
    // LOG interno (luego lo conectarás a la GUI)
    // =======================================================
    private void log(String message) {
        System.out.println("[FileSystem] " + message);
    }
}