package com.emagroup.sdk.impl;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import com.anysdk.framework.IAPWrapper;
import com.anysdk.framework.PluginWrapper;
import com.anysdk.framework.UserWrapper;
import com.anysdk.framework.java.AnySDK;
import com.anysdk.framework.java.AnySDKIAP;
import com.anysdk.framework.java.AnySDKListener;
import com.anysdk.framework.java.AnySDKParam;
import com.anysdk.framework.java.AnySDKUser;
import com.anysdk.framework.java.ToolBarPlaceEnum;
import com.emagroup.sdk.EmaBackPressedAction;
import com.emagroup.sdk.EmaCallBackConst;
import com.emagroup.sdk.EmaConst;
import com.emagroup.sdk.EmaPay;
import com.emagroup.sdk.EmaPayInfo;
import com.emagroup.sdk.EmaSDK;
import com.emagroup.sdk.EmaSDKListener;
import com.emagroup.sdk.EmaUtils;
import com.emagroup.sdk.EmaUtilsInterface;
import com.emagroup.sdk.ULocalUtils;
import com.emagroup.sdk.Url;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


/**
 * Created by Administrator on 2016/10/9.
 */
public class EmaUtilsImpl implements EmaUtilsInterface {

    private static EmaUtilsImpl instance;
    private Activity mActivity;


    public static EmaUtilsImpl getInstance(Activity activity) {
        if (instance == null) {
            instance = new EmaUtilsImpl(activity);
        }
        return instance;
    }

    private EmaUtilsImpl(Activity activity) {
        this.mActivity = activity;
    }

    @Override
    public void immediateInit(EmaSDKListener listener) {

    }

    @Override
    public void realInit(final EmaSDKListener listener, JSONObject data) {

        try {
            String channelAppKey = data.getString("channelAppKey");
            String channelAppSecret = data.getString("channelAppSecret");
            String channelAppPrivate = data.getString("channelAppPrivate");
            AnySDK.getInstance().init(mActivity, channelAppKey, channelAppSecret, channelAppPrivate, "https://platform.lemonade-game.com/ema-platform/authLogin.jsp");
            //这里之所以不回调“初始化成功”  是因为any本身就有成功回调，让它来吧；
        } catch (JSONException e) {
            EmaUtils.getInstance(mActivity).makeUserCallBack(EmaCallBackConst.INITFALIED, "初始化失败",null);
            e.printStackTrace();
        }

        AnySDKUser.getInstance().setListener(new AnySDKListener() {
            @Override
            public void onCallBack(int i, String s) {
                if (listener != null) {
                    switch(i) {
                        case UserWrapper.ACTION_RET_INIT_SUCCESS://初始化成功
                            EmaUtils.getInstance(mActivity).makeUserCallBack(EmaCallBackConst.INITSUCCESS, "初始化成功",null);
                            Log.e("EmaAnySDK","初始化成功");
                            break;
                        case UserWrapper.ACTION_RET_INIT_FAIL://初始化SDK失败回调
                            EmaUtils.getInstance(mActivity).makeUserCallBack(EmaCallBackConst.INITFALIED, "初始化失败",null);
                            break;
                        case UserWrapper.ACTION_RET_LOGIN_SUCCESS://登陆成功回调

                            afterLoginSuccess(listener);

                            break;
                        case UserWrapper.ACTION_RET_LOGIN_CANCEL://登陆取消回调
                            EmaUtils.getInstance(mActivity).makeUserCallBack(EmaCallBackConst.LOGINCANELL,"登陆取消回调",null);
                            break;
                        case UserWrapper.ACTION_RET_LOGIN_FAIL://登陆失败回调
                            EmaUtils.getInstance(mActivity).makeUserCallBack(EmaCallBackConst.LOGINFALIED,"登陆失败回调",null);
                            break;
                        case UserWrapper.ACTION_RET_LOGOUT_SUCCESS://登出成功回调
                            EmaUtils.getInstance(mActivity).makeUserCallBack(EmaCallBackConst.LOGOUTSUCCESS,"登出成功回调",null);
                            break;
                        case UserWrapper.ACTION_RET_LOGOUT_FAIL://登出失败回调
                            EmaUtils.getInstance(mActivity).makeUserCallBack(EmaCallBackConst.LOGOUTFALIED,"登出失败回调",null);
                            break;
                        case UserWrapper.ACTION_RET_ACCOUNTSWITCH_SUCCESS://切换账号成功回调
                            EmaUtils.getInstance(mActivity).makeUserCallBack(EmaCallBackConst.ACCOUNTSWITCHSUCCESS,"切换成功回调",null);
                            break;
                        case UserWrapper.ACTION_RET_ACCOUNTSWITCH_FAIL://切换账号失败回调
                            EmaUtils.getInstance(mActivity).makeUserCallBack(EmaCallBackConst.ACCOUNTSWITCHFAIL,"切换失败回调",null);
                            break;
                        case UserWrapper.ACTION_RET_EXIT_PAGE://退出游戏回调
                            if(s == "onGameExit" || s == "onNo3rdExiterProvide") {   //豌豆荚||有米
                                //弹出游戏退出界面
                                Log.e("tuichu","1");
                            } else {
                                Log.e("tuichu","2");
                                //执行游戏退出逻辑
                                mActivity.finish();
                                System.exit(0);
                            }
                            break;
                        case UserWrapper.ACTION_RET_GAME_EXIT_PAGE:
                            Log.e("tuichu","3");
                            mActivity.finish();
                            System.exit(0);
                            break;
                    }
                }
            }
        });
    }


    public void realLogin(EmaSDKListener listener, String userid, String deviceId){
        Map<String, String> info = new HashMap<String, String>();
        info.put("device_info", deviceId);
        info.put("uid", userid);
        AnySDKUser.getInstance().login(info);
    }

    /**
     * 用于支付前的一些操作
     *
     * @param listener
     */
    /**
     * 因为any的支付监听是单独先设置的
     * @param listener
     */
    public void doPayPre(final EmaSDKListener listener) {

        //支付过程中若 SDK 没有回调结果，就认为支付正在进行中，再次调用支付的时候会回调 PAYRESULT_NOW_PAYING，可以调用该函数重置支付状态
        AnySDKIAP.getInstance().resetPayState();

        AnySDKIAP.getInstance().setListener(new AnySDKListener() {
            @Override
            public void onCallBack(int arg0, String arg1) {
                switch(arg0)
                {
                    case IAPWrapper.PAYRESULT_SUCCESS://支付成功回调
                        // 购买成功
                        listener.onCallBack(EmaCallBackConst.PAYSUCCESS,"购买成功");
                        break;
                    case IAPWrapper.PAYRESULT_FAIL://支付失败回调
                        // 购买失败
                        //call一次取消订单
                        EmaPay.getInstance(mActivity).cancelOrder();

                        listener.onCallBack(EmaCallBackConst.PAYFALIED,"购买失败");
                        break;
                    case IAPWrapper.PAYRESULT_CANCEL://支付取消回调
                        // 取消购买
                        //call一次取消订单
                        EmaPay.getInstance(mActivity).cancelOrder();

                        listener.onCallBack(EmaCallBackConst.PAYCANELI,"取消购买");
                        break;
                    case IAPWrapper.PAYRESULT_NETWORK_ERROR://支付超时回调
                        //统一接口里面没有
                        break;
                    case IAPWrapper.PAYRESULT_PRODUCTIONINFOR_INCOMPLETE://支付信息提供不完全回调
                        //统一接口里面没有
                        break;
                    default:
                        //购买失败
                        //call一次取消订单
                        EmaPay.getInstance(mActivity).cancelOrder();

                        listener.onCallBack(EmaCallBackConst.PAYFALIED,"购买失败");
                        break;
                }
            }
        });
    }

    public void realPay(final EmaSDKListener listener, EmaPayInfo emaPayInfo) {

        Map<String, String> anyPayInfo = new HashMap();
        anyPayInfo.put("Product_Price", emaPayInfo.getPrice()/Integer.parseInt(emaPayInfo.getProductNum())+"");
        anyPayInfo.put("Product_Id",emaPayInfo.getProductId());
        anyPayInfo.put("Product_Name",emaPayInfo.getProductName());
        anyPayInfo.put("Product_Count", emaPayInfo.getProductNum());
        anyPayInfo.put("EXT",emaPayInfo.getOrderId());
        anyPayInfo.put("Coin_Name", "coin");

        anyPayInfo.put("Role_Id",(String) ULocalUtils.spGet(mActivity,EmaConst.SUBMIT_ROLE_ID,""));
        anyPayInfo.put("Role_Name", (String) ULocalUtils.spGet(mActivity,EmaConst.SUBMIT_ROLE_NAME,""));
        anyPayInfo.put("Role_Grade", (String) ULocalUtils.spGet(mActivity,EmaConst.SUBMIT_ROLE_LEVEL,""));
        anyPayInfo.put("Server_Id", (String) ULocalUtils.spGet(mActivity,EmaConst.SUBMIT_ZONE_ID,""));
        anyPayInfo.put("Server_Name", (String) ULocalUtils.spGet(mActivity,EmaConst.SUBMIT_ZONE_NAME,""));
        anyPayInfo.put("Role_Balance", "66");

        /*EmaSDKIAP iap = EmaSDKIAP.getInstance();
        ArrayList<String> idArrayList = iap.getPluginId();
        iap.payForProduct(idArrayList.get(0), anyPayInfo,listener);*/
        ArrayList<String> idArrayList =  AnySDKIAP.getInstance().getPluginId();
        AnySDKIAP.getInstance().payForProduct(idArrayList.get(0), anyPayInfo);
        Log.e("dopay","dopay");
    }

    public void logout() {
        if (AnySDKUser.getInstance().isFunctionSupported("logout")) {
            AnySDKUser.getInstance().callFunction("logout");
        }
    }

    public void swichAccount() {
        if (AnySDKUser.getInstance().isFunctionSupported("accountSwitch")) {
            AnySDKUser.getInstance().callFunction("accountSwitch");
        }
    }

    public void doShowToolbar() {
        AnySDKParam param = new AnySDKParam(ToolBarPlaceEnum.kToolBarTopLeft.getPlace());
        AnySDKUser.getInstance().callFunction("showToolBar", param);
    }

    public void doHideToobar() {
        if (AnySDKUser.getInstance().isFunctionSupported("hideToolBar")) {
            AnySDKUser.getInstance().callFunction("hideToolBar");
        }
    }

    public void onResume() {
        PluginWrapper.onResume();
    }

    public void onPause() {
        PluginWrapper.onPause();
    }

    public void onStop() {
        PluginWrapper.onStop();
    }

    public void onDestroy() {
        PluginWrapper.onDestroy();
        AnySDK.getInstance().release();
    }

    public void onBackPressed(EmaBackPressedAction action) {
        if (AnySDKUser.getInstance().isFunctionSupported("exit")) {
            AnySDKUser.getInstance().callFunction("exit");
        }else {
            action.doBackPressedAction();
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        PluginWrapper.onActivityResult(requestCode, resultCode, data);
    }

    public void onNewIntent(Intent intent) {
        PluginWrapper.onNewIntent(intent);
    }

    public void onRestart() {
        PluginWrapper.onRestart();
    }

    public void submitGameRole(Map<String, String> data) {
        if (AnySDKUser.getInstance().isFunctionSupported("submitLoginGameRole")) {
            Map<String, String> map = new HashMap<>();
            map.put("dataType", data.get(EmaConst.SUBMIT_DATA_TYPE));
            map.put("roleId", data.get(EmaConst.SUBMIT_ROLE_ID));
            map.put("roleName", data.get(EmaConst.SUBMIT_ROLE_NAME));
            map.put("roleLevel", data.get(EmaConst.SUBMIT_ROLE_LEVEL));
            map.put("zoneId", data.get(EmaConst.SUBMIT_ZONE_ID));
            map.put("zoneName", data.get(EmaConst.SUBMIT_ZONE_NAME));
            map.put("balance", "66");
            map.put("partyName", "emaUnion");
            map.put("vipLevel", "1");
            map.put("roleCTime", data.get(EmaConst.SUBMIT_ROLE_CT));
            map.put("roleLevelMTime", "-1");

            AnySDKParam param = new AnySDKParam(map);
            AnySDKUser.getInstance().callFunction("submitLoginGameRole", param);


            if ("2".equals(map.put("dataType", "1"))) {// 2为创建角色，有些渠道需要两次，创建一次登录成功一次，所以索性所有渠道都直接再来一次登录的
                param = new AnySDKParam(map);
                AnySDKUser.getInstance().callFunction("submitLoginGameRole", param);
            }

        }
    }

    //-----------------------------------xxx的网络请求方法-------------------------------------------------------------------------

    private void afterLoginSuccess(EmaSDKListener listener){
        //显示toolbar
        EmaSDK.getInstance().doShowToolbar();

        //获取用户的登陆后的 UID(即用户唯一标识)
        String uid = AnySDKUser.getInstance().getUserID();
        String nikename = "";

        HashMap<String, String> data = new HashMap<>();
        data.put(EmaConst.ALLIANCE_UID,uid);
        data.put(EmaConst.NICK_NAME,nikename);

        EmaUtils.getInstance(mActivity).makeUserCallBack(EmaCallBackConst.LOGINSUCCESS_CHANNEL, "渠道登录成功",data);

    }


    //-----------------------------------xxx 特有接口---------------------------------------------------

    public static String getMhrSignJson() {
        return Url.getServerUrl() + "/ema-platform/extra/mhrCreateOrder";
    }
}
