package db;

import java.util.ArrayList;

import static java.lang.System.currentTimeMillis;

public class DBSchema {

    public DBSchema(int magicCookie, int offset, int columnCount) {
        this.offset = offset;
        this.columnCount = columnCount;
        this.magicCookie = magicCookie;

        this.columnList = new Column[columnCount];
        this.lockCookie = currentTimeMillis();
    }

    private final int offset;
    private final int columnCount;
    private final int magicCookie;
    private long lockCookie;

    private final Column[] columnList;

    //gets record length WITHOUT delete flag
    public int getRecordLength() {
        int length = 0;
        for (Column column : columnList) {
            length += Integer.parseInt(column.getValue(ColumnType.Laenge));
        }
        return length;
    }

    public Column[] getColumnList() {
        return columnList;
    }

    public void setColumnListAt(int index, Column column) {
        this.columnList[index] = column;
    }


    public int getOffSet() {
        return offset;
    }


    public int getColumnCount() {
        return columnCount;
    }


    public int getMagicCookie() {
        return magicCookie;
    }

    public long getLockCookie() {
        lockCookie++;
        return lockCookie;
    }
}
