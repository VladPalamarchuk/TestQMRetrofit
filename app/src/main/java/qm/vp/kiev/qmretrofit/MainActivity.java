package qm.vp.kiev.qmretrofit;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.squareup.okhttp.Response;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.http.Multipart;
import retrofit.http.POST;
import retrofit.http.Part;


public class MainActivity extends ActionBarActivity {

    private static final String TAG = MainActivity.class.getName();

    final String BASE_URL = "http://mob.getnowpanel.ru";

    public interface ApiService {

        @Multipart
        @POST("/api/users/method/registration")
        public void getDummyContent(@Part("user_phone") String userPhone, Callback<String> callback);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.activityMainPost).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RestAdapter restAdapter = new RestAdapter.Builder()
                        .setEndpoint(BASE_URL)
                        .build();

                ApiService apiService = restAdapter.create(ApiService.class);

                apiService.getDummyContent("093", new Callback<String>() {
                    @Override
                    public void success(String s, Response response) {
                        Log.e(TAG, "success. s: " + s);
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        Log.e(TAG, "error");
                    }
                });
            }
        });
    }
}
