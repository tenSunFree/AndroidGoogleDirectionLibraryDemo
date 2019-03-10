package com.home.androidgoogledirectionlibrarydemo;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.*;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.akexorcist.googledirection.DirectionCallback;
import com.akexorcist.googledirection.GoogleDirection;
import com.akexorcist.googledirection.constant.TransportMode;
import com.akexorcist.googledirection.model.Direction;
import com.akexorcist.googledirection.model.Leg;
import com.akexorcist.googledirection.model.Route;
import com.akexorcist.googledirection.model.Step;
import com.akexorcist.googledirection.util.DirectionConverter;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.*;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private int LOCATION_MIN_DISTANCE = 120, LOCATION_MIN_TIME = 4000;
    private GoogleMap googleMap;
    private SupportMapFragment mapFragment;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private Marker iAmHereMarker, godTempleMarker, longshanTempleMarker, plumLakeMarker;
    private Paint paint;
    private Bitmap.Config iAmHereConfig, godTempleConfig, longshanTempleConfig, plumLakeConfig;
    private Bitmap iAmHereBitmap, godTempleBitmap, longshanTempleBitmap, plumLakeBitmap;
    private Canvas iAmHereCanvas, godTempleCanvas, longshanTempleCanvas, plumLakeCanvas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializationMapFragment();
        initializationLocationListener();
        initializationMap();
        getCurrentLocation();
    }

    /** 初始化mapFragment */
    private void initializationMapFragment() {
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapFragment);
        mapFragment.getMapAsync(this); // 設定回呼, 記得啟用MapsSdkForAndroidApi
    }

    @Override
    public void onMapReady(GoogleMap googleMap) { // 地圖就緒可供使用時, 就會觸發回呼
        this.googleMap = googleMap;
    }

    private void initializationLocationListener() {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {

            @Override
            public void onLocationChanged(Location location) {
                Log.d("more", "onLocationChanged, Longitude: " + location.getLongitude());
                Log.d("more", "onLocationChanged, Latitude: " + location.getLatitude());
                drawMarker(location); // 繪製標誌
                drawDirections(location); // 繪製路徑
                locationManager.removeUpdates(locationListener);
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
        };
    }

    @NonNull
    private GoogleMap.OnInfoWindowClickListener onInfoWindowClickListener() {
        return new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                /** 無法為自定義信息窗口的不同組件設置偵聽器，因為信息窗口將呈現為圖像。*/
                /** 但是，您可以為整個信息窗口設置一個偵聽器，如MarkerDemoActivity.java中的onInfoWindowClick和onInfoWindowLongClick所示 */
                marker.hideInfoWindow();
            }
        };
    }

    @NonNull
    private GoogleMap.OnMarkerClickListener onMarkerClickListener() {
        return new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                switch (marker.getTitle()) {
                    case "我在這裡！":
                        Toast.makeText(MainActivity.this, "我在這裡！", Toast.LENGTH_SHORT).show();
                        break;
                    case "新竹都城隍廟":
                        Toast.makeText(MainActivity.this, "新竹都城隍廟", Toast.LENGTH_SHORT).show();
                        break;
                    case "艋舺龍山寺":
                        Toast.makeText(MainActivity.this, "艋舺龍山寺", Toast.LENGTH_SHORT).show();
                        break;
                    case "梅花湖":
                        Toast.makeText(MainActivity.this, "梅花湖", Toast.LENGTH_SHORT).show();
                        break;
                }
                return false;
            }
        };
    }

    /**
     * 初始化GoogleMap
     */
    private void initializationMap() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            if (googleMap != null) {
                googleMap.setMyLocationEnabled(true);
                googleMap.getUiSettings().setMyLocationButtonEnabled(true); // 顯示自己位置按鈕
                googleMap.getUiSettings().setAllGesturesEnabled(true); // 啟用所有手勢
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 12);
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 13);
            }
        }
    }

    /**
     * 取得當前使用者的座標位置
     */
    private void getCurrentLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            boolean isNetWorkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            if (!isGPSEnabled && !isNetWorkEnabled) {
                Toast.makeText(this, "NetWork and GPS failed", Toast.LENGTH_SHORT).show();
            } else {
                if (isGPSEnabled) {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                            LOCATION_MIN_TIME, LOCATION_MIN_DISTANCE, locationListener);
                    locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                }
                if (isNetWorkEnabled) {
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                            LOCATION_MIN_TIME, LOCATION_MIN_DISTANCE, locationListener);
                    locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                }
            }
        }
    }

    /**
     * 在map上 添加多個Marker, 以及定位到指定地點
     */
    private void drawMarker(Location location) {
        if (googleMap != null) {
            googleMap.clear();
            paint = new Paint();
            iAmHereConfig = Bitmap.Config.ARGB_8888;
            iAmHereBitmap = Bitmap.createBitmap(160, 160, iAmHereConfig);
            iAmHereCanvas = new Canvas(iAmHereBitmap);
            iAmHereCanvas.drawBitmap(BitmapFactory.decodeResource(
                    getResources(), R.drawable.money), 22, 0, paint);
            iAmHereMarker = googleMap.addMarker(
                    new MarkerOptions()
                            .position(new LatLng(location.getLatitude(), location.getLongitude()))
                            .title("我在這裡！")
                            .anchor(0.5f, 0.5f) // 设置标记锚点, 锚点就是图标围绕旋转的中心, 这里设置的是以图标自身中心为锚点
                            .icon(BitmapDescriptorFactory.fromBitmap(iAmHereBitmap))); // 设置自定义的标记图标
            godTempleConfig = Bitmap.Config.ARGB_8888;
            godTempleBitmap = Bitmap.createBitmap(160, 160, godTempleConfig);
            godTempleCanvas = new Canvas(godTempleBitmap);
            godTempleCanvas.drawBitmap(BitmapFactory.decodeResource(
                    getResources(), R.drawable.money), 22, 0, paint);
            godTempleMarker = googleMap.addMarker(
                    new MarkerOptions()
                            .position(new LatLng(24.804499, 120.965515))
                            .title("新竹都城隍廟")
                            .anchor(0.5f, 0.5f) // 设置标记锚点, 锚点就是图标围绕旋转的中心, 这里设置的是以图标自身中心为锚点
                            .icon(BitmapDescriptorFactory.fromBitmap(godTempleBitmap))); // 设置自定义的标记图标
            longshanTempleConfig = Bitmap.Config.ARGB_8888;
            longshanTempleBitmap = Bitmap.createBitmap(160, 160, longshanTempleConfig);
            longshanTempleCanvas = new Canvas(longshanTempleBitmap);
            longshanTempleCanvas.drawBitmap(BitmapFactory.decodeResource(
                    getResources(), R.drawable.money), 22, 0, paint);
            longshanTempleMarker = googleMap.addMarker(
                    new MarkerOptions()
                            .position(new LatLng(25.036798, 121.499962))
                            .title("艋舺龍山寺")
                            .anchor(0.5f, 0.5f) // 设置标记锚点, 锚点就是图标围绕旋转的中心, 这里设置的是以图标自身中心为锚点
                            .icon(BitmapDescriptorFactory.fromBitmap(longshanTempleBitmap)));
            plumLakeConfig = Bitmap.Config.ARGB_8888;
            plumLakeBitmap = Bitmap.createBitmap(160, 160, plumLakeConfig);
            plumLakeCanvas = new Canvas(plumLakeBitmap);
            plumLakeCanvas.drawBitmap(BitmapFactory.decodeResource(
                    getResources(), R.drawable.money), 22, 0, paint);
            plumLakeMarker = googleMap.addMarker(
                    new MarkerOptions()
                            .position(new LatLng(24.648708, 121.735116))
                            .title("梅花湖")
                            .anchor(0.5f, 0.5f) // 设置标记锚点, 锚点就是图标围绕旋转的中心, 这里设置的是以图标自身中心为锚点
                            .icon(BitmapDescriptorFactory.fromBitmap(plumLakeBitmap)));

            googleMap.setInfoWindowAdapter(new CustomInfoWindowAdapter());
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                    new LatLng(location.getLatitude(), location.getLongitude()), 9));
            iAmHereMarker.showInfoWindow(); // 让信息窗口直接显示出来, 不用点击标记才显示
        }
    }

    /**
     * 自定義信息窗口和其內容
     */
    class CustomInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

        private final View mWindow;

        CustomInfoWindowAdapter() {
            mWindow = getLayoutInflater().inflate(R.layout.custom_info_window, null);
        }

        @Override
        public View getInfoWindow(Marker marker) {
            render(marker, mWindow);
            return mWindow;
        }

        @Override
        public View getInfoContents(Marker marker) {
            return null;
        }

        private void render(final Marker marker, View view) {
            int badge;
            if (marker.equals(iAmHereMarker)) {
                badge = R.drawable.i_am_here;
            } else if (marker.equals(godTempleMarker)) {
                badge = R.drawable.hsinchu_city_god_temple;
            } else if (marker.equals(longshanTempleMarker)) {
                badge = R.drawable.longshan_temple;
            } else if (marker.equals(plumLakeMarker)) {
                badge = R.drawable.plum_lake;
            } else {
                badge = 0;
            }
            ((ImageView) view.findViewById(R.id.badge)).setImageResource(badge);
            TextView titleUi = ((TextView) view.findViewById(R.id.title));
            titleUi.setText(marker.getTitle());
        }
    }

    private void drawDirections(Location location) { // 繪製路徑, 記得啟用DirectionsApi
        GoogleDirection.withServerKey("DirectionsKey") // ServerKey要另外創建一個沒有限制的Key來使用, 不然會出錯
                .from(new LatLng(location.getLatitude(), location.getLongitude()))
                .and(new LatLng(24.804499, 120.965515))
                .and(new LatLng(25.036798, 121.499962))
                .to(new LatLng(24.648708, 121.735116))
                .transportMode(TransportMode.DRIVING)
                .execute(new DirectionCallback() {
                    @Override
                    public void onDirectionSuccess(Direction direction, String rawBody) {
                        Log.d("more", "MainActivity, onDirectionSuccess");
                        if(direction.isOK()) {
                            Log.d("more", "MainActivity, onDirectionSuccess, isOK");
                            Route route = direction.getRouteList().get(0);
                            int legCount = route.getLegList().size();
                            for (int index = 0; index < legCount; index++) {
                                Leg leg = route.getLegList().get(index);
                                List<Step> stepList = leg.getStepList();
                                ArrayList<PolylineOptions> polylineOptionList = DirectionConverter.createTransitPolyline(MainActivity.this, stepList, 5, Color.RED, 3, Color.BLUE);
                                for (PolylineOptions polylineOption : polylineOptionList) {
                                    googleMap.addPolyline(polylineOption);
                                }
                            }
                            setCameraWithCoordinationBounds(route);
                        } else {
                            Log.d("more", "MainActivity, onDirectionSuccess, !isOK");
                            Log.d("more", "MainActivity, onDirectionSuccess, " +
                                    "direction.getStatus(): " + direction.getStatus());
                            Log.d("more", "MainActivity, onDirectionSuccess, " +
                                    "direction.getErrorMessage(): " + direction.getErrorMessage());
                        }
                    }

                    @Override
                    public void onDirectionFailure(Throwable t) {
                        Log.d("more", "MainActivity, onDirectionFailure");
                    }
                });
    }

    private void setCameraWithCoordinationBounds(Route route) {
        LatLng southwest = route.getBound().getSouthwestCoordination().getCoordination();
        LatLng northeast = route.getBound().getNortheastCoordination().getCoordination();
        LatLngBounds bounds = new LatLngBounds(southwest, northeast);
        googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100));
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapFragment.onResume();
        getCurrentLocation();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapFragment.onPause();
        locationManager.removeUpdates(locationListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapFragment.onDestroy();
    }
}
