package test;

import db.ColumnType;
import db.Data;
import db.Column;
import exceptions.RecordNotFoundException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.channels.FileChannel;
import java.util.HashMap;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

class DataTest {

    Data data;

    @BeforeEach
    void setUp() {
        try {
            try {
                data = null;
                copyFile(new File("./src/machines.db"),
                        new File("./src/machines2.db"));
            } catch (IOException e) {
                e.printStackTrace();
            }
            data = new Data("./src/machines.db");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @AfterEach
    void tearDown() {
        try {
            copyFile(new File("./src/machines2.db"),
                    new File("./src/machines.db"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Test
    void getSchemaTest() {
        assertEquals(data.getSchema().getMagicCookie(), 4242);
        assertEquals(data.getSchema().getOffSet(), 67);
        assertEquals(data.getSchema().getColumnCount(), 4);
    }

    @Test
    void createRowsTest() {
        assertEquals(
                "id",
                data.getSchema().getColumnList()[0].getValue(ColumnType.Feldname)
        );

        assertEquals(
                "name",
                data.getSchema().getColumnList()[1].getValue(ColumnType.Feldname)
        );

        assertEquals(
                "Gerätename",
                data.getSchema().getColumnList()[1].getValue(ColumnType.Feldbeschreibung)
        );

        assertEquals(
                "F",
                data.getSchema().getColumnList()[3].getValue(ColumnType.Typ)
        );

        assertEquals(
                "5",
                data.getSchema().getColumnList()[2].getValue(ColumnType.Laenge)
        );
    }

    @Test
    void getRecordLengthTest() {
        assertEquals(120, data.getSchema().getRecordLength());
    }

    @Test
    void createRecordsFromFileTest() {
        HashMap<Column, String> recordMap01 =
                data.getRecordListAt(0).getRows();
        assertEquals(
                "   50",
                recordMap01.get(data.getSchema().getColumnList()[3])
        );

        HashMap<Column, String> recordMap02 =
                data.getRecordListAt(1).getRows();
        assertEquals(
                "  200",
                recordMap02.get(data.getSchema().getColumnList()[2])
        );

        HashMap<Column, String> recordMap03 =
                data.getRecordListAt(2).getRows();
        assertEquals(
                "wlr3",
                recordMap03.get(data.getSchema().getColumnList()[0]).substring(0, 4)
        );
    }

    @Test
    void readTest() {
        String[] testArray01 =
                new String[] {"", "", "", ""};
        String[] testArray02 =
                new String[] {"wlr1", "Werk Laderoboter 1", "50", "50"};
        String[] testArray03 =
                new String[] {"wlr2", "Werk Laderoboter 2", "200", "50"};
        String[] testArray04 =
                new String[] {"wsr1", "Werk Schweissroboter 2", "50", "250"};

        assertArrayEquals(testArray02, data.read(0));
        assertArrayEquals(testArray03, data.read(1));
        assertTrue(assertArrayNotEquals(testArray04, data.read(3)));
        assertTrue(assertArrayNotEquals(testArray01, data.read(1)));
    }

    @Test
    void updateTest() {
        String[] testArray02 = new String[] {"wlr44", "Werk", "444", "444"};
        String[] testArray03 = new String[] {"wlr22", "Werk", "50000", "4444"};
        String[] testArray04 = new String[] {"wsr33", "WerkSchweiss", "3", ""};

        data.update(1, testArray02, 123);
        data.update(4, testArray03, 123);
        data.update(5, testArray04, 123);

        assertThrows(RecordNotFoundException.class, () ->
                data.update(20, testArray04, 123));
        assertThrows(RecordNotFoundException.class, () ->
                data.update(-1, testArray04, 123));
        assertArrayEquals(testArray02, data.read(1));
        assertArrayEquals(testArray03, data.read(4));
        assertArrayEquals(testArray04, data.read(5));
        assertTrue(assertArrayNotEquals(data.read(1), testArray03));
    }

    @Test
    void deleteTest() {
        try {
            data.delete(1, 123);
            assertTrue(data.getRecordListAt(1).isDeleteFlag());
            assertThrows(RecordNotFoundException.class, () ->
                    data.read(1));
            assertThrows(RecordNotFoundException.class, () ->
                    data.read(11));
        } catch (Exception e) {
            fail("Throwed Exception " + e);
        }
    }

    @Test
    void findTest() {
        String[] criteriaArray01 = new String[]{"w", null, null, null};
        int[] resultArray01 = new int[]{0,1,2,3,4,5,6};

        String[] criteriaArray02 = new String[]{null, null, null, null};
        int[] resultArray02 = new int[]{0,1,2,3,4,5,6,7,8,9};

        String[] criteriaArray03 = new String[]{
                "srv3", "Backupserver", "500", "250"
        };
        int[] resultArray03 = new int[]{9};

        assertArrayEquals(data.find(criteriaArray01), resultArray01);
        assertArrayEquals(data.find(criteriaArray02), resultArray02);
        assertArrayEquals(data.find(criteriaArray03), resultArray03);
        assertTrue(assertArrayNotEquals(data.find(criteriaArray01), resultArray03));

    }

    @Test
    void createTest() {
        String[] createData01 =
                new String[]{"qwer1", "LALULUALALAL", "999", "50000"};
        String[] createData02 =
                new String[]{"abcde", "Dataset02", "0", "0"};
        String[] createData03 =
                new String[]{"Dasta02", "Datasatz0503", "1", "5"};
        String[] createData04 =
                new String[]{"satzen04", "SATZENDATA1", "11111", "3939"};

        data.delete(3, 123);
        data.create(createData01);
        assertArrayEquals(createData01, data.read(3));
        data.create(createData02);
        assertArrayEquals(createData02, data.read(10));
        data.delete(10, 123);
        data.create(createData03);
        assertArrayEquals(createData03, data.read(10));
        data.create(createData04);
        assertArrayEquals(createData04, data.read(11));
    }


    @Test
    void lockTest() {
        assertThrows(RecordNotFoundException.class, () ->
                data.lock(11));
        long lockcookie = data.lock(2);

        Thread t1 = new Thread(() -> data.lock(2));
        t1.start();
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        assertSame(t1.getState(), Thread.State.WAITING);
        data.unlock(2, lockcookie);
    }

    @Test
    void unlockTest() {
        try {
            long cookie = data.lock(1);
            data.unlock(1, cookie);
        } catch (Exception e) {
            fail("Throwed Exception " + e);
        }
        data.lock(1);
        assertThrows(SecurityException.class, () ->
                data.unlock(1, 1));
    }

    @Test
    void threadExceptions() {
        final RunnableCatch tr1 = new RunnableCatch(() -> {
            try {
                try {
                    Thread.sleep(500); // first wait
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                long cookie = data.lock(1);
                assertTrue(cookie != 0);
                for (int i = 0; i < 10000; i++) {
                    @SuppressWarnings("unused")
                    double y = Math.log((double)i);
                }
                data.unlock(1, cookie);
            } catch (SecurityException e) {
                fail("testLock: should not have thrown SecurityException");
            } catch (RecordNotFoundException e) {
                fail("testLock: should not have thrown RecordNotFoundException");
            }
        });
        final Thread t1 = new Thread(tr1, "thread 1");
        final RunnableCatch tr2 = new RunnableCatch(() -> {
            try {
                long cookie = data.lock(1);
                try {
                    Thread.sleep(1000); // first wait
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                assertTrue(cookie != 0);
                assertSame(t1.getState(), Thread.State.WAITING);
                data.unlock(1, cookie);
            } catch (SecurityException e) {
                fail("testLock: should not have thrown SecurityException");
            } catch (RecordNotFoundException e) {
                fail("testLock: should not have thrown RecordNotFoundException");
            }
        });
        final Thread t2 = new Thread(tr2, "thread 2");
        t1.start();
        t2.start();
        try {
            t1.join();
            assertNull(tr1.getException());
            t2.join();
            assertNull(tr2.getException());
        } catch (InterruptedException e) {
            System.err.println(e.getLocalizedMessage());
        }
    }

    @Test
    void getRecNoWithDeletedRecordsTest() {
        assertEquals(1, data.getRecNoWithDeletedRecords(1));
        assertEquals(0, data.getRecNoWithDeletedRecords(0));
        assertEquals(10, data.getRecNoWithDeletedRecords(10));
        data.delete(1, 1);
        assertEquals(2, data.getRecNoWithDeletedRecords(1));
        data.delete(9, 1);
        assertEquals(11, data.getRecNoWithDeletedRecords(9));
        data.delete(0, 1);
        assertEquals(2, data.getRecNoWithDeletedRecords(0));
    }

    @Test
    void getRecordCountWithoutDeletedRecordsTest() {
        assertEquals(10, data.getRecordCountWithoutDeletedRecords());
        data.delete(0, 1);
        assertEquals(9, data.getRecordCountWithoutDeletedRecords());
        data.delete(9,1);
        assertEquals(8, data.getRecordCountWithoutDeletedRecords());
        data.delete(1,1);
        data.delete(2,1);
        data.delete(3,1);
        data.delete(4,1);
        data.delete(5,1);
        data.delete(6,1);
        data.delete(7,1);
        data.delete(8,1);
        assertEquals(0, data.getRecordCountWithoutDeletedRecords());
    }

    static boolean assertArrayNotEquals(String[] a, String[] b) {
        int length = Math.max(a.length, b.length);
        for (int i = 0; i < length; i++) {
            if (!Objects.equals(a[i], b[i])) {
                return true;
            }
        }
        return false;
    }

    static boolean assertArrayNotEquals(int[] a, int[] b) {
        int length = Math.max(a.length, b.length);
        for (int i = 0; i < length; i++) {
            if (a[i] != b[i]) {
                return true;
            }
        }
        return false;
    }

    static void copyFile(File sourceFile, File destFile) throws IOException {
        FileChannel source = null;
        FileChannel destination = null;
        try {
             source = new FileInputStream(sourceFile).getChannel();
             destination =
                     new FileOutputStream(destFile).getChannel();
                destination.transferFrom(source, 0, source.size());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (source != null) {
                source.close();
            }
            if (destination != null) {
                destination.close();
            }
        }
    }
}