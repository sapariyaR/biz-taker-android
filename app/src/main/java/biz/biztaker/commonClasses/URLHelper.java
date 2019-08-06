package biz.biztaker.commonClasses;

/**
 * Created by parth on 17/2/18.
 * write all the application urls in this file
 */

public class URLHelper {

    private static String serverName = "http://192.168.0.102:8080/";//"http://localhost:8080/";
    public static String getSignInWithGoogleURL(){
        return serverName + "";
    }
    public static String getCustomLoginUrl(){return serverName +  "api/auth/login/true";}
    public static String getRefreshAuthTokenUrl(){return serverName +  "/api/auth/refresh";}
}
