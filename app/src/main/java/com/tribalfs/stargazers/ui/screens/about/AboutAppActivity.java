package com.tribalfs.stargazers.ui.screens.about;

import static dev.oneuiproject.oneui.layout.AppInfoLayout.NO_UPDATE;
import static com.tribalfs.stargazers.ui.core.util.IsOnlineKt.isOnline;
import static com.tribalfs.stargazers.ui.core.util.OpenApplicationSettingsKt.openApplicationSettings;
import static com.tribalfs.stargazers.ui.core.util.OpenUrlKt.openUrl;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;


import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import dev.oneuiproject.oneui.layout.AppInfoLayout;

import com.tribalfs.stargazers.BuildConfig;
import com.tribalfs.stargazers.R;
import com.tribalfs.stargazers.data.api.GitHubService;
import com.tribalfs.stargazers.data.api.RetrofitClient;
import com.tribalfs.stargazers.data.model.Release;
import retrofit2.Call;
import retrofit2.Response;

public class AboutAppActivity extends AppCompatActivity {
    private AppInfoLayout appInfoLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_app);

        appInfoLayout = findViewById(R.id.appInfoLayout);

        appInfoLayout.addOptionalText("OneUI Design version " + BuildConfig.ONEUI_DESIGN_VERSION);

        appInfoLayout.setMainButtonClickListener(new AppInfoLayout.OnClickListener() {
            @Override
            public void onUpdateClicked(View v) {
                openUrl(AboutAppActivity.this,
                        "https://github.com/tribalfs/Stargazers/raw/master/app/release/app-release.apk");
            }

            @Override
            public void onRetryClicked(View v) {
                fetchLatestRelease();
            }
        });
        fetchLatestRelease();
    }

    @Override
    protected void onStart(){
        super.onStart();

    }

    private void fetchLatestRelease() {
        appInfoLayout.setStatus(AppInfoLayout.LOADING);

        if (!isOnline(getApplicationContext())){
            appInfoLayout.setStatus(AppInfoLayout.NO_CONNECTION);
            return;
        }

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> {
            try {
                GitHubService gitHubApi = RetrofitClient.getInstance();
                Call<Release> call = gitHubApi.getLatestRelease("tribalfs", "Stargazers");
                Response<Release> response = call.execute();

                if (response.isSuccessful() && response.body() != null) {
                    String latestRelease = response.body().tag_name;

                    runOnUiThread(() -> {
                        if (latestRelease.equals(BuildConfig.VERSION_NAME)) {
                            appInfoLayout.setStatus(NO_UPDATE);
                        }else{
                            appInfoLayout.setStatus(AppInfoLayout.UPDATE_AVAILABLE);
                        }
                    });
                } else {
                    runOnUiThread(() -> {
                        appInfoLayout.setStatus(AppInfoLayout.NOT_UPDATEABLE);
                    });
                }
            } catch (Exception e) {
                runOnUiThread(() -> {
                    appInfoLayout.setStatus(AppInfoLayout.NOT_UPDATEABLE);
                });
            }
        });

        executorService.shutdown();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_about_app, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_app_info) {
            openApplicationSettings(this);
            return true;
        }
        return false;
    }


    public void openGitHubPage(View v) {
        openUrl(this,"https://github.com/tribalfs/Stargazers");
    }

    public void openOSL(View v) {
        openUrl(this,"https://raw.githubusercontent.com/tribalfs/Stargazers/refs/heads/master/app/OpenSourceLicenses");
    }

}