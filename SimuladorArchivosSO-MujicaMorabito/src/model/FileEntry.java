package model;

import java.awt.Color;

public class FileEntry {
    private String name;
    private String owner;        // usuario dueño del archivo
    private int totalBlocks;     // cantidad de bloques asignados
    private int firstBlock;      // dirección del primer bloque (-1 si no asignado)
    private boolean isDirectory;
    private boolean isPublic;
    private Color color;         // color único para visualización en el disco

    // Para directorios
    private structures.LinkedList<FileEntry> children;

    public FileEntry(String name, String owner, int totalBlocks, boolean isDirectory, Color color) {
        this.name = name;
        this.owner = owner;
        this.totalBlocks = totalBlocks;
        this.firstBlock = -1;
        this.isDirectory = isDirectory;
        this.isPublic = false;
        this.color = color;

        if (isDirectory) {
            this.children = new structures.LinkedList<>();
        }
    }

    // Agregar hijo (solo si es directorio)
    public void addChild(FileEntry child) {
        if (!isDirectory) throw new RuntimeException(name + " no es un directorio.");
        children.addLast(child);
    }

    // Eliminar hijo por nombre
    public boolean removeChild(FileEntry child) {
        if (!isDirectory) return false;
        return children.remove(child);
    }

    // Buscar hijo por nombre
    public FileEntry findChild(String childName) {
        if (!isDirectory) return null;
        structures.Node<FileEntry> current = children.getHead();
        while (current != null) {
            if (current.data.getName().equals(childName)) return current.data;
            current = current.next;
        }
        return null;
    }

    // Getters
    public String getName()      { return name; }
    public String getOwner()     { return owner; }
    public int getTotalBlocks()  { return totalBlocks; }
    public int getFirstBlock()   { return firstBlock; }
    public boolean isDirectory() { return isDirectory; }
    public boolean isPublic()    { return isPublic; }
    public Color getColor()      { return color; }
    public structures.LinkedList<FileEntry> getChildren() { return children; }

    // Setters
    public void setName(String name)         { this.name = name; }
    public void setFirstBlock(int firstBlock){ this.firstBlock = firstBlock; }
    public void setPublic(boolean isPublic)  { this.isPublic = isPublic; }
    public void setTotalBlocks(int blocks)   { this.totalBlocks = blocks; }

    @Override
    public String toString() {
        return (isDirectory ? "[DIR] " : "[FILE] ") + name + " | Owner: " + owner
                + " | Blocks: " + totalBlocks + " | FirstBlock: " + firstBlock;
    }
}