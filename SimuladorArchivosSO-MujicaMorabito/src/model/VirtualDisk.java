package model;

import java.awt.Color;
import structures.LinkedList;
import structures.Node;

public class VirtualDisk {

    private Block[] blocks;
    private int totalBlocks;
    private int freeBlocksCount;

    // Colores predefinidos para diferenciar archivos en la GUI
    private static final Color[] FILE_COLORS = {
        new Color(70, 130, 180),   // azul acero
        new Color(60, 179, 113),   // verde mar
        new Color(205, 92, 92),    // rojo indio
        new Color(255, 165, 0),    // naranja
        new Color(147, 112, 219),  // púrpura
        new Color(64, 224, 208),   // turquesa
        new Color(255, 105, 180),  // rosa
        new Color(210, 180, 140),  // arena
    };
    private int colorIndex = 0;

    public VirtualDisk(int totalBlocks) {
        this.totalBlocks = totalBlocks;
        this.freeBlocksCount = totalBlocks;
        this.blocks = new Block[totalBlocks];

        for (int i = 0; i < totalBlocks; i++) {
            blocks[i] = new Block(i);
        }
    }

    // -------------------------------------------------------
    // Asignación encadenada desde un bloque inicial específico (Para el JSON)
    // -------------------------------------------------------
    public int allocateBlocksAt(String fileName, int numBlocks, int startBlock) {
        if (numBlocks <= 0) throw new IllegalArgumentException("Número de bloques inválido.");
        if (freeBlocksCount < numBlocks) return -1; // No hay espacio en todo el disco
        if (startBlock < 0 || startBlock >= totalBlocks || !blocks[startBlock].isFree()) {
            return -1; // El bloque inicial exigido ya está ocupado o no es válido
        }

        Color color = getNextColor();
        int currentBlock = startBlock;
        blocks[currentBlock].allocate(fileName, color);
        int blocksAllocated = 1;

        // Buscar y encadenar los bloques restantes
        while (blocksAllocated < numBlocks) {
            // ¡EL CAMBIO MAGISTRAL! Busca a partir del bloque siguiente al actual
            int nextFree = findNextFreeBlock(currentBlock + 1); 
            
            blocks[currentBlock].setNextBlock(nextFree); // El actual apunta al siguiente
            currentBlock = nextFree; // Saltamos al nuevo bloque
            blocks[currentBlock].allocate(fileName, color); // Lo ocupamos
            blocksAllocated++;
        }

        // El último bloque de la cadena debe apuntar a -1
        blocks[currentBlock].setNextBlock(-1);
        freeBlocksCount -= numBlocks;
        
        return startBlock;
    }

    // -------------------------------------------------------
    // Asignación encadenada estándar (Para el botón "Crear Archivo" en la GUI)
    // -------------------------------------------------------
    public int allocateBlocks(String fileName, int numBlocks) {
        if (freeBlocksCount < numBlocks) return -1;
        
        // Empezamos a buscar desde 0 cuando es un archivo nuevo sin posición exigida
        int firstFree = findNextFreeBlock(0); 
        if (firstFree == -1) return -1;
        
        return allocateBlocksAt(fileName, numBlocks, firstFree);
    }

    // -------------------------------------------------------
    // NUEVO: Busca el siguiente bloque libre a partir de un índice (con Wrap-Around)
    // -------------------------------------------------------
    private int findNextFreeBlock(int startIndex) {
        // 1. Buscar hacia adelante desde el startIndex hasta el final del disco
        for (int i = startIndex; i < totalBlocks; i++) {
            if (blocks[i].isFree()) {
                return i;
            }
        }
        
        // 2. Si llegamos al final del disco y no encontramos, damos la vuelta (wrap-around)
        // y buscamos desde el bloque 0 hasta el startIndex
        for (int i = 0; i < startIndex; i++) {
            if (blocks[i].isFree()) {
                return i;
            }
        }
        
        return -1; // No hay bloques libres en absoluto
    }

    // -------------------------------------------------------
    // Liberar todos los bloques encadenados a partir de firstBlock
    // -------------------------------------------------------
    public void freeBlocks(int firstBlock) {
        int current = firstBlock;
        while (current != -1) {
            int next = blocks[current].getNextBlock();
            blocks[current].free();
            freeBlocksCount++;
            current = next;
        }
    }

    // -------------------------------------------------------
    // Obtener la lista de números de bloque de un archivo
    // -------------------------------------------------------
    public LinkedList<Integer> getBlockChain(int firstBlock) {
        LinkedList<Integer> chain = new LinkedList<>();
        int current = firstBlock;
        while (current != -1) {
            chain.addLast(current);
            current = blocks[current].getNextBlock();
        }
        return chain;
    }

    // -------------------------------------------------------
    // Verificar si hay espacio para N bloques
    // -------------------------------------------------------
    public boolean hasSpace(int numBlocks) {
        return freeBlocksCount >= numBlocks;
    }

    // -------------------------------------------------------
    // Getters útiles para la GUI
    // -------------------------------------------------------
    public Block getBlock(int index)  { return blocks[index]; }
    public Block[] getAllBlocks()     { return blocks; }
    public int getTotalBlocks()       { return totalBlocks; }
    public int getFreeBlocksCount()   { return freeBlocksCount; }
    public int getUsedBlocksCount()   { return totalBlocks - freeBlocksCount; }

    // -------------------------------------------------------
    // Color rotativo para cada nuevo archivo
    // -------------------------------------------------------
    private Color getNextColor() {
        Color c = FILE_COLORS[colorIndex % FILE_COLORS.length];
        colorIndex++;
        return c;
    }

    // -------------------------------------------------------
    // Resumen del disco (útil para debug y log)
    // -------------------------------------------------------
    public String getSummary() {
        return "Disco: " + totalBlocks + " bloques totales | "
             + freeBlocksCount + " libres | "
             + getUsedBlocksCount() + " ocupados";
    }
}