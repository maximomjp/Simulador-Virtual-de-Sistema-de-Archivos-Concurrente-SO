/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

import structures.LinkedList;
import structures.Node;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 *
 * @author alemo
 */
public class JsonManager {
    
 
    // =======================================================
    // EXPORTAR estado del sistema a un archivo JSON
    // =======================================================
    public static boolean exportState(FileSystem fileSystem, DiskScheduler scheduler, String filePath) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("{\n");
 
            // Disco
            sb.append("  \"disk\": {\n");
            sb.append("    \"totalBlocks\": ").append(fileSystem.getDisk().getTotalBlocks()).append(",\n");
            sb.append("    \"freeBlocks\": ").append(fileSystem.getDisk().getFreeBlocksCount()).append(",\n");
            sb.append("    \"usedBlocks\": ").append(fileSystem.getDisk().getUsedBlocksCount()).append("\n");
            sb.append("  },\n");
 
            // Cabezal
            sb.append("  \"headPosition\": ").append(scheduler.getCurrentHeadPosition()).append(",\n");
 
            // Archivos
            sb.append("  \"files\": [\n");
            Node<FileEntry> current = fileSystem.getAllFiles().getHead();
            while (current != null) {
                FileEntry f = current.data;
                sb.append("    {\n");
                sb.append("      \"name\": \"").append(escapeJson(f.getName())).append("\",\n");
                sb.append("      \"owner\": \"").append(escapeJson(f.getOwner())).append("\",\n");
                sb.append("      \"blocks\": ").append(f.getTotalBlocks()).append(",\n");
                sb.append("      \"firstBlock\": ").append(f.getFirstBlock()).append(",\n");
                sb.append("      \"isPublic\": ").append(f.isPublic()).append("\n");
                sb.append("    }");
                if (current.next != null) sb.append(",");
                sb.append("\n");
                current = current.next;
            }
            sb.append("  ]\n");
 
            sb.append("}\n");
 
            // Escribir archivo
            FileWriter writer = new FileWriter(filePath);
            writer.write(sb.toString());
            writer.close();
 
            log("EXPORT: Estado exportado a '" + filePath + "'.");
            return true;
 
        } catch (IOException e) {
            log("EXPORT ERROR: " + e.getMessage());
            return false;
        }
    }
 
    // =======================================================
    // CARGAR caso de prueba desde JSON (formato del PDF)
    // Formato esperado:
    // {
    //   "test_id": "P1",
    //   "initial_head": 50,
    //   "requests": [ {"pos": 95, "op": "READ"}, ... ],
    //   "system_files": { "95": {"name": "config.sys", "blocks": 4}, ... }
    // }
    // =======================================================
    public static TestCase loadTestCase(String filePath) {
        try {
            String json = readFile(filePath);
            TestCase testCase = new TestCase();
 
            // Parsear test_id
            testCase.testId = extractString(json, "test_id");
 
            // Parsear initial_head
            testCase.initialHead = extractInt(json, "initial_head");
 
            // Parsear requests
            testCase.requests = new LinkedList<>();
            String requestsArray = extractArray(json, "requests");
            if (requestsArray != null) {
                LinkedList<String> requestObjects = splitJsonArray(requestsArray);
                Node<String> current = requestObjects.getHead();
                while (current != null) {
                    String obj = current.data;
                    int pos = extractInt(obj, "pos");
                    String op = extractString(obj, "op");
                    testCase.requests.addLast(new TestRequest(pos, op));
                    current = current.next;
                }
            }
 
            // Parsear system_files
            testCase.systemFiles = new LinkedList<>();
            String systemFilesBlock = extractObject(json, "system_files");
            if (systemFilesBlock != null) {
                LinkedList<String> keys = extractObjectKeys(systemFilesBlock);
                Node<String> keyNode = keys.getHead();
                while (keyNode != null) {
                    String key = keyNode.data;
                    String fileObj = extractObject(systemFilesBlock, key);
                    if (fileObj != null) {
                        String name = extractString(fileObj, "name");
                        int blocks = extractInt(fileObj, "blocks");
                        int position = Integer.parseInt(key.trim());
                        testCase.systemFiles.addLast(new SystemFile(position, name, blocks));
                    }
                    keyNode = keyNode.next;
                }
            }
 
            log("LOAD: Caso de prueba '" + testCase.testId + "' cargado desde '" + filePath + "'.");
            return testCase;
 
        } catch (Exception e) {
            log("LOAD ERROR: " + e.getMessage());
            return null;
        }
    }
 
    // =======================================================
    // CLASES para representar un caso de prueba
    // =======================================================
    public static class TestCase {
        public String testId;
        public int initialHead;
        public LinkedList<TestRequest> requests;
        public LinkedList<SystemFile> systemFiles;
 
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("TestCase: ").append(testId).append(" | Head: ").append(initialHead).append("\n");
 
            sb.append("Requests: ");
            Node<TestRequest> r = requests.getHead();
            while (r != null) {
                sb.append(r.data.toString());
                if (r.next != null) sb.append(", ");
                r = r.next;
            }
            sb.append("\n");
 
            sb.append("System Files: ");
            Node<SystemFile> f = systemFiles.getHead();
            while (f != null) {
                sb.append(f.data.toString());
                if (f.next != null) sb.append(", ");
                f = f.next;
            }
 
            return sb.toString();
        }
    }
 
    public static class TestRequest {
        public int position;
        public String operation;
 
        public TestRequest(int position, String operation) {
            this.position = position;
            this.operation = operation;
        }
 
        @Override
        public String toString() {
            return "{pos=" + position + ", op=" + operation + "}";
        }
    }
 
    public static class SystemFile {
        public int position;
        public String name;
        public int blocks;
 
        public SystemFile(int position, String name, int blocks) {
            this.position = position;
            this.name = name;
            this.blocks = blocks;
        }
 
        @Override
        public String toString() {
            return "{pos=" + position + ", name=" + name + ", blocks=" + blocks + "}";
        }
    }
 
    // =======================================================
    // PARSER JSON MANUAL (sin librerías externas)
    // =======================================================
 
    // Leer archivo completo a String
    private static String readFile(String filePath) throws IOException {
        StringBuilder sb = new StringBuilder();
        BufferedReader reader = new BufferedReader(new FileReader(filePath));
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line).append("\n");
        }
        reader.close();
        return sb.toString();
    }
 
    // Extraer valor String de un campo: "key": "value"
    private static String extractString(String json, String key) {
        String search = "\"" + key + "\"";
        int keyIndex = json.indexOf(search);
        if (keyIndex == -1) return null;
 
        int colonIndex = json.indexOf(":", keyIndex + search.length());
        if (colonIndex == -1) return null;
 
        int startQuote = json.indexOf("\"", colonIndex + 1);
        if (startQuote == -1) return null;
 
        int endQuote = json.indexOf("\"", startQuote + 1);
        if (endQuote == -1) return null;
 
        return json.substring(startQuote + 1, endQuote);
    }
 
    // Extraer valor int de un campo: "key": 123
    private static int extractInt(String json, String key) {
        String search = "\"" + key + "\"";
        int keyIndex = json.indexOf(search);
        if (keyIndex == -1) return -1;
 
        int colonIndex = json.indexOf(":", keyIndex + search.length());
        if (colonIndex == -1) return -1;
 
        int start = colonIndex + 1;
        while (start < json.length() && (json.charAt(start) == ' ' || json.charAt(start) == '\n')) {
            start++;
        }
 
        StringBuilder numStr = new StringBuilder();
        while (start < json.length() && (Character.isDigit(json.charAt(start)) || json.charAt(start) == '-')) {
            numStr.append(json.charAt(start));
            start++;
        }
 
        return numStr.length() > 0 ? Integer.parseInt(numStr.toString()) : -1;
    }
 
    // Extraer un array JSON: "key": [ ... ]
    private static String extractArray(String json, String key) {
        String search = "\"" + key + "\"";
        int keyIndex = json.indexOf(search);
        if (keyIndex == -1) return null;
 
        int bracketStart = json.indexOf("[", keyIndex);
        if (bracketStart == -1) return null;
 
        int depth = 0;
        int bracketEnd = bracketStart;
        for (int i = bracketStart; i < json.length(); i++) {
            if (json.charAt(i) == '[') depth++;
            else if (json.charAt(i) == ']') depth--;
            if (depth == 0) {
                bracketEnd = i;
                break;
            }
        }
 
        return json.substring(bracketStart + 1, bracketEnd);
    }
 
    // Extraer un objeto JSON: "key": { ... }
    private static String extractObject(String json, String key) {
        String search = "\"" + key + "\"";
        int keyIndex = json.indexOf(search);
        if (keyIndex == -1) return null;
 
        int braceStart = json.indexOf("{", keyIndex + search.length());
        if (braceStart == -1) return null;
 
        int depth = 0;
        int braceEnd = braceStart;
        for (int i = braceStart; i < json.length(); i++) {
            if (json.charAt(i) == '{') depth++;
            else if (json.charAt(i) == '}') depth--;
            if (depth == 0) {
                braceEnd = i;
                break;
            }
        }
 
        return json.substring(braceStart + 1, braceEnd);
    }
 
    // Separar elementos de un array JSON en objetos individuales
    private static LinkedList<String> splitJsonArray(String arrayContent) {
        LinkedList<String> items = new LinkedList<>();
        int depth = 0;
        int start = -1;
 
        for (int i = 0; i < arrayContent.length(); i++) {
            char c = arrayContent.charAt(i);
            if (c == '{') {
                if (depth == 0) start = i;
                depth++;
            } else if (c == '}') {
                depth--;
                if (depth == 0 && start != -1) {
                    items.addLast(arrayContent.substring(start, i + 1));
                    start = -1;
                }
            }
        }
 
        return items;
    }
 
    // Extraer las keys de un objeto JSON (para system_files)
    private static LinkedList<String> extractObjectKeys(String objectContent) {
        LinkedList<String> keys = new LinkedList<>();
        int i = 0;
 
        while (i < objectContent.length()) {
            int quoteStart = objectContent.indexOf("\"", i);
            if (quoteStart == -1) break;
 
            int quoteEnd = objectContent.indexOf("\"", quoteStart + 1);
            if (quoteEnd == -1) break;
 
            String possibleKey = objectContent.substring(quoteStart + 1, quoteEnd);
 
            // Verificar que después viene un ":"
            int afterQuote = quoteEnd + 1;
            while (afterQuote < objectContent.length() && objectContent.charAt(afterQuote) == ' ') {
                afterQuote++;
            }
 
            if (afterQuote < objectContent.length() && objectContent.charAt(afterQuote) == ':') {
                // Verificar si es una key numérica (para system_files) o una key normal
                // Solo agregar keys de primer nivel (que tengan un { después del :)
                int afterColon = afterQuote + 1;
                while (afterColon < objectContent.length()
                        && (objectContent.charAt(afterColon) == ' ' || objectContent.charAt(afterColon) == '\n')) {
                    afterColon++;
                }
                if (afterColon < objectContent.length() && objectContent.charAt(afterColon) == '{') {
                    keys.addLast(possibleKey);
                    // Saltar el objeto completo
                    int depth = 0;
                    for (int j = afterColon; j < objectContent.length(); j++) {
                        if (objectContent.charAt(j) == '{') depth++;
                        else if (objectContent.charAt(j) == '}') depth--;
                        if (depth == 0) {
                            i = j + 1;
                            break;
                        }
                    }
                    continue;
                }
            }
 
            i = quoteEnd + 1;
        }
 
        return keys;
    }
 
    // Escapar caracteres especiales para JSON
    private static String escapeJson(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\")
                   .replace("\"", "\\\"")
                   .replace("\n", "\\n")
                   .replace("\r", "\\r")
                   .replace("\t", "\\t");
    }
 
    // =======================================================
    // LOG
    // =======================================================
    private static void log(String message) {
        System.out.println("[JsonManager] " + message);
    }
}
