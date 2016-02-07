package com.example.secondapp.activity;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.*;

import android.app.AlertDialog;
import android.text.TextUtils;
import android.widget.*;
import com.alipay.sdk.app.PayTask;
import com.alipay.sdk.pay.PayResult;
import com.alipay.sdk.pay.SignUtils;
import com.example.secondapp.base.BaseActivity;
import com.example.secondapp.bean.FruitBean;
import org.json.JSONArray;
import org.json.JSONObject;
import com.example.secondapp.R;
import com.example.secondapp.adapter.ShoppingCartAdapter;
import com.example.secondapp.bean.ShoppingcartBean;
import com.example.secondapp.http.AsyncHttpResponseHandler;
import com.example.secondapp.http.HttpClientUtils;
import com.example.secondapp.http.HttpParams;
import com.example.secondapp.serviceId.ServerId;
import com.example.secondapp.sharedprefernces.SharedPrefsUtil;
import com.example.secondapp.view.MyDialog;
import com.example.secondapp.view.PullToRefreshLV;
import com.example.secondapp.view.PullToRefreshLV.OnRefreshListener;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView.OnItemClickListener;

public class ShoppingCartList extends BaseActivity implements OnClickListener{
	public static PullToRefreshLV listview;
	LinearLayout back;
	public static LinearLayout gopayfor;
	public static ShoppingCartAdapter adapter;
	public static List<ShoppingcartBean> list;
	int count;
	public static TextView gopaynum;
	public static TextView totalprice;
	public static TextView nocom;
	ShoppingcartBean bean;
	SharedPreferences sharedPreferences;
	private static ImageView deleteall;

	//---------------------------------支付开始----------------------------------------
	// 商户PID
	public static final String PARTNER = "2088021404950836";
	// 商户收款账号
	public static final String SELLER = "794370578@QQ.com";
	// 商户私钥，pkcs8格式
	public static final String RSA_PRIVATE = "MIICeQIBADANBgkqhkiG9w0BAQEFAASCAmMwggJfAgEAAoGBAMo10/sPXGOIlZSz" +
			"qfQtKApeHpzvq57dtwpiWVsbyu/jpLFeb+51IxSXrdgItDp/By42Jkxlv4KjTGLP" +
			"SsI1UO06liAq40ll439Oy3fvHn3MRJzUFnBZiwb4ksTMXoMbDLJ7VavXRi5Carfs" +
			"sIzOHovhEUA7rpQY10MnmUVyOaLLAgMBAAECgYEAsu4+zgIhttG9yv/QfAkZTVNL" +
			"s4NBUirPNZkhiMVnJi9CJNF0KoXsrOQrKOdK1BhjujigWyoN9QObLDRR6thS6oXt" +
			"GVwSS4ONvJdlslrXFdpefCSL/R9sdTPoe2cLPsU0sd8zmvUxNmt5RF6qByMBl0vu" +
			"0ejyYkNUlw4GUY31NQECQQDrmffyIlM9e8Do0cfdp7j6mkcKFkmqYYsk+AYrnVUa" +
			"2C5tSlwtW6yK3oyu3jJttjHoIN5LvL1io7qV9M265OuRAkEA27fBM7kNgrxt0lj+" +
			"7LrRdJczafpaH5lFcIUristlO1fDtgO1eVbWfPYCYdCh3iBneUhlyaTfaMugJd87" +
			"6JnimwJBAJ5BoFFfSTbFiAFb2LtFTHXZZ9qYugbe2s6MY+isGFyd1iHHjz9Qkwf9" +
			"Edbsnkcf2Uopueh58WTuwgi0lfTyjGECQQDSRS3pkPykFC5Jsw/sf/E50gXLM2MS" +
			"gH1a7Kc6AeMUgq5yv2PqBrfoCQtIjwCJ8pr0D9wRuO5xfQX7mILd3H9nAkEAgcng" +
			"TgC/q1MLSvFPoLFv+takfuZJc9hmuQddGEqxe4Vjyi1f5G641uM/iEQ5mDXOvSMj" +
			"eomIUNrroE3GcwVCvA==";
	// 支付宝公钥
	public static final String RSA_PUBLIC = "";
	private static final int SDK_PAY_FLAG = 1;

	private static final int SDK_CHECK_FLAG = 2;

	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case SDK_PAY_FLAG: {
					PayResult payResult = new PayResult((String) msg.obj);

					// 支付宝返回此次支付结果及加签，建议对支付宝签名信息拿签约时支付宝提供的公钥做验签
					String resultInfo = payResult.getResult();

					String resultStatus = payResult.getResultStatus();

					// 判断resultStatus 为“9000”则代表支付成功，具体状态码代表含义可参考接口文档
					if (TextUtils.equals(resultStatus, "9000")) {
						Toast.makeText(ShoppingCartList.this, "支付成功",
								Toast.LENGTH_SHORT).show();
						finish();
					} else {
						// 判断resultStatus 为非“9000”则代表可能支付失败
						// “8000”代表支付结果因为支付渠道原因或者系统原因还在等待支付结果确认，最终交易是否成功以服务端异步通知为准（小概率状态）
						if (TextUtils.equals(resultStatus, "8000")) {
							Toast.makeText(ShoppingCartList.this, "支付结果确认中",
									Toast.LENGTH_SHORT).show();

						} else {
							// 其他值就可以判断为支付失败，包括用户主动取消支付，或者系统返回的错误
							Toast.makeText(ShoppingCartList.this, "支付失败",
									Toast.LENGTH_SHORT).show();

						}
					}
					break;
				}
				case SDK_CHECK_FLAG: {
					Toast.makeText(ShoppingCartList.this, "检查结果为：" + msg.obj,
							Toast.LENGTH_SHORT).show();
					break;
				}
				default:
					break;
			}
		};
	};
	//------------------------------------------------------------------------------------

	@Override
	protected void onCreate(Bundle arg0) {
		if("0".equals(getGson().fromJson(getSp().getString("is_login", ""), String.class))){
			//未登录
			Intent login = new Intent(ShoppingCartList.this, Logon.class);
			startActivity(login);
		}else {
			list = new ArrayList<ShoppingcartBean>();
			getshoppingcart(Integer.parseInt(getGson().fromJson(getSp().getString("uid", ""), String.class)));
			requestWindowFeature(Window.FEATURE_NO_TITLE);
			super.onCreate(arg0);
			setContentView(R.layout.shoppingcart);
			initView();
			listview.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
					Intent intent = new Intent(ShoppingCartList.this, CommodityDetailTest.class);
					intent.putExtra("id", list.get(position - 1).fruitid);
					startActivity(intent);
				}
			});

			listview.setonRefreshListener(new OnRefreshListener() {

				@Override
				public void onRefresh() {
					new AsyncTask<Void, Void, Void>() {

						@Override
						protected Void doInBackground(Void... params) {
							try {
								Thread.sleep(800);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
							for (int i = 0; i < 2; i++) {

							}
							return null;
						}

						protected void onPostExecute(Void result) {
							adapter.notifyDataSetChanged();
							listview.onRefreshComplete();
						};
					}.execute(null, null, null);
				}
			});
		}

	}
	
	public void initView(){
		ShoppingCartAdapter.totalall = 0.0;
		listview = (PullToRefreshLV) findViewById(R.id.rfslv);
		back = (LinearLayout) findViewById(R.id.back);
		gopaynum = (TextView) findViewById(R.id.gopaynum);
		totalprice = (TextView) findViewById(R.id.totalprice);
		deleteall = (ImageView) findViewById(R.id.deleteall);
		gopayfor = (LinearLayout) findViewById(R.id.gopayfor);
		nocom = (TextView) findViewById(R.id.test);
		back.setOnClickListener(this);
		gopayfor.setOnClickListener(this);
		deleteall.setOnClickListener(this);
		adapter = new ShoppingCartAdapter(this, list ,getGson().fromJson(getSp().getString("uid", ""), String.class));
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.back:
			finish();
			break;
		case R.id.deleteall:
			showMyDialog(0);
			break;
			case R.id.gopayfor:
				//去支付
				ArrayList<FruitBean> arrayList = new ArrayList<FruitBean>();
				for(int i=0;i<list.size();i++){
					ShoppingcartBean cell = list.get(i);
					FruitBean fruitbean = new FruitBean();
					fruitbean.setProduct_id(cell.fruitid);
					fruitbean.setProduct_name(cell.fruitname);
					fruitbean.setCountNum(String.valueOf(cell.fruitcount));
					fruitbean.setPrice_tuangou(cell.fruitprice);
					fruitbean.setPrice(cell.fruitprice);
					fruitbean.setProduct_pic(cell.fruitimage);
					arrayList.add(fruitbean);

				}
				if(arrayList != null && arrayList.size() > 0){
					Intent orderMakeView = new Intent(ShoppingCartList.this, OrderConfirm.class);
					orderMakeView.putExtra("listsgoods",arrayList);
					startActivity(orderMakeView);
				}
//				String txtTotal = totalprice.getText().toString();
//				pay("订单标题","订单简介", txtTotal, getOutTradeNo());
				break;
		default:
			break;
		}
	}
	
	public void showMyDialog(final int position) {
		MyDialog dialog = new MyDialog(this);
		//dialog.setIcon(R.drawable.a008);
		dialog.setTitle("删除商品");
		dialog.setMessage("   确定从购物车删除所有商品吗？   ");
		dialog.setPositiveButton("取消", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {

			}
		});
		dialog.setNegativeButton("确定", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				HttpParams params = new HttpParams();
				params.put("userId", getGson().fromJson(getSp().getString("uid", ""), String.class));
				HttpClientUtils.getInstance().post(ServerId.serveradress, ServerId.deleteallshoppingcart, params, new AsyncHttpResponseHandler(){
				 
				});
				list.clear();
				SharedPrefsUtil.putValue(ShoppingCartList.this, "Setting", 0);
				gopaynum.setText("(" + list.size() + ")");
				totalprice.setText("" + 0.0);
				gopayfor.setBackgroundColor(getResources().getColor(R.color.common_grey));
				nocom.setVisibility(View.VISIBLE);
				adapter.notifyDataSetChanged();
			}
		});
		dialog.show();
	}
	
	public void getshoppingcart(int userid){
		HttpParams params = new HttpParams();
		params.put("userId", String.valueOf(userid));
		HttpClientUtils.getInstance().post(ServerId.serveradress, ServerId.checkshoppingcart, params, new AsyncHttpResponseHandler(){
			@Override
			public void onSuccess(JSONObject jsonObject) {
				System.out.println("jsonObject = " + jsonObject);
				JSONArray jsonArray = jsonObject.optJSONArray("data");
				if (jsonArray != null) {
					for (int i = 0; i < jsonArray.length(); i++) {
						ShoppingcartBean bean = new ShoppingcartBean();
						JSONObject object = jsonArray.optJSONObject(i);
						bean.fruitimage = object.optString("product_pic");
						bean.fruitprice = object.optString("price");
						bean.fruitname = object.optString("product_name");
						bean.fruitcount = object.optInt("numbers");
						bean.cartid = object.optInt("cart_id");
						bean.fruitid = object.optInt("product_id");
						list.add(bean);
					}
				}
				Message message = new Message();
				message.what = 123;
				handler.sendMessage(message);
			}
		});
	}
	
	Handler handler = new Handler(new Handler.Callback() {

		@Override
		public boolean handleMessage(Message msg) {
			switch (msg.what) {
			case 123:
				if (list.size() == 0) {
					nocom.setVisibility(View.VISIBLE);
					gopaynum.setText("(" + list.size() + ")");
					totalprice.setText("" + 0.0);
					gopayfor.setBackgroundColor(getResources().getColor(R.color.common_grey));
				}
				count = list.size();
				SharedPrefsUtil.putValue(ShoppingCartList.this, "Setting" + getGson().fromJson(getSp().getString("uid", ""), String.class), count);
				listview.setAdapter(adapter);
				//adapter.notifyDataSetChanged();
				break;
			default:
				break;
			}
			return false;
		}
	});
	
	@Override
	public void onPause() {
		ShoppingCartAdapter.totalall = 0;
		/*count = adapter.getCount();
		SharedPrefsUtil.putValue(ShoppingCartList.this, "Setting", count);*/
		super.onPause();
	}

	//---------------------------------------------------------支付宝------------------------------------------

	/**
	 * call alipay sdk pay. 调用SDK支付
	 *
	 */
	public void pay(String subject, String body, String price, String sn) {
		if (TextUtils.isEmpty(PARTNER) || TextUtils.isEmpty(RSA_PRIVATE)
				|| TextUtils.isEmpty(SELLER)) {
			new AlertDialog.Builder(this)
					.setTitle("警告")
					.setMessage("需要配置PARTNER | RSA_PRIVATE| SELLER")
					.setPositiveButton("确定",
							new DialogInterface.OnClickListener() {
								public void onClick(
										DialogInterface dialoginterface, int i) {
									//
									finish();
								}
							}).show();
			return;
		}
		// 订单
		String orderInfo = getOrderInfo(subject, body, price ,sn);

		// 对订单做RSA 签名
		String sign = sign(orderInfo);
		try {
			// 仅需对sign 做URL编码
			sign = URLEncoder.encode(sign, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		// 完整的符合支付宝参数规范的订单信息
		final String payInfo = orderInfo + "&sign=\"" + sign + "\"&"
				+ getSignType();

		Runnable payRunnable = new Runnable() {

			@Override
			public void run() {
				// 构造PayTask 对象
				PayTask alipay = new PayTask(ShoppingCartList.this);
				// 调用支付接口，获取支付结果
				String result = alipay.pay(payInfo);

				Message msg = new Message();
				msg.what = SDK_PAY_FLAG;
				msg.obj = result;
				mHandler.sendMessage(msg);
			}
		};

		// 必须异步调用
		Thread payThread = new Thread(payRunnable);
		payThread.start();
	}

	/**
	 * check whether the device has authentication alipay account.
	 * 查询终端设备是否存在支付宝认证账户
	 *
	 */
	public void check(View v) {
		Runnable checkRunnable = new Runnable() {

			@Override
			public void run() {
				// 构造PayTask 对象
				PayTask payTask = new PayTask(ShoppingCartList.this);
				// 调用查询接口，获取查询结果
				boolean isExist = payTask.checkAccountIfExist();

				Message msg = new Message();
				msg.what = SDK_CHECK_FLAG;
				msg.obj = isExist;
				mHandler.sendMessage(msg);
			}
		};

		Thread checkThread = new Thread(checkRunnable);
		checkThread.start();

	}

	/**
	 * get the sdk version. 获取SDK版本号
	 *
	 */
	public void getSDKVersion() {
		PayTask payTask = new PayTask(this);
		String version = payTask.getVersion();
		Toast.makeText(this, version, Toast.LENGTH_SHORT).show();
	}

	/**
	 * create the order info. 创建订单信息
	 *
	 */
	public String getOrderInfo(String subject, String body, String price, String sn) {

		// 签约合作者身份ID
		String orderInfo = "partner=" + "\"" + PARTNER + "\"";

		// 签约卖家支付宝账号
		orderInfo += "&seller_id=" + "\"" + SELLER + "\"";

		// 商户网站唯一订单号
		orderInfo += "&out_trade_no=" + "\"" + sn + "\"";

		// 商品名称
		orderInfo += "&subject=" + "\"" + subject + "\"";

		// 商品详情
		orderInfo += "&body=" + "\"" + body + "\"";

		// 商品金额
		orderInfo += "&total_fee=" + "\"" + price + "\"";

		// 服务器异步通知页面路径
//        orderInfo += "&notify_url=" + "\"http://115.29.200.169/alipay_notify.php\"";
//        orderInfo += "&notify_url=" + "\"http://baidu.com\"";

		orderInfo += "&notify_url=" + "\""
				+ "http://115.29.208.113/alipay_notify.php" + "\"";
//
//        // 服务接口名称， 固定值
//        orderInfo += "&service=\"mobile.securitypay.pay\"";

		// 服务接口名称， 固定值
		orderInfo += "&service=\"mobile.securitypay.pay\"";

		// 支付类型， 固定值
		orderInfo += "&payment_type=\"1\"";

		// 参数编码， 固定值
		orderInfo += "&_input_charset=\"utf-8\"";

		// 设置未付款交易的超时时间
		// 默认30分钟，一旦超时，该笔交易就会自动被关闭。
		// 取值范围：1m～15d。
		// m-分钟，h-小时，d-天，1c-当天（无论交易何时创建，都在0点关闭）。
		// 该参数数值不接受小数点，如1.5h，可转换为90m。
		orderInfo += "&it_b_pay=\"30m\"";

		// extern_token为经过快登授权获取到的alipay_open_id,带上此参数用户将使用授权的账户进行支付
		// orderInfo += "&extern_token=" + "\"" + extern_token + "\"";

		// 支付宝处理完请求后，当前页面跳转到商户指定页面的路径，可空
		orderInfo += "&return_url=\"m.alipay.com\"";

		// 调用银行卡支付，需配置此参数，参与签名， 固定值 （需要签约《无线银行卡快捷支付》才能使用）
		// orderInfo += "&paymethod=\"expressGateway\"";

		return orderInfo;
	}

	/**
	 * get the out_trade_no for an order. 生成商户订单号，该值在商户端应保持唯一（可自定义格式规范）
	 *
	 */
	public String getOutTradeNo() {
		SimpleDateFormat format = new SimpleDateFormat("MMddHHmmss",
				Locale.getDefault());
		Date date = new Date();
		String key = format.format(date);

		Random r = new Random();
		key = key + r.nextInt();
		key = key.substring(0, 15);
		return key;
	}

	/**
	 * sign the order info. 对订单信息进行签名
	 *
	 * @param content
	 *            待签名订单信息
	 */
	public String sign(String content) {
		return SignUtils.sign(content, RSA_PRIVATE);
	}

	/**
	 * get the sign type we use. 获取签名方式
	 *
	 */
	public String getSignType() {
		return "sign_type=\"RSA\"";
	}

	//-------zfb----支付完成之后的操作
}
