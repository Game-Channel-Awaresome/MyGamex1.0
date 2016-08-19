package com.example.sdk.emasdk;

/**
 * 回调方法返回值的状态常量定义类
 * 
 * @author EMAGroup 
 * @version 1.0
 */

public class EmaCallBackConst {
	
	//初始化状态 100开始
	public final static int INITSUCCESS = 0;//初始化成功
	public final static int INITFALIED = 1;//初始化失败
	
	//账号状态登录 200开始
	public final static int LOGINSUCCESS = 2;//登录成功
	public final static int LOGINFALIED = 5;//登录失败
	public final static int LOGINCANELL = 6;//登录取消
	public final static int LOGINSWITCHSUCCESS = 203;//切换账号，退出当前账号成功
	public final static int LOGINSWITCHCANELL = 204;//切换账号，退出当前账号失败
	
	//账号状态登出 300开始
	public final static int LOGOUTSUCCESS = 7;//	登出成功
	public final static int LOGOUTFALIED = 8;//登出失败
	
	//支付状态 400开始
	public final static int PAYSUCCESS = 0;//支付成功
	public final static int PAYFALIED = 1;//支付失败
	public final static int PAYCANELI = 2;//支付取消
	public final static int PAYREPEAT = 403;//订单重复
	
	//注册状态 500开始
	public final static int REGISTERSUCCESS = 500;//注册成功
	public final static int REGISTERFALIED = 501;//注册失败
}
