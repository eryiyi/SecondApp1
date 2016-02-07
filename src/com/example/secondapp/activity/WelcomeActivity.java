package com.example.secondapp.activity;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.location.LocationManagerProxy;
import com.amap.api.location.LocationProviderProxy;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.example.secondapp.MainActivity;
import com.example.secondapp.R;
import com.example.secondapp.SecondApplication;
import com.example.secondapp.base.BaseActivity;
import com.example.secondapp.city.CityList;
import com.example.secondapp.http.AsyncHttpResponseHandler;
import com.example.secondapp.http.HttpClientUtils;
import com.example.secondapp.http.HttpParams;
import com.example.secondapp.serviceId.ServerId;
import com.example.secondapp.util.StringUtil;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class WelcomeActivity extends BaseActivity implements View.OnClickListener,Runnable,AMapLocationListener {
    private LocationManagerProxy mLocationManagerProxy;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.welcome);
        Boolean flag = StringUtil.isOPen(WelcomeActivity.this);
        if(!flag){
            Intent intent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivityForResult(intent, 0);
        }
        init();
        // 启动一个线程
        new Thread(WelcomeActivity.this).start();
    }

    @Override
    public void onClick(View view) {

    }

    @Override
    public void run() {
        try {
            // 3秒后跳转到登录界面
            Thread.sleep(3000);
            if(!StringUtil.isNullOrEmpty(getGson().fromJson(getSp().getString("password", ""), String.class)) && !StringUtil.isNullOrEmpty(getGson().fromJson(getSp().getString("mobile", ""), String.class))){
                login(getGson().fromJson(getSp().getString("mobile", ""), String.class), getGson().fromJson(getSp().getString("password", ""), String.class));
            }else {
//                if(!StringUtil.isNullOrEmpty(getGson().fromJson(getSp().getString("city", ""), String.class))){
//                   //如果选择了
                    Intent intent = new Intent(WelcomeActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
//                }else{
//                    Intent intent = new Intent(WelcomeActivity.this, CityList.class);
//                    startActivity(intent);
//                    finish();
//                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    JSONObject object;
    void login(String mobile, String pwr){
        HttpParams params = new HttpParams();
        params.put("mobile", mobile);
        params.put("password", pwr);
        HttpClientUtils.getInstance().post(ServerId.serveradress, ServerId.logonurl, params, new AsyncHttpResponseHandler(){
            @Override
            public void onSuccess(JSONObject jsonObject) {
                try {
                    object = jsonObject.getJSONObject("data");
                    com.example.secondapp.bean.Response.code = jsonObject.getInt("code");
                } catch (JSONException e) {
                    e.printStackTrace();
                }finally{
                    Message message = new Message();
                    message.what = 123;
                    handler.sendMessage(message);
                }
            }
        });
    }

    Handler handler = new Handler(new Handler.Callback() {

        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case 123:
                    if(com.example.secondapp.bean.Response.code == 200 && object != null){
                        if(object != null){
                            //-------保存登陆的信息-------
                            save("uid", object.optInt("uid"));
                            try {
                                save("user_name", object.getString("user_name"));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            try {
                                save("mobile", object.getString("mobile"));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            save("is_login", "1");
                            Intent intent = new Intent(WelcomeActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        }else {
                            Intent intent = new Intent(WelcomeActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    }else{
                        //
                        Intent intent = new Intent(WelcomeActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    }
                    break;
                default:
                    break;
            }
            return true;
        }
    });


    /**
     * 初始化定位
     */
    private void init() {
        // 初始化定位，只采用网络定位
        mLocationManagerProxy = LocationManagerProxy.getInstance(this);
        mLocationManagerProxy.setGpsEnable(false);
        // 此方法为每隔固定时间会发起一次定位请求，为了减少电量消耗或网络流量消耗，
        // 注意设置合适的定位时间的间隔（最小间隔支持为2000ms），并且在合适时间调用removeUpdates()方法来取消定位请求
        // 在定位结束后，在合适的生命周期调用destroy()方法
        // 其中如果间隔时间为-1，则定位只定一次,
        // 在单次定位情况下，定位无论成功与否，都无需调用removeUpdates()方法移除请求，定位sdk内部会移除
        mLocationManagerProxy.requestLocationData(
                LocationProviderProxy.AMapNetwork, 60 * 10000, 15, this);

    }

    @Override
    public void onLocationChanged(AMapLocation amapLocation) {

        if (amapLocation != null
                && amapLocation.getAMapException().getErrorCode() == 0) {
            SecondApplication.lat = amapLocation.getLatitude();
            SecondApplication.lon = amapLocation.getLongitude();
            SecondApplication.cityName =amapLocation.getCity();
            SecondApplication.desc =amapLocation.getAddress();
        } else {
            Log.e("AmapErr", "Location ERR:" + amapLocation.getAMapException().getErrorCode());
        }

    }

    @Override
    public void onPause() {
        super.onPause();
        // 移除定位请求
        if(mLocationManagerProxy != null){
            mLocationManagerProxy.removeUpdates(this);
            // 销毁定位
            mLocationManagerProxy.destroy();
        }
    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

    @Override
    public void onResume() {
        super.onResume();
    }
}
