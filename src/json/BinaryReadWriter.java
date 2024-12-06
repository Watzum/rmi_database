package json;

import db.Column;
import db.ColumnType;
import db.Record;

import javax.management.monitor.StringMonitorMBean;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.util.Random;

public class BinaryReadWriter {

    DataClass dataClass;
    RandomAccessFile raf;
    File currentFile;

    public DataClass createDataFromFile(File f) throws IOException {
        dataClass = new DataClass();
        currentFile = f;
        setHeader();
        setData();
        return dataClass;
    }

    private void setData() throws IOException {
        raf.seek(dataClass.offSet);
        while (raf.getChannel().size() > 0) {
            if (raf.readShort() == 0) {
                boolean isDeleted = true;
            } else {
                boolean isDeleted = false;
            }

        }
    }

    private void setColumns() throws IOException {
        for (Column c : dataClass.fields) {
            byte[] dataBytes =
                    new byte[Integer.parseInt(c.getValue(ColumnType.Laenge))];
            raf.read(dataBytes);
        }
    }

    private void setHeader() throws IOException {
        raf = new RandomAccessFile(currentFile, "r");
        dataClass.magicCookie = raf.readInt();
        dataClass.offSet = raf.readInt();
        dataClass.numberOfFields = raf.readShort();
        System.out.println(raf.getFilePointer());
        setFields();
    }

    private void setFields() throws IOException {
        for (int i = 0; i < dataClass.numberOfFields; i++) {
            short length = raf.readShort();
            byte[] nameBytes = new byte[length];
            raf.read(nameBytes);
            String name = new String(nameBytes, StandardCharsets.ISO_8859_1);
            length = raf.readShort();
            byte[] descriptionBytes = new byte[length];
            raf.read(descriptionBytes);
            String description = new String(descriptionBytes, StandardCharsets.ISO_8859_1);
            String typ = String.valueOf(raf.readByte());
            String dataLength = String.valueOf(raf.readShort());

            Column column = new Column(name, description, dataLength, typ);
            dataClass.fields.add(column);
            System.out.println(column);
        }
    }
}