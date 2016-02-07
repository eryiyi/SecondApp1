package com.example.secondapp.activity;

import com.example.secondapp.R;
import com.example.secondapp.adapter.MyOrderAdapter4;
import com.example.secondapp.base.BaseActivity;
import com.example.secondapp.bean.MyOrderBean;
import com.example.secondapp.fragmentdetail.MOFragment4;
import com.example.secondapp.http.AsyncHttpResponseHandler;
import com.example.secondapp.http.HttpClientUtils;
import com.example.secondapp.http.HttpParams;
import com.example.secondapp.networkbitmap.BitmapUtil;
import com.example.secondapp.serviceId.ServerId;
import com.example.secondapp.view.MyDialog;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ToBeEvaluation extends BaseActivity implements OnClickListener{
	Button btn1;
	Button btn2;
	LinearLayout back;
	private static int pos;
	private static MyOrderBean myOrderBean;
	ImageView imageView;
	TextView comname;
	TextView comprice;
	TextView number;
	TextView paid;
	TextView money;
	@Override
	protected void onCreate(Bundle arg0) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(arg0);
		setContentView(R.layout.tobeevaluation);
		back = (LinearLayout) findViewById(R.id.back);
		myOrderBean = (MyOrderBean) getIntent().getSerializableExtra("orderbean");
		imageView = (ImageView) findViewById(R.id.comimage);
		BitmapUtil.getInstance().download(ServerId.serveradress, myOrderBean.getImg(), imageView);
		comname = (TextView) findViewById(R.id.comname);
		comname.setText(myOrderBean.getOrder_no());
		comprice = (TextView) findViewById(R.id.comprice);
		comprice.setText("¥" + myOrderBean.getReal_price());
		number = (TextView) findViewById(R.id.number);
		number.setText(myOrderBean.getGoods_nums() + "件");
		paid = (TextView) findViewById(R.id.paid);
		paid.setText("¥" + myOrderBean.getReal_amount());
		money = (TextView) findViewById(R.id.money);
		money.setText("¥" + myOrderBean.getReal_amount());
		btn1 = (Button) findViewById(R.id.btn1);
		btn2 = (Button) findViewById(R.id.btn2);
		btn1 = (Button) findViewById(R.id.btn1);
		btn2 = (Button) findViewById(R.id.btn2);
		pos = getIntent().getIntExtra("mof4", 0);
		btn1.setOnClickListener(this);
		btn2.setOnClickListener(this);
		back.setOnClickListener(this);
	}
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn1:
			showMyDialog(pos);
			break;
		case R.id.btn2:
			Intent intent = new Intent(ToBeEvaluation.this, EvaluationOrder.class);
			Bundle bundle = new Bundle();
			bundle.putSerializable("orderbean", myOrderBean);
			intent.putExtras(bundle);
			startActivity(intent);
			break;
		case R.id.back:
			finish();
			break;
		default:
			break;
		}
	}
	
	public void showMyDialog(final int position) {
		MyDialog dialog = new MyDialog(this);
		//dialog.setIcon(R.drawable.a008);
		dialog.setTitle("取消订单");
		dialog.setMessage("确认删除这个订单吗？ ");
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
				params.put("order_id", myOrderBean.getOrder_id());
				HttpClientUtils.getInstance().post(ServerId.serveradress, ServerId.deletetopayment, params, new AsyncHttpResponseHandler(){
					 
				});
				MyOrderAdapter4.list.remove(position);
				MOFragment4.adapter.notifyDataSetChanged();
				if (MyOrderAdapter4.list.size() == 0) {
					MOFragment4.noorder.setVisibility(View.VISIBLE);
				}
				finish();
			}
		});
		dialog.show();
	}
}
