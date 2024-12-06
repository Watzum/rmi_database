package test;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import rmi.DataRMI;
import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;
import static org.junit.jupiter.api.Assertions.*;
import static test.DataTest.*;

class DataRMITest {

    DataRMI data;

    @BeforeEach
    void setUp() {
        try {
            try {
                copyFile(new File("./src/machines.db"),
                        new File("./src/machines2.db"));
            } catch (IOException e) {
                e.printStackTrace();
            }
            data = new DataRMI("./src/machines.db");
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
    void read() {
        String[] testArray01 =
                new String[] {"", "", "", ""};
        String[] testArray02 =
                new String[] {"wlr1", "Werk Laderoboter 1", "50", "50"};
        String[] testArray03 =
                new String[] {"wlr2", "Werk Laderoboter 2", "200", "50"};
        String[] testArray04 =
                new String[] {"wsr1", "Werk Schweissroboter 2", "50", "250"};

        try {
            assertArrayEquals(testArray02, data.read(0));
            assertArrayEquals(testArray03, data.read(1));
            assertTrue(assertArrayNotEquals(testArray04, data.read(3)));
            assertTrue(assertArrayNotEquals(testArray01, data.read(1)));
        } catch (Exception e) {
            fail("Throwed Exception " + e);
        }
    }

    @Test
    void update() {
        String[] testArray02 = new String[] {"wlr44", "Werk", "444", "444"};
        String[] testArray03 = new String[] {"wlr22", "Werk", "50000", "4444"};
        String[] testArray04 = new String[] {"wsr33", "WerkSchweiss", "3", ""};

        try {
            data.update(1, testArray02, 123);
            data.update(4, testArray03, 123);
            data.update(5, testArray04, 123);
        } catch (Exception e) {
            fail("Throwed Exception " + e);
        }
        assertThrows(RemoteException.class, () ->
                data.update(20, testArray04, 123));
        assertThrows(RemoteException.class, () ->
                data.update(-1, testArray04, 123));
        try {
            assertArrayEquals(testArray02, data.read(1));
            assertArrayEquals(testArray03, data.read(4));
            assertArrayEquals(testArray04, data.read(5));
            assertTrue(assertArrayNotEquals(data.read(1), testArray03));
        } catch (Exception e) {
            fail("Throwed Exception " + e);
        }
    }

    @Test
    void delete() {
        try {
            data.delete(1, 123);
            assertThrows(RemoteException.class, () ->
                    data.read(1));
            assertThrows(RemoteException.class, () ->
                    data.read(11));
        } catch (Exception e) {
            fail("Throwed Exception " + e);
        }
    }

    @Test
    void find() {
        String[] criteriaArray01 = new String[]{"w", null, null, null};
        int[] resultArray01 = new int[]{0,1,2,3,4,5,6};

        String[] criteriaArray02 = new String[]{null, null, null, null};
        int[] resultArray02 = new int[]{0,1,2,3,4,5,6,7,8,9};

        String[] criteriaArray03 = new String[]{
                "srv3", "Backupserver", "500", "250"
        };
        int[] resultArray03 = new int[]{9};

        try {
            assertArrayEquals(data.find(criteriaArray01), resultArray01);
            assertArrayEquals(data.find(criteriaArray02), resultArray02);
            assertArrayEquals(data.find(criteriaArray03), resultArray03);
            assertTrue(assertArrayNotEquals(data.find(criteriaArray01), resultArray03));
        } catch (Exception e) {
            fail("Throwed Exception where it should not");
        }
    }

    @Test
    void create() {
        String[] createData01 =
                new String[]{"qwer1", "LALULUALALAL", "999", "50000"};
        String[] createData02 =
                new String[]{"abcde", "Dataset02", "0", "0"};
        String[] createData03 =
                new String[]{"Dasta02", "Datasatz0503", "1", "5"};
        String[] createData04 =
                new String[]{"satzen04", "SATZENDATA1", "11111", "3939"};

        try {
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
        } catch (Exception e) {
            fail("Throwed Exception " + e);
        }
    }

    @Test
    void lock() {
        assertThrows(RemoteException.class, () ->
                data.lock(11));
        try {
            data.lock(2);
        } catch (RemoteException e) {
            fail("Throwed Exception " + e);
        }

        Thread t1 = new Thread(() -> {
            try {
                data.lock(2);
            } catch (RemoteException e) {
                fail("Throwed Exception " + e);
            }
        });
        t1.start();
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        assertSame(t1.getState(), Thread.State.WAITING);
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
            } catch (RemoteException e) {
                fail("testLock: should not have thrown RemoteException");
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
            } catch (RemoteException e) {
                fail("testLock: should not have thrown RemoteException");
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
}