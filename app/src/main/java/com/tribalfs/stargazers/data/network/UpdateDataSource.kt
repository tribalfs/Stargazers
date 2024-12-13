package com.tribalfs.stargazers.data.network

import android.os.Parcelable
import com.tribalfs.stargazers.BuildConfig
import com.tribalfs.stargazers.data.model.Tags
import com.tribalfs.stargazers.data.network.api.RetrofitClient.instance
import dev.oneuiproject.oneui.layout.AppInfoLayout.Status
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue
import retrofit2.Call

@Parcelize
data class Update(
    val status: @RawValue Status,
    val latestRelease: String?
): Parcelable

object UpdateDataSource {
    suspend fun getUpdate(): Update = withContext(Dispatchers.IO){
        try {
            val gitHubApi = instance
            val call: Call<List<Tags>> =
                gitHubApi.getTags("tribalfs", "Stargazers")
            val response = call.execute()

            return@withContext  if (response.isSuccessful && response.body() != null) {
                val latestTag = response.body()!!.firstOrNull()?.name
                if (latestTag == null || latestTag == BuildConfig.VERSION_NAME) {
                    Update(Status.NoUpdate, latestTag)
                } else {
                    Update(Status.UpdateAvailable, latestTag)
                }
            } else {
                Update(Status.NotUpdatable, null)
            }
        } catch (e: Exception) {
            return@withContext  Update(Status.NotUpdatable, null)
        }
    }
}