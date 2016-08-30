package de.dhge.ar.arnavigator.util;

import java.util.ArrayList;
import java.util.List;

public class ScanResult {

    final static String SAVED_ENTRY_TAG = "OBJECTS";

    private String type;
    private String name;
    private String content;

    public ScanResult(String type, String name, String content) {
        this.type = type;
        this.name = name;
        this.content = content;
    }

    // builds ScanResult from SaveFormat
    public static ScanResult fromSaveFormat(String savedEntry) {
        String[] parts = savedEntry.split("~");
        return new ScanResult(parts[0], parts[1], parts[2]);
    }

    // gets ScanResults from databse
    public static List<ScanResult> getAll(TinyDB database) {
        List<String> result = database.getListString(SAVED_ENTRY_TAG);
        List<ScanResult> scanResults = new ArrayList<>();

        for (String item : result) {
            scanResults.add(fromSaveFormat(item));
        }

        return scanResults;
    }

    // save ScanResults to database
    public static void saveScanResults(TinyDB database, List<ScanResult> results) {
        ArrayList<String> entries = new ArrayList<>();

        for (ScanResult result : results) {
            entries.add(result.getSaveFormat());
        }

        database.putListString(SAVED_ENTRY_TAG, entries);
    }

    public String getType() {
        return this.type;
    }

    public String getName() {
        return this.name;
    }

    public String getContent() {
        return this.content;
    }

    public String getSaveFormat() {
        return String.format("%s~%s~%s", this.type, this.name, this.content);
    }
}
