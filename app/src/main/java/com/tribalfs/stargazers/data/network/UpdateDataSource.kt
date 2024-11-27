package com.tribalfs.stargazers.data.network

import android.os.Parcelable
import com.tribalfs.stargazers.BuildConfig
import com.tribalfs.stargazers.data.model.Release
import com.tribalfs.stargazers.data.network.api.RetrofitClient.instance
import dev.oneuiproject.oneui.layout.AppInfoLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.parcelize.Parcelize
import retrofit2.Call

@Parcelize
data class Update(
    val status: Int,
    val latestRelease: String?
): Parcelable

object UpdateDataSource {
    suspend fun getUpdate(): Update = withContext(Dispatchers.IO){
        try {
            val gitHubApi = instance
            val call: Call<Release> =
                gitHubApi.getLatestRelease("tribalfs", "Stargazers")
            val response = call.execute()

            return@withContext  if (response.isSuccessful && response.body() != null) {
                val latestRelease = response.body()!!.tag_name
                if (latestRelease == BuildConfig.VERSION_NAME) {
                    Update(AppInfoLayout.NO_UPDATE, latestRelease)
                } else {
                    Update(AppInfoLayout.UPDATE_AVAILABLE, latestRelease)
                }
            } else {
                Update(AppInfoLayout.NOT_UPDATEABLE, null)
            }
        } catch (e: Exception) {
            return@withContext  Update(AppInfoLayout.NOT_UPDATEABLE, null)
        }
    }
}