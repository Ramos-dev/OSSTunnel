package server;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.ListObjectsRequest;
import com.aliyun.oss.model.OSSObjectSummary;
import com.amazonaws.ClientConfiguration;
import com.amazonaws.Protocol;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.S3ClientOptions;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.exception.CosClientException;
import com.qcloud.cos.exception.CosServiceException;
import com.qcloud.cos.http.HttpProtocol;
import com.qcloud.cos.model.COSObjectSummary;
import com.qcloud.cos.region.Region;
import org.springframework.core.annotation.Order;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import javax.validation.Valid;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

/**
 * @author nano
 */
@ShellComponent
@Order(1)
public class BucketCommands
        extends Commands {

    public static class Args {
        @Parameter(names = {"--type",
                "-t"}, order = 0, description = "【不同类型的云厂家，如阿里、腾讯、亚马逊】Different kind of Cloud,such as OSS(aliyun)\\COS(\\Tencent)\\S3(Amazon)", required = true)
        private String type = "";

        @Parameter(names = {"--endpoint",
                "-e"}, order = 1, description = "【endpoint地址，支持私有云和内部网络】endpoint address,support http and https", required = true)
        private String endpoint = "";

        @Parameter(names = {"--bucketname",
                "-b"}, order = 2, description = "【bucket名称】bucketname, you known", required = true)
        private String bucketname = "";

        @Parameter(names = {"--path", "-p"}, order = 3, description = "【bucket下的多级目录】path just like dir", required = false)
        private String path = "/";

        @Parameter(names = {"--accessKeyId", "-ak"}, order = 4, description = "accessKeyId", required = true, password = true)
        private String accessKeyId = "";

        @Parameter(names = {"--accessKeySecret",
                "-sk"}, order = 5, description = "accessKeySecret", required = true, password = true)
        private String accessKeySecret = "";

        //        @Parameter( names = { "--ststoken", "-t" }, order = 6, description = "token", required = false, password = true )
        //        private String token = "";

        @Parameter(names = {"--help", "-h"}, help = true, order = 8)
        private boolean help;

        @Parameter(names = {"--version",
                "-v"}, description = "【显示版本信息】Display version information", help = true, order = 9)
        private boolean v;

    }

    @ShellMethod("【远端对象存储配置】Config bucket connect and list active session manipulation and interaction... \nYou should have read permission to the bucket. \nUse config --help for more information")
    public String config(@ShellOption(optOut = true) @Valid Args args) {

        setBucketname(args.bucketname);
        setEndpoint(args.endpoint);
        setAccessKeyId(args.accessKeyId);
        setAccessKeySecret(args.accessKeySecret);
        setPath(args.path);
        setType(args.type.toUpperCase());
        JCommander jCommander = new JCommander(args);

        if (args.help) {
            jCommander.setProgramName("config");
            jCommander.usage();
            return "";
        }
        if (args.v) {
            return "Version : 2.0 (2020-10-06 21:44:14) On " + System.getProperty("os.name");
        }

        if ("OSS".equals(getType())) {
            OSS ossClient = OSSConn();
            if (ossClient != null) {
                return sessionsTitle + listOSSSessions(ossClient);
            } else {
                setCommandstate(false);
                return "【阿里云对象存储连接错误】Aliyun OSS Object-Based Storage Connect Error,may be wrong ak & sk?\n";

            }
        } else if ("COS".equals(getType())) {

            COSClient cosClient = COSConn();
            if (cosClient != null) {
                return sessionsTitle + listOSSSessions(cosClient);

            } else {
                setCommandstate(false);
                return "【腾讯云对象存储连接错误】Tencent COS Object-Based Storage Connect Error,may be wrong ak & sk?\n";

            }

        } else if ("S3".equals(getType())) {
            AmazonS3 s3Client = S3Conn();

            if (s3Client != null) {
                return sessionsTitle + listS3Sessions(s3Client);

            } else {
                setCommandstate(false);
                return "【亚马逊s3对象存储链接错误】S3 Object-Based Storage Connect Error,may be wrong ak & sk?\n";

            }

        }

        jCommander.usage();
        return "";
    }

    public String listOSSSessions(OSS ossClient) {

        sessionsList.clear();
        StringBuffer sessions = new StringBuffer("");

        com.aliyun.oss.model.ObjectListing objectListing = null;
        do {
            com.aliyun.oss.model.ListObjectsRequest request =
                    new ListObjectsRequest(getBucketname()).withDelimiter("").withPrefix(getPath().substring(1));
            if (objectListing != null) {
                request.setMarker(objectListing.getNextMarker());
            }
            objectListing = ossClient.listObjects(request);

            List<OSSObjectSummary> folders = objectListing.getObjectSummaries();

            //对folder的里的sum根据时间进行排序


            Collections.sort(folders, new Comparator<OSSObjectSummary>() {
                @Override
                public int compare(OSSObjectSummary b1, OSSObjectSummary b2) {
                    return b2.getLastModified().compareTo(b1.getLastModified());
                }
            });


            int i = 0;
            for (OSSObjectSummary sum : folders) {
                String metainfo =
                        ossClient.getObjectMetadata(getBucketname(), sum.getKey()).getUserMetadata().get(
                                "info");
                if (metainfo == null || metainfo.isEmpty()) {
                    continue;
                }
                i++;
                sessions.append(String.valueOf(i) + "\t");
                sessions.append(sum.getKey() + "\t");
                String meta = null;
                meta = decode(metainfo);

                sessions.append(meta + "\t");
                LinkedList linkedList = new LinkedList();
                linkedList.add(sum.getKey());
                linkedList.add(metainfo);
                sessionsList.add(linkedList);
                sessions.append(sum.getLastModified() + "\t");
                sessions.append(System.lineSeparator());
            }

        }
        while (objectListing.isTruncated());
        if (sessions.length() < 1) {
            sessions.append("\n【没有上线主机】No active sessions.");
        }
        setSessionsList(sessionsList);
        return sessions.toString();
    }


    public String listS3Sessions(AmazonS3 s3Client) {

        sessionsList.clear();
        StringBuffer sessions = new StringBuffer("");
        com.amazonaws.services.s3.model.ListObjectsRequest listObjectsRequest = new com.amazonaws.services.s3.model.ListObjectsRequest()
                .withBucketName(bucketname)
                //查询bucket下固定的前缀
                .withPrefix("");


        ObjectListing objectListing = s3Client.listObjects(listObjectsRequest);


        List<S3ObjectSummary> objects = objectListing.getObjectSummaries();


        Collections.sort(objects, new Comparator<S3ObjectSummary>() {
            @Override
            public int compare(S3ObjectSummary b1, S3ObjectSummary b2) {
                return b2.getLastModified().compareTo(b1.getLastModified());
            }
        });


        int i = 0;
        for (S3ObjectSummary sum : objects) {
            String metainfo =
                    s3Client.getObjectMetadata(bucketname, sum.getKey()).getUserMetadata().get(
                            "info");

            if (metainfo == null || metainfo.isEmpty()) {
                continue;
            }
            i++;
            sessions.append(String.valueOf(i) + "\t");
            sessions.append(sum.getKey() + "\t");
            String meta = null;
            meta = decode(metainfo);

            sessions.append(meta + "\t");
            LinkedList linkedList = new LinkedList();
            linkedList.add(sum.getKey());
            linkedList.add(metainfo);
            sessionsList.add(linkedList);
            sessions.append(sum.getLastModified() + "\t");
            sessions.append(System.lineSeparator());

        }
        if (sessions.toString().length() < 1) {
            sessions.append("\n【没有上线的主机】No active sessions.");
        }
        setSessionsList(sessionsList);
        return sessions.toString();
    }

//    public String listOSSSessions( AmazonS3 s3Client )
//    {
//
//        sessionsList.clear();
//        StringBuffer sessions = new StringBuffer( "" );
//        ListObjectsV2Result result = s3Client.listObjectsV2( bucketname );
//        result.setPrefix( path.substring( 1 ) );
//
////
////        com.amazonaws.services.s3.model.ListObjectsRequest listObjectsRequest = new com.amazonaws.services.s3.model.ListObjectsRequest()
////                .withBucketName(bucketname)
////                //查询bucket下固定的前缀
////                .withPrefix(path.substring(1));
//       // List<S3ObjectSummary> objects = result.getObjectSummaries();
//        com.amazonaws.services.s3.model.ListObjectsRequest listObjectsRequest = new com.amazonaws.services.s3.model.ListObjectsRequest()
//                .withBucketName(bucketname)
//                //查询bucket下固定的前缀
//                .withPrefix(path.substring(1));
//
//
//        ObjectListing objectListing = s3Client.listObjects( listObjectsRequest);
//
//
//        List<S3ObjectSummary> objects = objectListing.getObjectSummaries();
//        int i = 0;
//        for ( S3ObjectSummary sum : objects )
//        {
//            String metainfo =
//                            s3Client.getObjectMetadata( bucketname, sum.getKey() ).getUserMetadata().get(
//                                            "info" );
//
//            if ( metainfo == null || metainfo.isEmpty() )
//            {
//                continue;
//            }
//            i++;
//            sessions.append( String.valueOf( i ) + "\t" );
//            sessions.append( sum.getKey() + "\t" );
//            String meta = new String( Base64.getDecoder().decode( metainfo ) );
//            sessions.append( meta + "\t" );
//            LinkedList linkedList = new LinkedList();
//            linkedList.add( sum.getKey() );
//            linkedList.add( metainfo );
//            sessionsList.add( linkedList );
//            sessions.append( sum.getLastModified() + "\t" );
//            sessions.append( System.lineSeparator() );
//
//        }
//        if ( sessions.toString().length() < 1 )
//        {
//            sessions.append( "\nNo active sessions." );
//        }
//        setSessionsList( sessionsList );
//        return sessions.toString();
//    }

    protected String listOSSSessions(COSClient cosClient) {

        sessionsList.clear();
        StringBuffer sessions = new StringBuffer("");

        com.qcloud.cos.model.ListObjectsRequest listObjectsRequest = new com.qcloud.cos.model.ListObjectsRequest();
        listObjectsRequest.setBucketName(bucketname);

        listObjectsRequest.setPrefix(getPath().substring(1));
        listObjectsRequest.setDelimiter("/");
        listObjectsRequest.setMaxKeys(1000);
        com.qcloud.cos.model.ObjectListing objectListing = null;
        int i = 0;

        do {
            try {
                objectListing = cosClient.listObjects(listObjectsRequest);

            } catch (CosServiceException e) {
                e.printStackTrace();
                return "";
            } catch (CosClientException e) {
                e.printStackTrace();
                return "";
            }

            List<COSObjectSummary> cosObjectSummaries = objectListing.getObjectSummaries();

            Collections.sort(cosObjectSummaries, new Comparator<COSObjectSummary>() {
                @Override
                public int compare(COSObjectSummary b1, COSObjectSummary b2) {
                    return b2.getLastModified().compareTo(b1.getLastModified());
                }
            });


            for (COSObjectSummary summary : cosObjectSummaries) {
                String metainfo =
                        cosClient.getObjectMetadata(bucketname, summary.getKey()).getUserMetadata().get(
                                "info");
                if (metainfo == null || metainfo.isEmpty()) {

                    continue;
                }
                i++;
                sessions.append(String.valueOf(i) + "\t");
                sessions.append(summary.getKey() + "\t");
                String meta = null;
                meta = decode(metainfo);

                sessions.append(meta + "\t");
                LinkedList linkedList = new LinkedList();
                linkedList.add(summary.getKey());
                linkedList.add(metainfo);
                sessionsList.add(linkedList);
                sessions.append(summary.getLastModified() + "\t");
                sessions.append(System.lineSeparator());
            }

            String nextMarker = objectListing.getNextMarker();
            listObjectsRequest.setMarker(nextMarker);
        }
        while (objectListing.isTruncated());

        if (sessions.toString().length() < 1) {
            sessions.append("\n【没有上线主机】No active sessions.");
        }
        setSessionsList(sessionsList);

        return sessions.toString();
    }

    public OSS OSSConn() {

        OSS ossClient = null;
        try {
            ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
            if (ossClient != null && ossClient.doesBucketExist(bucketname)) {
                setCommandstate(true);
                setOSSClient(ossClient);
            } else {
                System.out.println("Object-Based Storage Connect Error,may be wrong ak & sk?\n");
            }

        } catch (Exception e) {
            System.out.println("Object-Based Storage Connect Error:\n" + e.getLocalizedMessage());
            return null;
        }

        return ossClient;

    }

    public COSClient COSConn() {

        COSClient cosClient = null;
        try {
            COSCredentials cred = new BasicCOSCredentials(accessKeyId, accessKeySecret);
            Region region = new Region(endpoint.substring(endpoint.indexOf("cos.") + 4,
                    endpoint.indexOf(".myqcloud")));
            ClientConfig clientConfig = new ClientConfig(region);
            cosClient = new COSClient(cred, clientConfig);
            if (endpoint.startsWith("https://")) {
                clientConfig.setHttpProtocol(HttpProtocol.https);
            }

            if (cosClient != null && cosClient.doesBucketExist(bucketname)) {
                setCommandstate(true);
                setOSSClient(cosClient);
            } else {
                System.out.println("Object-Based Storage Connect Error,may be wrong ak & sk?");

            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Object-Based Storage Connect Error:\n" + e.getLocalizedMessage());
            return null;
        }

        return cosClient;

    }

    public AmazonS3 S3Conn() {

        AmazonS3 s3Client = null;
        try {

            AWSCredentials cred = new BasicAWSCredentials(accessKeyId, accessKeySecret);
            ClientConfiguration clientConfig = new ClientConfiguration();
            if (endpoint.startsWith("https://")) {
                clientConfig.setProtocol(Protocol.HTTPS);
            } else {
                clientConfig.setProtocol(Protocol.HTTP);

            }
//            s3Client = AmazonS3ClientBuilder.standard()
//                                            .withCredentials( new AWSStaticCredentialsProvider(
//                                                            cred ) ).withClientConfiguration( clientConfig ).withRegion(
//                                            endpoint.substring( endpoint.indexOf( "s3." ) + 3,
//                                                                endpoint.indexOf( ".amazonaws" ) ) ).build();


            //生成云存储api client
            s3Client = new AmazonS3Client(cred);

            //配置云存储服务地址
            s3Client.setEndpoint(endpoint);

            //设置客户端生成的http请求hos格式，目前只支持path type的格式，不支持bucket域名的格式
            S3ClientOptions s3ClientOptions = new S3ClientOptions();
            s3ClientOptions.setPathStyleAccess(true);
            s3Client.setS3ClientOptions(s3ClientOptions);
            if (s3Client != null) {
                setCommandstate(true);
                setOSSClient(s3Client);
            } else {
                System.out.println("Object-Based Storage Connect Error,may be wrong ak & sk?");

            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Object-Based Storage Connect Error:\n" + e.getLocalizedMessage());
            return null;
        }

        return s3Client;

    }


    /**
     * Base64解码
     *
     * @param s 编码好的base64
     * @return 结果
     */
    public static String decode(String s) {
        int index = 0;
        StringBuilder strBuff = new StringBuilder();
        StringBuilder resultBuff = new StringBuilder();
        while (true) {
            //补零
            if (index == s.length() || s.charAt(index) == '=') {
                int zeroCount = 8 - strBuff.length();
                for (int i = 0; i < zeroCount; i++) {
                    strBuff.append('0');
                }
                index = -1;
            }
            //更新缓冲区字符
            while (index != -1 && strBuff.length() < 8 && index < s.length()) {
                int chatIndex = getCharIndexInBaseChars(s.charAt(index++));
                strBuff.append(get6BitStr(chatIndex));
            }
            //从缓冲区取8个字符
            String temp2 = strBuff.substring(0, 8);
            int temp10 = Integer.valueOf(temp2, 2);
            resultBuff.append((char) temp10);
            strBuff.delete(0, 8);
            //判断是否结束
            if (index == -1)
                break;
        }
        return resultBuff.toString().trim();
    }

    /**
     * 得到某个字符，在编码表的位置
     *
     * @param i 字符
     * @return 编号
     */
    private static int getCharIndexInBaseChars(char i) {
        int ascii = (int) i;
        if (ascii == 43) //'+'
            return 62;
        else if (ascii == 47) //'/'
            return 63;
        else if (ascii >= 48 && ascii <= 57) // 0-9
            return ascii + 4;
        else if (ascii >= 65 && ascii <= 90) // A-Z
            return ascii - 65;
        else if (ascii >= 97 && ascii <= 122) // a-z
            return ascii - 71;
        else
            return -1;
    }

    private static String get6BitStr(int ascii) {
        StringBuilder s = new StringBuilder(Integer.toBinaryString(ascii));
        if (s.length() < 6) {
            int zeroCount = 6 - s.length();
            for (int j = 0; j < zeroCount; j++) {
                s.insert(0, '0');
            }
        }
        return s.toString();
    }


}
