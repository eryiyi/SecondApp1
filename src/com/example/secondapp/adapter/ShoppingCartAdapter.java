package com.example.secondapp.adapter;

import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;
import com.example.secondapp.R;
import com.example.secondapp.activity.ShoppingCartList;
import com.example.secondapp.bean.ShoppingcartBean;
import com.example.secondapp.http.AsyncHttpResponseHandler;
import com.example.secondapp.http.HttpClientUtils;
import com.example.secondapp.http.HttpParams;
import com.example.secondapp.networkbitmap.BitmapUtil;
import com.example.secondapp.serviceId.ServerId;
import com.example.secondapp.sharedprefernces.SharedPrefsUtil;
import com.example.secondapp.view.MyDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ShoppingCartAdapter extends BaseAdapter{
	List<ShoppingcartBean> list;
	LayoutInflater inflater;
	Context context;
	public static double totalall;
	int count;
	ImageView deletered;
	double UnitPrice;
	int flag = 0;
	int countall = 0;
	private String uid;
	public ShoppingCartAdapter(Context context, List<ShoppingcartBean> list, String uid){
		this.list = list;
		inflater = LayoutInflater.from(context);
		this.context = context;  
		this.uid = uid;
	}
	@Override
	public int getCount() {
		if(list != null){
			return list.size();
		}
		return 0;
	}

	@Override
	public Object getItem(int arg0) {
		if(list != null && arg0 < list.size()){
			return list.get(arg0);
		}
		return null;
	}

	@Override
	public long getItemId(int arg0) {
	
		return arg0;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		final Holder holder;
		if (convertView == null) {
			holder = new Holder();
			convertView = inflater.inflate(R.layout.itemshoppingcart, null);
			holder.scpic = (ImageView) convertView.findViewById(R.id.comimage);
			holder.scdescribe = (TextView) convertView.findViewById(R.id.textname);
			holder.scpri = (TextView) convertView.findViewById(R.id.text1);
			holder.sccount = (TextView) convertView.findViewById(R.id.count);
			holder.total = (TextView) convertView.findViewById(R.id.text2);
			convertView.setTag(holder);
		}else {
			holder = (Holder) convertView.getTag();
		}
		if (position < list.size()) {
			UnitPrice = Double.parseDouble(list.get(position).fruitprice);
			BitmapUtil.getInstance().download("http://115.29.208.113/", list.get(position).fruitimage, holder.scpic);
			holder.scdescribe.setText(list.get(position).fruitname);
			holder.scpri.setText(list.get(position).fruitprice);
			holder.sccount.setText("" + list.get(position).fruitcount);
			holder.total.setText("" + list.get(position).fruitcount * UnitPrice);
		}
		
		((TextView) convertView.findViewById(R.id.minus)).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				count = list.get(position).fruitcount;
				if (count > 1) {
					HttpParams params = new HttpParams();
					params.put("userId", uid);
					params.put("product_id", list.get(position).fruitid);
					params.put("numbers", --count);
					HttpClientUtils.getInstance().post(ServerId.serveradress, ServerId.cartnumber, params, new AsyncHttpResponseHandler(){
						@Override
						public void onSuccess(JSONObject jsonObject) throws JSONException {
							System.out.println("jsonObject = " + jsonObject);
							super.onSuccess(jsonObject);
						}
					});
					holder.sccount.setText("" + count);
					holder.total.setText("" + count * Double.parseDouble(list.get(position).fruitprice));
					ShoppingCartList.list.get(position).fruitcount = count;
					totalall = totalall - Double.parseDouble(list.get(position).fruitprice);
					ShoppingCartList.totalprice.setText("" + totalall);
				}
			}
		});
		
		((TextView) convertView.findViewById(R.id.plus)).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				count = list.get(position).fruitcount;
				HttpParams params = new HttpParams();
				params.put("userId", uid);
				params.put("product_id", list.get(position).fruitid);
				params.put("numbers", ++count);
				HttpClientUtils.getInstance().post(ServerId.serveradress, ServerId.cartnumber, params, new AsyncHttpResponseHandler(){
					@Override
					public void onSuccess(JSONObject jsonObject) throws JSONException {
						System.out.println("jsonObject = " + jsonObject);
						super.onSuccess(jsonObject);
					}
				});
				holder.sccount.setText("" + count);
				holder.total.setText("" + count * Double.parseDouble(list.get(position).fruitprice));
				ShoppingCartList.list.get(position).fruitcount = count;
				totalall = totalall + Double.parseDouble(list.get(position).fruitprice);
				ShoppingCartList.totalprice.setText("" + totalall);
			}
		});
		
		convertView.findViewById(R.id.deletered).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				showMyDialog(position);
			}
		});
		
		System.out.println("countall = " + countall);
		if (flag == 0 && countall < list.size() ) {
			totalall += list.get(position).fruitcount * Double.parseDouble(list.get(position).fruitprice);
			ShoppingCartList.totalprice.setText("" + totalall);
			ShoppingCartList.gopaynum.setText("(" + list.size() + ")");
		}else{
			ShoppingCartList.totalprice.setText("" + totalall);
		}
		countall++;
		return convertView;
	}
	
	class Holder{
		ImageView scpic;
		TextView scdescribe;
		TextView scpri;
		TextView sccount;
		TextView total;
	}

	public void showMyDialog(final int position) {
		MyDialog dialog = new MyDialog(context);
		//dialog.setIcon(R.drawable.a008);
		dialog.setTitle("删除商品");
		dialog.setMessage("   确定从购物车删除这个商品吗？   ");
		dialog.setPositiveButton("取消", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				System.out.println("list.size() = " + 	list.size());
			}
		});
		dialog.setNegativeButton("确定", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				totalall -=  list.get(position).fruitcount * Double.parseDouble(list.get(position).fruitprice);
				ShoppingCartList.totalprice.setText("¥" + totalall);
				HttpParams params = new HttpParams();
				params.put("userId", uid);
				params.put("cart_id", list.get(position).cartid);
				HttpClientUtils.getInstance().post(ServerId.serveradress, ServerId.deleteshoppingcart, params, new AsyncHttpResponseHandler(){
				 
				});
				list.remove(position);
				SharedPrefsUtil.putValue(context, "Setting", list.size());
				if (list.size() == 0) {
					ShoppingCartList.gopayfor.setBackgroundColor(context.getResources().getColor(R.color.common_grey));
					ShoppingCartList.nocom.setVisibility(View.VISIBLE);
				}else{
					ShoppingCartList.gopaynum.setText("(" + list.size() + ")");
					ShoppingCartList.adapter.notifyDataSetChanged();
					flag = 1;
				}
				
			}
		});
		dialog.show();
	}
}
