package biz.biztaker.commonClasses;

/**
 * Created by Anand Jakhaniya on 18-02-2018.
 * @author Anand Jakhaniya
 */

public interface BizTakerWebServiceCallBack {

    void onResponse(Object response);

    void onErrorResponse(Exception error);

}
