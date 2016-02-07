package com.example.secondapp.activity;

import java.util.ArrayList;
import java.util.List;

import com.example.secondapp.base.BaseActivity;
import org.json.JSONArray;
import org.json.JSONObject;
import com.example.secondapp.R;
import com.example.secondapp.adapter.MyCollectionAdapter;
import com.example.secondapp.bean.FruitBean;
import com.example.secondapp.http.AsyncHttpResponseHandler;
import com.example.secondapp.http.HttpClientUtils;
import com.example.secondapp.http.HttpParams;
import com.example.secondapp.serviceId.ServerId;
import com.example.secondapp.view.PullToRefreshLV;
import com.example.secondapp.view.PullToRefreshLV.OnRefreshListener;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MyCollection extends BaseActivity {
	PullToRefreshLV lv;
	List<FruitBean> list;
	LinearLayout logonback;
	public static MyCollectionAdapter adapter;
	public static TextView nocollection;
	@Override
	protected void onCreate(Bundle arg0) {
		checkCollection(Integer.parseInt(getGson().fromJson(getSp().getString("uid", ""), String.class)));
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(arg0);
		setContentView(R.layout.mycollection);
		lv = (PullToRefreshLV) findViewById(R.id.collectionlv);
		logonback = (LinearLayout) findViewById(R.id.logonback);
		nocollection = (TextView) findViewById(R.id.nocollection);
		list = new ArrayList<FruitBean>();
		list.clear();
		adapter = new MyCollectionAdapter(this, list,getGson().fromJson(getSp().getString("uid", ""), String.class) );
		//lv.setAdapter(adapter);
		logonback.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				finish();
			}
		});
		
		lv.setonRefreshListener(new OnRefreshListener() {

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
						lv.onRefreshComplete();
					};
				}.execute(null, null, null);
			}
		});
		lv.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
				Intent intent = new Intent(MyCollection.this, CommodityDetailTest.class);
				/*Bundle bundle = new Bundle();
				bundle.putSerializable("fruitdetail", list.get(position));
				intent.putExtras(bundle);*/
				intent.putExtra("id", list.get(position - 1).getProduct_id());
				startActivity(intent);
			}
		});

	}

	public void checkCollection(int userid) {
		HttpParams params = new HttpParams();
		params.put("userId", String.valueOf(userid));
		HttpClientUtils.getInstance().post(ServerId.serveradress,
				ServerId.getCollection, params, new AsyncHttpResponseHandler() {
					@Override
					public void onSuccess(JSONObject jsonObject) {
						System.out.println("jsonObject = " + jsonObject);
						JSONArray jsonArray = jsonObject.optJSONArray("data");
						if (jsonArray != null) {
							for (int i = 0; i < jsonArray.length(); i++) {
								FruitBean bean = new FruitBean();
								JSONObject object = jsonArray.optJSONObject(i);
								bean.setProduct_pic(object.optString("product_pic"));
								bean.setPrice(object.optString("price_tuangou"));
								bean.setProduct_name(object.optString("product_name"));
								bean.setPrice(object.optString("price"));
								bean.setProduct_id(object.optInt("product_id"));
								bean.setBuy_numbers(object.optString("buy_numbers"));
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
					nocollection.setVisibility(View.VISIBLE);
				}
				lv.setAdapter(adapter);
				//adapter.notifyDataSetChanged();
				break;
			default:
				break;
			}
			return false;
		}
	});
}
