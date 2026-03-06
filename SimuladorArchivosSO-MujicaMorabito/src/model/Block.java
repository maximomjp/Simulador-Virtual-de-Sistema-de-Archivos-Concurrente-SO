package model;

public class Block {
    private int blockNumber;
    private boolean isFree;
    private String ownerFile;   // nombre del archivo que ocupa este bloque
    private int nextBlock;      // número del siguiente bloque (-1 si es el último)
    private java.awt.Color color; // color del archivo dueño (para la GUI)

    public Block(int blockNumber) {
        this.blockNumber = blockNumber;
        this.isFree = true;
        this.ownerFile = null;
        this.nextBlock = -1;
        this.color = null;
    }

    // Asignar este bloque a un archivo
    public void allocate(String ownerFile, java.awt.Color color) {
        this.isFree = false;
        this.ownerFile = ownerFile;
        this.color = color;
    }

    // Liberar este bloque
    public void free() {
        this.isFree = true;
        this.ownerFile = null;
        this.nextBlock = -1;
        this.color = null;
    }

    public int getBlockNumber() { return blockNumber; }
    public boolean isFree()     { return isFree; }
    public String getOwnerFile(){ return ownerFile; }
    public int getNextBlock()   { return nextBlock; }
    public java.awt.Color getColor() { return color; }

    public void setNextBlock(int nextBlock) { this.nextBlock = nextBlock; }
    public void setFree(boolean free)       { this.isFree = free; }

    @Override
    public String toString() {
        return "Block{#" + blockNumber + ", free=" + isFree + ", owner=" + ownerFile + ", next=" + nextBlock + "}";
    }
}