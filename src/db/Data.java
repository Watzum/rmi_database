package db;

import exceptions.DuplicateKeyException;
import exceptions.RecordNotFoundException;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;


public class Data implements DB {

    private DBSchema schema;

    public DBSchema getSchema() {
        return schema;
    }

    private final ArrayList<Record> recordList = new ArrayList<>();

    public ArrayList<Record> getRecordList() {
        return recordList;
    }

    public Record getRecordListAt(int recNo) {
        return recordList.get(recNo);
    }

    private final HashMap<Integer, Long> lockedRecords = new HashMap<>();

    private final File dbFile;

    public Data(String filePath) throws FileNotFoundException {
        dbFile = new File(filePath);
        if (!dbFile.exists()) {
            throw new FileNotFoundException();
        }
        createSchema();
    }


    //mode = read: "r", read/write: "rw"
    private RandomAccessFile getRAFAt(int position, String mode) {
        RandomAccessFile raf = null;
        try {
            raf = new RandomAccessFile(dbFile, mode);
            raf.seek(position);
            return raf;
        } catch (Exception ex) {
            ex.printStackTrace();
            try {
                raf.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }


    private void createSchema() {
        int[] header = getSchemaHeader();
        schema = new DBSchema(header[0], header[1], header[2]);
        createRowsFromFile();
        createRecordsFromFile();
    }


    private int[] getSchemaHeader() {
        RandomAccessFile raf = null;
        try {
            raf = getRAFAt(0, "r");
            int magicCookie = 0;
            if (raf != null) {
                magicCookie = raf.readInt();
            }
            int offset = 0;
            if (raf != null) {
                offset = raf.readInt();
            }
            int columnCount = 0;
            if (raf != null) {
                columnCount = raf.readShort();
            }
            return new int[] {magicCookie, offset, columnCount};
        } catch (IOException IOex) {
            IOex.printStackTrace();
            return new int[] {};
        } finally {
            try {
                if (raf != null) {
                    raf.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    private void createRowsFromFile() {
        RandomAccessFile raf = null;
        try {
            raf = getRAFAt(10, "r"); //4+4+2 Header
            int fieldLength;
            for (int i = 0; i < schema.getColumnCount(); i++) {
                Column column = new Column();
                if (raf != null) {
                    fieldLength = raf.readShort();
                    setRowValue(raf, fieldLength, ColumnType.Feldname, column);
                    fieldLength = raf.readShort();
                    setRowValue(raf, fieldLength, ColumnType.Feldbeschreibung, column);
                    setRowValue(raf, 1, ColumnType.Typ, column);
                    fieldLength = raf.readShort();
                    column.setValue(ColumnType.Laenge, String.valueOf(fieldLength));
                }
                schema.setColumnListAt(i, column);
            }
        } catch (IOException IOex) {
            IOex.printStackTrace();
        } finally {
            try {
                if (raf != null) {
                    raf.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }


    private void setRowValue(RandomAccessFile raf, int length,
                             ColumnType type, Column r) throws IOException {
       byte[] fieldBytes = new byte[length];
       raf.read(fieldBytes);
       r.setValue(
               type,
               new String(fieldBytes, StandardCharsets.ISO_8859_1)
       );
    }


    private void createRecordsFromFile() {
        RandomAccessFile raf = null;
        try {
            raf = getRAFAt(schema.getOffSet(), "r");
            int numberOfBytesLeft;
            if (raf != null) {
                numberOfBytesLeft = (int)raf.getChannel().size();
                int recordCount = numberOfBytesLeft / (schema.getRecordLength() + 2);
                for (int i = 0; i < recordCount; i++) {
                    createRecordFromFile(raf);
                }
            }

        } catch (IOException IOex) {
            IOex.printStackTrace();
        } finally {
            try {
                if (raf != null) {
                    raf.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    private void createRecordFromFile(RandomAccessFile raf) throws IOException {
        Record record;
        if (raf.readShort() == 0) {
            record = new Record(true, schema.getColumnList());
        } else {
            record = new Record(false, schema.getColumnList());
        }
        byte[] fieldBytes;
        for (Column column : schema.getColumnList()) {
            fieldBytes = new byte[
                    Integer.parseInt(column.getValue(ColumnType.Laenge))];
            raf.read(fieldBytes);
            record.setColumnValue(
                    column,
                    new String(fieldBytes, StandardCharsets.ISO_8859_1
                    ));
        }
        recordList.add(record);
    }


    public static String removeUnwantedCharactersOf(String s) {
        s = s.replace((char) 0, ' ');
        s = s.stripTrailing();
        s = s.stripLeading();
        return s;
    }


    public int getPosInFileOf(int RecordNumber) {
        RandomAccessFile raf = null;
        try {
            int position = schema.getOffSet() +
                    (RecordNumber * (schema.getRecordLength() + 2));
            raf = getRAFAt(0, "r");
            int numberOfBytesLeft = 0;
            if (raf != null) {
                numberOfBytesLeft = (int)raf.getChannel().size();
            }
            if (position > numberOfBytesLeft) {
                throw new RecordNotFoundException(
                        "Record Number is higher than the number of records"
                );
            } else if (RecordNumber < 0) {
                throw new RecordNotFoundException(
                        "Record number can not be lower than 0"
                );
            } else {
                return position;
            }
        } catch (IOException IOex) {
            IOex.printStackTrace();
            return -1;
        } finally {
            try {
                if (raf != null) {
                    raf.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    @Override
    public String[] read(int recNo) throws RecordNotFoundException {
        isRecNoValid(recNo);
        Column[] columnList = schema.getColumnList();
        String[] recordValues = new String[columnList.length];
        Record record = recordList.get(recNo);
        for (int i = 0; i < columnList.length; i++) {
            recordValues[i] = removeUnwantedCharactersOf(
                                record.getColumnValue(columnList[i]));
        }
        return recordValues;
    }


    @Override
    public void update(int recNo, String[] data, long lockCookie)
            throws RecordNotFoundException, SecurityException {
        if (lockedRecords.containsKey(recNo) && lockedRecords.get(recNo) != lockCookie) {
            throw new SecurityException();
        }
        try {
            if (recNo >= 0 && recNo < recordList.size()) {
                Record record = recordList.get(recNo);
                Column[] columnList = schema.getColumnList();
                for (int i = 0; i < columnList.length; i++) {
                    if (columnList[i].getValue(ColumnType.Feldname).equals("x") ||
                            columnList[i].getValue(ColumnType.Feldname).equals("y")) {
                        String formattedIntString =  getFormattedIntString(data[i],
                                Integer.parseInt(columnList[i].getValue(ColumnType.Laenge)));
                        record.setColumnValue(columnList[i], formattedIntString);
                    } else {

                        String formattedString = getFormattedString(data[i],
                                Integer.parseInt(columnList[i].getValue(ColumnType.Laenge)));

                        record.setColumnValue(columnList[i], formattedString);
                    }
                }
                writeUpdatedRecord(recNo);
            } else {
                throw new RecordNotFoundException(
                        "Record Number is higher than the number of records" +
                                "/lower than 0"
                );
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void writeUpdatedRecord(int recNo) throws IOException {
        int filePosition = getPosInFileOf(recNo);
        RandomAccessFile raf = getRAFAt(filePosition, "rw");
        Record record = recordList.get(recNo);
        if (raf != null) {
            if (record.isDeleteFlag()) {
                raf.writeShort(0);
            } else {
                raf.writeShort(1);
            }
            for (Column column : schema.getColumnList()) {
                raf.write(record.getColumnValue(column).getBytes(StandardCharsets.ISO_8859_1));
            }
        }
        raf.close();
    }


    private String getFormattedIntString(String s, int len) {
        StringBuilder newString = new StringBuilder();
        int diff = len - s.length();
        newString.append(String.valueOf((char) 0).repeat(Math.max(0, diff)));

        for (int i = 0; i < (len - diff); i++) {
            newString.append(s.charAt(i));
        }
        return newString.toString();
    }


    private String getFormattedString(String s, int len) {
        StringBuilder newString = new StringBuilder();
        for (int i = 0; i < len; i++) {
            if (i < s.length()) {
                newString.append(s.charAt(i));
            } else {
                newString.append((char) 0);
            }
        }
        return newString.toString();
    }


    private boolean isRecNoValid(int recNo) throws RecordNotFoundException {
        if (recNo < 0) {
            throw new RecordNotFoundException(
                    "Record number can not be lower than 0"
            );
        } else if (recNo > recordList.size()) {
            throw new RecordNotFoundException(
                    "Record Number is higher than the number of records"
            );
        } else if (recordList.get(recNo).isDeleteFlag()) {
            throw new RecordNotFoundException(
                    "The record is deleted"
            );
        } else {
            return true;
        }
    }

    @Override
    public void delete(int recNo, long lockCookie) throws RecordNotFoundException, SecurityException {
        if (lockedRecords.containsKey(recNo) && lockedRecords.get(recNo) != lockCookie) {
            throw new SecurityException();
        }
        if (isRecNoValid(recNo)) {
            String[] recordData = read(recNo);
            Record record = recordList.get(recNo);
            record.setDeleteFlag(true);
            update(recNo, recordData, lockCookie);
        }
        lockedRecords.remove(recNo);
    }

    @Override
    public int[] find(String[] criteria) {
        Column[] columnList = schema.getColumnList();
        int[] results = new int[recordList.size()];
        int resultsPointer = 0;
        boolean recordFits;
        String recordValue;
        for (int i = 0; i < recordList.size(); i++) {
            if (!recordList.get(i).isDeleteFlag()) {
                recordFits = true;
                for (int j = 0; j < columnList.length; j++) {
                    if (criteria[j] != null) {
                        recordValue = recordList.get(i).getColumnValue(columnList[j]);
                        if (!recordValue.contains(criteria[j])) {
                            recordFits = false;
                        }
                    }
                }
                if (recordFits) {
                    results[resultsPointer] = i;
                    resultsPointer++;
                }
            }
        }
        return Arrays.copyOfRange(results, 0, resultsPointer);
    }

    @Override
    public int create(String[] data) throws DuplicateKeyException {
        Record firstDeletedRecord = getFirstDeletedRecord();
        if (firstDeletedRecord != null) {
            firstDeletedRecord.setDeleteFlag(false);
            int recNo = recordList.indexOf(firstDeletedRecord);
            update(recNo, data, 123);
            return recNo;
        } else {
            appendRecordToFile(data);
            try {
                createRecordFromFile(
                        Objects.requireNonNull(getRAFAt(
                                getPosInFileOf(
                                        recordList.size()
                                ), "r"
                        ))
                );
            return recordList.size() + 1;
            } catch (IOException IOex) {
                return -1;
            }
        }
    }

    private void appendRecordToFile(String[] data) {
        int fileLength = getPosInFileOf(recordList.size() - 1) + (schema.getRecordLength() + 2);
        RandomAccessFile raf = null;
        try {
            raf = getRAFAt(fileLength, "rw");
            if (raf != null) {
                raf.writeShort(1);
                int i = 0;
                int length;
                String rowData;
                for (Column column : schema.getColumnList()) {
                    if (column.getValue(ColumnType.Typ).equals("F")) {
                        length = Integer.parseInt(column.getValue(ColumnType.Laenge));
                        rowData = getFormattedIntString(data[i], length);
                    } else {
                        length = Integer.parseInt(column.getValue(ColumnType.Laenge));
                        rowData = getFormattedString(data[i], length);
                    }
                    raf.write(rowData.getBytes(StandardCharsets.ISO_8859_1));
                    i++;
                }
                raf.close();
            }
        } catch (IOException IOe) {
            IOe.printStackTrace();
        } finally {
            try {
                if (raf != null) {
                    raf.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private Record getFirstDeletedRecord() {
        for (Record record : recordList) {
            if (record.isDeleteFlag()) {
                return record;
            }
        }
        return null;
    }

    @Override
    public long lock(int recNo) throws RecordNotFoundException {
        isRecNoValid(recNo);
        try {
            synchronized (lockedRecords) {
                while (lockedRecords.containsKey(recNo)) {
                    lockedRecords.wait();
                }
                long lockCookie = schema.getLockCookie();
                lockedRecords.put(recNo, lockCookie);
                return lockCookie;
            }
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
        return -1;
    }

    @Override
    public void unlock(int recNo, long cookie) throws RecordNotFoundException, SecurityException {
        isRecNoValid(recNo);
        synchronized (lockedRecords) {
            if (lockedRecords.containsKey(recNo)) {
                if (lockedRecords.get(recNo) == cookie) {
                    lockedRecords.remove(recNo);
                    lockedRecords.notifyAll();
                } else {
                    throw new SecurityException("LockCookie is wrong!");
                }
            } else {
                System.out.println("Record " + recNo + " is not locked!");
            }
        }
    }

    public HashMap[] getColumnInformation() {
        HashMap<String, String>[] columnInformation;
        Column[] columns = schema.getColumnList();
        ColumnType[] columnInfos = columns[0].getKeys();
        columnInformation = new HashMap[recordList.get(0).getColumnCount()];
        for (int i = 0; i < columns.length; i++) {
            HashMap<String, String> map = new HashMap<>();
            for (ColumnType t : columnInfos) {
                map.put(t.toString(), columns[i].getValue(t));
            }
            columnInformation[i] = map;
        }
        return columnInformation;
    }

    public String[][] readAllRecords() {
        Column[] columnList = schema.getColumnList();
        String[][] allRecords =
                new String[getRecordCountWithoutDeletedRecords()]
                        [columnList.length];
        int i = 0;
        for (Record record : recordList) {
            if (record.isDeleteFlag() == false) {
                String[] recordValues = new String[columnList.length];
                for (int j = 0; j < columnList.length; j++) {
                    recordValues[j] = removeUnwantedCharactersOf(
                            record.getColumnValue(columnList[j]));
                }
                allRecords[i] = recordValues;
                i++;
            }
        }
        return allRecords;
    }

    //starts with 1 (not 0)
    public int getRecordCountWithoutDeletedRecords() {
        int deletedRecords = getRecNoWithDeletedRecords(recordList.size() -1) -
                (recordList.size() -1);
        return recordList.size() - deletedRecords;
    }

    //Converts the Rowcount of the table (MainView) to the index in the
    //record list (taking into account the deleted records)
    public int getRecNoWithDeletedRecords(int recNo) {
        for (int i = 0; i < recordList.size() && i <= recNo; i++) {
            if (recordList.get(i).isDeleteFlag()) {
                recNo++;
            }
        }
        return recNo;
    }
}