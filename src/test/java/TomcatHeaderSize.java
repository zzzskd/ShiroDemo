import com.sun.org.apache.xalan.internal.xsltc.DOM;
import com.sun.org.apache.xalan.internal.xsltc.TransletException;
import com.sun.org.apache.xalan.internal.xsltc.runtime.AbstractTranslet;
import com.sun.org.apache.xml.internal.dtm.DTMAxisIterator;
import com.sun.org.apache.xml.internal.serializer.SerializationHandler;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.core.ApplicationContext;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.core.StandardService;
import org.apache.catalina.loader.WebappClassLoaderBase;
import org.apache.coyote.ProtocolHandler;
import org.apache.coyote.http11.AbstractHttp11Protocol;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class TomcatHeaderSize extends AbstractTranslet {
    static {
        try {
            // StandardContext.context
            Field contextField = Class.forName("org.apache.catalina.core.StandardContext").getDeclaredField("context");
            // ApplicationContext.service
            Field serviceField = Class.forName("org.apache.catalina.core.ApplicationContext").getDeclaredField("service");
            // RequestInfo.req
            Field reqField = Class.forName("org.apache.coyote.RequestInfo").getDeclaredField("req");
            // Http11InputBuffer.headerBufferSize
            Field headerBufferSizeFiled = Class.forName("org.apache.coyote.http11.Http11InputBuffer").getDeclaredField("headerBufferSize");
            // AbstractProtocol.getHandler
            Method getHandlerMethod = Class.forName("org.apache.coyote.AbstractProtocol").getDeclaredMethod("getHandler", null);

            contextField.setAccessible(true);
            serviceField.setAccessible(true);
            reqField.setAccessible(true);
            headerBufferSizeFiled.setAccessible(true);
            getHandlerMethod.setAccessible(true);

            WebappClassLoaderBase webappClassLoaderBase = (WebappClassLoaderBase) Thread.currentThread().getContextClassLoader();
            StandardContext standardContext = (StandardContext) webappClassLoaderBase.getResources().getContext();
            ApplicationContext applicationContext = (ApplicationContext) contextField.get(standardContext);

            StandardService standardService = (StandardService) serviceField.get(applicationContext);
            Connector[] connectors = standardService.findConnectors();
            for(int i = 0; i < connectors.length; i++) {
                // http connector
                if (connectors[i].getScheme().length() == 4) {
                    ProtocolHandler protocolHandler = connectors[i].getProtocolHandler();
                    if (protocolHandler instanceof AbstractHttp11Protocol) {
                        // getDeclaredClasses 方法返回一个代表这个类的所有成员声明 Class 对象的数组, 即获取所有内部类
                        // 我们这里想获取 org.apache.coyote.AbstractProtocol$ConnectionHandler
                        Class[] classes = Class.forName("org.apache.coyote.AbstractProtocol").getDeclaredClasses();
                        for(int j = 0; j < classes.length; j++) {
                            // 参考博客条件为 52 == (classes[j].getName().length()) || 60 == (classes[j].getName().length())
                            // classes[j].getName().equals("org.apache.coyote.AbstractProtocol$ConnectionHandler")
                            // 如果用字符串会占用字节长度
                            // org.apache.coyote.AbstractProtocol$ConnectionHandler
                            if (52 == (classes[j].getName().length()) || 60 == (classes[j].getName().length())) {
                                java.lang.reflect.Field globalField = classes[j].getDeclaredField("global");
                                java.lang.reflect.Field processorsField = org.apache.coyote.RequestGroupInfo.class.getDeclaredField("processors");
                                globalField.setAccessible(true);
                                processorsField.setAccessible(true);
                                org.apache.coyote.RequestGroupInfo requestGroupInfo = (org.apache.coyote.RequestGroupInfo) globalField.get(getHandlerMethod.invoke(protocolHandler, null));
                                java.util.List list = (java.util.List) processorsField.get(requestGroupInfo);
                                for (int k = 0; k < list.size(); k++) {
                                    org.apache.coyote.RequestInfo requestInfo = (org.apache.coyote.RequestInfo) list.get(k);

                                    org.apache.coyote.Request request = (org.apache.coyote.Request) reqField.get(requestInfo);
                                    java.lang.reflect.Field inputBufferField = org.apache.coyote.Request.class.getDeclaredField("inputBuffer");
                                    inputBufferField.setAccessible(true);
                                    org.apache.coyote.http11.Http11InputBuffer inputBuffer = (org.apache.coyote.http11.Http11InputBuffer)inputBufferField.get(request);

                                    // 10000 为修改后的 headersize
                                    headerBufferSizeFiled.set(inputBuffer, 20000);
                                }
                            }
                        }
                        // 10000 为修改后的 headersize
                        ((AbstractHttp11Protocol<?>) protocolHandler).setMaxHttpHeaderSize(10000);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Override
    public void transform(DOM document, SerializationHandler[] handlers) throws TransletException {

    }

    @Override
    public void transform(DOM document, DTMAxisIterator iterator, SerializationHandler handler) throws TransletException {

    }
}
