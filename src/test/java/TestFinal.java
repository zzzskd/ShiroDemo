import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

public class TestFinal {
    private final int test;

    public TestFinal(int test) {
        this.test = test;
    }
    public int getTest() {
        return test;
    }

    public static void main(String[] args) throws NoSuchFieldException, IllegalAccessException {
        TestFinal t = new TestFinal(11111);
        Field testField = TestFinal.class.getDeclaredField("test");
        testField.setAccessible(true);
        testField.set(t, 222222);
        System.out.println(t.getTest());
    }
}
