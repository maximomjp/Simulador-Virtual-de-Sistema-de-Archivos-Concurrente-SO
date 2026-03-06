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
    // Asignación encadenada: asigna 'numBlocks' bloques a un archivo
    // Retorna el índice del primer bloque, o -1 si no hay espacio
    // -------------------------------------------------------
    public int allocateBlocks(String fileName, int numBlocks) {
        if (numBlocks <= 0) throw new IllegalArgumentException("Número de bloques inválido.");
        if (freeBlocksCount < numBlocks) return -1; // no hay espacio

        Color color = getNextColor();

        // Recolectar bloques libres
        LinkedList<Integer> freeIndices = new LinkedList<>();
        for (int i = 0; i < totalBlocks; i++) {
            if (blocks[i].isFree()) {
                freeIndices.addLast(i);
                if (freeIndices.size() == numBlocks) break;
            }
        }

        // Encadenar los bloques
        Node<Integer> current = freeIndices.getHead();
        int firstBlock = current.data;

        while (current != null) {
            int idx = current.data;
            blocks[idx].allocate(fileName, color);

            if (current.next != null) {
                blocks[idx].setNextBlock(current.next.data);
            } else {
                blocks[idx].setNextBlock(-1); // último bloque
            }

            current = current.next;
        }

        freeBlocksCount -= numBlocks;
        return firstBlock;
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
