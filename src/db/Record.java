package db;

import java.util.HashMap;

public class Record {

    final private HashMap<Column, String> columns = new HashMap<>();

    public int getColumnCount() {
        return columns.size();
    }

    private boolean deleteFlag;
    

    public Record(boolean deleteFlag, Column[] columnList) {
        this.deleteFlag = deleteFlag;
        for (Column column : columnList) {
            columns.put(column, null);
        }
    }

    public String getColumnValue(Column key) {
        if (columns.containsKey(key)) {
            return columns.get(key);
        } else {
            System.out.println("no such key; getRowValue()");
            return null;
        }
    }

    public HashMap<Column, String> getRows() {
        return columns;
    }

    public void setColumnValue(Column key, String value) {
        if (columns.containsKey(key)) {
            columns.put(key, value);
        } else {
            System.out.println("no such key; setRowValue");
        }
    }

    public boolean isDeleteFlag() {
        return deleteFlag;
    }

    public void setDeleteFlag(boolean deleteFlag) {
        this.deleteFlag = deleteFlag;
    }
}
