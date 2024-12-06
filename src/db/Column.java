package db;

import java.util.HashMap;

public class Column {

    final private HashMap<ColumnType, String> structure = new HashMap<>();

    public Column() {
        structure.put(ColumnType.Feldname, null);
        structure.put(ColumnType.Feldbeschreibung, null);
        structure.put(ColumnType.Laenge, null);
        structure.put(ColumnType.Typ, null);
    }

    public Column(String fn, String fb, String l, String typ) {
        structure.put(ColumnType.Feldname, fn);
        structure.put(ColumnType.Feldbeschreibung, fb);
        structure.put(ColumnType.Laenge, l);
        structure.put(ColumnType.Typ, typ);
    }

    public String getValue(ColumnType key) {
        if (structure.containsKey(key)) {
            return structure.get(key);
        }
        System.out.println("No such key; Returning null; getValue()");
        return null;
    }

    public void setValue(ColumnType key, String value) {
        if (structure.containsKey(key)) {
            structure.put(key, value);
        } else {
            System.out.println("No such key; setValue()");
        }
    }

    public ColumnType[] getKeys() {
        return structure.keySet().toArray(new ColumnType[0]);
    }

    public int getLength() {
        return Integer.parseInt(structure.get(ColumnType.Laenge));
    }

    @Override
    public String toString() {
        return "Name: " + structure.get(ColumnType.Feldname) +
               " Beschreibung: " + structure.get(ColumnType.Feldbeschreibung) +
               " Typ: " + structure.get(ColumnType.Typ) +
               " Laenge " + structure.get(ColumnType.Laenge);
    }
}
