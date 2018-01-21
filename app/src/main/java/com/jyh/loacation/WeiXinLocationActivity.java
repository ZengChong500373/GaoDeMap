package com.jyh.loacation;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps2d.AMap;
import com.amap.api.maps2d.CameraUpdateFactory;
import com.amap.api.maps2d.LocationSource;
import com.amap.api.maps2d.MapView;
import com.amap.api.maps2d.model.BitmapDescriptorFactory;
import com.amap.api.maps2d.model.CameraPosition;
import com.amap.api.maps2d.model.LatLng;
import com.amap.api.maps2d.model.Marker;
import com.amap.api.maps2d.model.MarkerOptions;
import com.amap.api.maps2d.model.MyLocationStyle;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;


public class WeiXinLocationActivity extends AppCompatActivity  implements AMapLocationListener ,PoiSearch.OnPoiSearchListener ,AMap.OnMapTouchListener,AMap.OnCameraChangeListener {
    private MapView mapView;
    private AMap aMap;
    //声明mlocationClient对象
    private AMapLocationClient mlocationClient;
    //声明mLocationOption对象
    private AMapLocationClientOption mLocationOption = null;
    private Marker mk;
    private boolean isFristEntry = true;//是否是第一次进入
    private LocationSource.OnLocationChangedListener mListener;
    private PoiSearch poiSearch;
    private PoiSearch.Query query;
    private String cityCode;//城市编码
    private PoiItem firstLocation; //第一次定位的位置
    private TextView tvLoading;
    private MyLocationAdapter adapter;
    private PoiItem selectWhere;//当前选中的listview item 位置
    private ListView lv;
    /** 判断是不是拖动 是拖动就刷新附近 */
    private boolean isTouchMap = true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weixin_location);
        mapView = findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);
        init();
        initonClick();
    }

    private void init() {
        // 如果没有map 就获取map
        if (aMap == null) {
            aMap = mapView.getMap();
        }
        //地图缩放级别
        aMap.moveCamera(CameraUpdateFactory.zoomTo(17));
        //地图模式可选类型：MAP_TYPE_NORMAL,MAP_TYPE_SATELLITE,MAP_TYPE_NIGHT
        aMap.setMapType(AMap.MAP_TYPE_NORMAL);

        mlocationClient = new AMapLocationClient(this);
        //初始化定位参数
        mLocationOption = new AMapLocationClientOption();
      //设置定位监听
        mlocationClient.setLocationListener(this);
      //设置定位模式为高精度模式，Battery_Saving为低功耗模式，Device_Sensors是仅设备模式
        mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
       //设置定位间隔,单位毫秒,默认为2000ms
        mLocationOption.setInterval(2000);
        //设置定位参数
        mlocationClient.setLocationOption(mLocationOption);

        // 此方法为每隔固定时间会发起一次定位请求，为了减少电量消耗或网络流量消耗，
        // 注意设置合适的定位时间的间隔（最小间隔支持为1000ms），并且在合适时间调用stopLocation()方法来取消定位请求
        // 在定位结束后，在合适的生命周期调用onDestroy()方法
        // 在单次定位情况下，定位无论成功与否，都无需调用stopLocation()方法移除请求，定位sdk内部会移除
        //启动定位
        mlocationClient.startLocation();



        /** 自定义图标 */
        MyLocationStyle sy = new MyLocationStyle();
        sy.myLocationIcon(BitmapDescriptorFactory
                .fromResource(R.drawable.location_point));
        sy.strokeWidth(-1);
        /**没有蓝色定位小蓝圈*/
        sy.radiusFillColor(Color.TRANSPARENT);
        sy.strokeColor(Color.TRANSPARENT);

        aMap.setMyLocationStyle(sy);
        // 移动监听
       aMap.setOnCameraChangeListener(this);
       // 触摸监听
        aMap.setOnMapTouchListener(this);

        lv =  findViewById(R.id.lv);
        adapter = new MyLocationAdapter(getApplicationContext(), null);
        lv.setAdapter(adapter);

        tvLoading = findViewById(R.id.tvLoading);
    }
    private void initonClick() {
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1,
                                    int position, long arg3) {
                selectWhere = (PoiItem) adapter.getItem(position);
                LatLng ll = new LatLng(selectWhere.getLatLonPoint()
                        .getLatitude(), selectWhere.getLatLonPoint()
                        .getLongitude());
                mk.setPosition(ll);//mark 改变位置
                // 图层位置的变化动画
                // aMap.animateCamera(CameraUpdateFactory.newCameraPosition(new
                // CameraPosition(ll, 16, 0, 0)), 1500, null);
                aMap.animateCamera(CameraUpdateFactory
                        .newCameraPosition(new CameraPosition(ll, 17, 0, 0)));

                adapter.showSelect(position);
                isTouchMap = false;//不是滑动，所以不需要搜索附近poi
            }
        });
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        //在activity执行onDestroy时执行mMapView.onDestroy()，销毁地图
        mapView.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView.onResume ()，重新绘制加载地图
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView.onPause ()，暂停地图的绘制
        mapView.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //在activity执行onSaveInstanceState时执行mMapView.onSaveInstanceState (outState)，保存地图当前的状态
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onLocationChanged(AMapLocation amapLocation) {
        if (amapLocation != null) {
            if (amapLocation.getErrorCode() == 0) {
                //定位成功回调信息，设置相关消息
                amapLocation.getLocationType();//获取当前定位结果来源，如网络定位结果，详见定位类型表
                double lat=  amapLocation.getLatitude();//获取纬度
                double lon=  amapLocation.getLongitude();//获取经度
                amapLocation.getAccuracy();//获取精度信息
                cityCode=   amapLocation.getCityCode();//城市编码
                PoiSearch(lat,lon,cityCode);
                if (mk == null) {
                    // 添加一个标记物
                    AddMark(lat,lon);
                }
                LatLng ll = new LatLng(lat
                      ,lon);
                LatLonPoint latLonPoint=new LatLonPoint(lat,lon);
                if (isFristEntry) {
                  firstLocation = new PoiItem(amapLocation.getBuildingId(), latLonPoint,
                           "位置", amapLocation.getAddress());

                    aMap.moveCamera(CameraUpdateFactory.zoomTo(17));
                    //定位成功移动图层至定位区域
                    aMap.animateCamera(CameraUpdateFactory
                            .newCameraPosition(new CameraPosition(ll, 17, 0, 0)));
                }

            } else {
                //显示错误信息ErrCode是错误码，errInfo是错误信息，详见错误码表。
                Log.e("AmapError","location Error, ErrCode:"
                        + amapLocation.getErrorCode() + ", errInfo:"
                        + amapLocation.getErrorInfo());
            }
        }
    }
    //添加mark
    private void AddMark(double lat, double lon) {
        LatLng la = new LatLng(lat, lon);
        mk = aMap.addMarker(new MarkerOptions().position(la));
    }
    private void PoiSearch(double lat, double lon, String cityCode) {// AMapLocation
        LatLonPoint point = new LatLonPoint(lat, lon);
        query = new PoiSearch.Query(
                "",
                "汽车维修|摩托车服务|餐饮服务|购物服务|生活服务|体育休闲服务|医疗保健服务|住宿服务|风景名胜|商务住宅|政府机构及社会团体|科教文化服务|交通设施服务|金融保险服务|道路附属设施|地名地址信息|公共设施",
                cityCode);
        // "公共设施|商务住宅"
        query.setPageSize(20);// 设置每页最多返回多少条poiitem
        poiSearch = new PoiSearch(this, query);
      //  poiSearch.setBound(new PoiSearch.SearchBound(point, 1000));// 设置周边搜索的中心点以及区域
        poiSearch.setBound(new PoiSearch.SearchBound(point, 1000,true));
        poiSearch.setOnPoiSearchListener(this);//
        poiSearch.searchPOIAsyn();// 开始搜索
    }

    @Override
    public void onPoiSearched(PoiResult result, int code) {
        if (code == 1000) {//成功是1000 其他都是失败
            if (result != null & result.getQuery() != null) {
                ArrayList<PoiItem> list = result.getPois();
                Collections.sort(list, new DisComparator());
                // 点击选择listview 第一位是自己选择的地方
                list.add(0, firstLocation);
                adapter.setData(list);
                // 第一次进入定位默认选中第一个
                if (list.size() > 0 && isFristEntry == true) {
                    selectWhere = list.get(0);
                    isFristEntry = false;
                }
                // 判断是拖拽 选中第一个 是点击选中点击
                if (isTouchMap) {
                    selectWhere = list.get(0);
                    adapter.showSelect(0);
                }
                lv.setVisibility(View.VISIBLE);
                tvLoading.setVisibility(View.INVISIBLE);
            }
        } else {
            Toast.makeText(getApplicationContext(),
                    "失败"+code, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onPoiItemSearched(PoiItem poiItem, int i) {

    }
   //图层被手指触摸了
    @Override
    public void onTouch(MotionEvent motionEvent) {
        switch (motionEvent.getAction()) {
            case MotionEvent.ACTION_MOVE://表示手指在滑动自定义图层
                isTouchMap = true;
                break;
            default:
                break;
        }
    }

    // 移动的mark
    //图层在位移，因为手指触摸
    @Override
    public void onCameraChange(CameraPosition position) {
        if (mk == null) {
            LatLng ll = position.target;
            AddMark(ll.latitude, ll.longitude);
        }
        mk.setPosition(position.target);
    }
    //图层结束，改变mark位置，并搜索附近兴趣点
    @Override
    public void onCameraChangeFinish(CameraPosition position) {
        LatLng ll = position.target;
        if (mk == null) {
            AddMark(ll.latitude, ll.longitude);
        }
        mk.setPosition(position.target);
        if (isTouchMap) {
            PoiSearch(ll.latitude, ll.longitude, cityCode);
            lv.setVisibility(View.INVISIBLE);
            tvLoading.setVisibility(View.VISIBLE);
        } else {
            lv.setVisibility(View.VISIBLE);
            tvLoading.setVisibility(View.INVISIBLE);
        }

    }

    /** 距离比较 */
    class DisComparator implements Comparator<PoiItem> {
        @Override
        public int compare(PoiItem obj1, PoiItem obj2) {
            if (obj1.getDistance() > obj2.getDistance()) {
                return 1;
            } else {
                return -1;
            }

        }
    }



}
