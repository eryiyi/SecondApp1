package com.example.secondapp.fragmentdetail;

import java.util.ArrayList;
import java.util.List;

import android.widget.Toast;
import com.example.secondapp.base.BaseFragment;
import com.example.secondapp.data.FruitBeanData;
import com.example.secondapp.util.StringUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.example.secondapp.R;
import com.example.secondapp.activity.CommodityDetailTest;
import com.example.secondapp.adapter.FruitShowListViewAdapter;
import com.example.secondapp.bean.FruitBean;
import com.example.secondapp.http.AsyncHttpResponseHandler;
import com.example.secondapp.http.HttpClientUtils;
import com.example.secondapp.http.HttpParams;
import com.example.secondapp.serviceId.ServerId;
import com.example.secondapp.view.MyListViewUpDown;
import com.example.secondapp.view.MyListViewUpDown.OnLoadListener;
import com.example.secondapp.view.MyListViewUpDown.OnRefreshListener;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

public class RmdFragment1 extends BaseFragment implements OnRefreshListener, OnLoadListener{
	
	View view;
	MyListViewUpDown listView;
	List<FruitBean> list;
	FruitShowListViewAdapter adapter;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		list = new ArrayList<FruitBean>();
		getRF1Data();
		view = inflater.inflate(R.layout.rmdfragment, null);
		listView = (MyListViewUpDown) view.findViewById(R.id.lv);
		listView.setonRefreshListener(this);
		listView.setOnLoadListener(this);
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
				Intent intent = new Intent(getActivity(), CommodityDetailTest.class);
				intent.putExtra("id", list.get(position - 1).getProduct_id());
				startActivity(intent);
			}
		});
		return view;
	}  
	
	@Override
	public void onDestroyView() {
		super.onDestroy();
	}
	
	public void getRF1Data(){
		HttpParams params = new HttpParams();
		HttpClientUtils.getInstance().post(ServerId.serveradress, ServerId.popularity, params, new AsyncHttpResponseHandler(){
			@Override
			public void onSuccess(JSONObject s) {
				if (StringUtil.isJson(s.toString())) {
					try {
						JSONObject jo = new JSONObject(s.toString());
						String code =  jo.getString("code");
						String dataJ =  jo.getString("data");
						if(Integer.parseInt(code) == 200 && !"false".equals(dataJ)){
							FruitBeanData data = getGson().fromJson(s.toString(), FruitBeanData.class);
							if(data != null && data.getData() != null){
								list.addAll(data.getData());
							}

						}
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
				Message message = new Message();
				message.what = 123;
				handler.sendMessage(message);
			}
		});
	}
	
	Handler handler = new Handler(Looper.myLooper(), new Handler.Callback() {

		@Override
		public boolean handleMessage(Message msg) {
			switch (msg.what) {
			case 123:
				adapter = new FruitShowListViewAdapter(getActivity(), list);
				listView.setAdapter(adapter);
				//adapter.notifyDataSetChanged();
				break;
			case 0:

				break;
			default:
				break;
			}
			return true;
		}
	});
	@Override
	public void onLoad() {
		new AsyncTask<Void, Void, Void>() {
			@Override
			protected Void doInBackground(Void... params) {
				try {
					Thread.sleep(800);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				//上拉加载的内容
				
				return null;
			}

			protected void onPostExecute(Void result) {
				adapter.notifyDataSetChanged();
				listView.onLoadComplete();
			};
		}.execute(null, null, null);
	}

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
				/*FruitBean bean = new FruitBean("下拉刷新测试");  
				list.add(bean);*/
				getRF1Data();
				return null;
			}

			protected void onPostExecute(Void result) {
				adapter.notifyDataSetChanged();
				listView.onRefreshComplete();
			};
		}.execute(null, null, null);
	}
}
