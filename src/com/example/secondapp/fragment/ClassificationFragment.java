package com.example.secondapp.fragment;

import java.util.ArrayList;
import java.util.List;

import com.example.secondapp.base.BaseFragment;
import org.json.JSONArray;
import org.json.JSONObject;
import com.example.secondapp.R;
import com.example.secondapp.activity.ClassDetail;
import com.example.secondapp.adapter.ClassAdapter;
import com.example.secondapp.bean.ClassBean;
import com.example.secondapp.http.AsyncHttpResponseHandler;
import com.example.secondapp.http.HttpClientUtils;
import com.example.secondapp.http.HttpParams;
import com.example.secondapp.serviceId.ServerId;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public class ClassificationFragment extends BaseFragment {
	View view;
	ListView classlv;
	List<ClassBean> list = new ArrayList<ClassBean>();
	ClassAdapter adapter;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		getdata();
		view = inflater.inflate(R.layout.classificationfragment, null);
		classlv = (ListView) view.findViewById(R.id.classlv);
		classlv.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
				Intent intent = new Intent(getActivity(), ClassDetail.class);
				intent.putExtra("key", list.get(position).typeid);
				intent.putExtra("title", list.get(position).typename);
				startActivity(intent);
			}
		});
		return view;
	}

	public void getdata() {
		HttpParams params = new HttpParams();
		HttpClientUtils.getInstance().get(ServerId.serveradress, ServerId.producttype, params, new AsyncHttpResponseHandler() {
					@Override
					public void onSuccess(JSONObject jsonObject) {
						System.out.println("jsonObject = " + jsonObject);
						JSONArray jsonArray = jsonObject.optJSONArray("data");
						if (jsonArray != null) {
							for (int i = 0; i < jsonArray.length(); i++) {
								JSONObject object = jsonArray.optJSONObject(i);
								ClassBean classBean = new ClassBean();
								classBean.typeid = object.optInt("type_id");
								classBean.typelogo = object.optString("type_logo");
								classBean.typename = object.optString("type_name");
								classBean.example = object.optString("example");
								list.add(classBean);
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
				adapter = new ClassAdapter(getActivity(), list);
				classlv.setAdapter(adapter);
				break;
			}
			return true;
		}
	});
}
