package server;

import java.util.ArrayList;
import java.util.LinkedList;

/**
 * @author nano
 */
public class Commands {
    public final String sessionsTitle = ("上线主机【Active sessions】\n===============\n");

    private static String type = "";

    public static Object OSSClient;

    public static ArrayList<LinkedList> sessionsList = new ArrayList<>();

    public static String endpoint = "";

    public static String bucketname = "";

    public static String path = "";

    public static String accessKeyId = "";

    public static String accessKeySecret = "";

    public static String token = " ";

    public static boolean commandstate = true;

    public String getSessionsTitle() {
        return sessionsTitle;
    }

    public static String getType() {
        return type;
    }

    public static void setType(String type) {
        Commands.type = type;
    }

    public static Object getOSSClient() {
        return OSSClient;
    }

    public static void setOSSClient(Object OSSClient) {
        Commands.OSSClient = OSSClient;
    }

    public static ArrayList<LinkedList> getSessionsList() {
        return sessionsList;
    }

    public static void setSessionsList(ArrayList<LinkedList> sessionsList) {
        Commands.sessionsList = sessionsList;
    }

    public static String getEndpoint() {
        return endpoint;
    }

    public static void setEndpoint(String endpoint) {
        Commands.endpoint = endpoint;
    }

    public static String getBucketname() {
        return bucketname;
    }

    public static void setBucketname(String bucketname) {
        Commands.bucketname = bucketname;
    }

    public static String getPath() {
        return path;
    }

    public static void setPath(String path) {
        Commands.path = path;
    }

    public static String getAccessKeyId() {
        return accessKeyId;
    }

    public static void setAccessKeyId(String accessKeyId) {
        Commands.accessKeyId = accessKeyId;
    }

    public static String getAccessKeySecret() {
        return accessKeySecret;
    }

    public static void setAccessKeySecret(String accessKeySecret) {
        Commands.accessKeySecret = accessKeySecret;
    }

    public static String getToken() {
        return token;
    }

    public static void setToken(String token) {
        Commands.token = token;
    }

    public static boolean isCommandstate() {
        return commandstate;
    }

    public static void setCommandstate(boolean commandstate) {
        Commands.commandstate = commandstate;
    }
}
