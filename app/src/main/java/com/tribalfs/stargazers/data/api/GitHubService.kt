package com.tribalfs.stargazers.data.api

import com.tribalfs.stargazers.data.model.Stargazer
import com.tribalfs.stargazers.data.model.StargazerDetails
import com.tribalfs.stargazers.data.model.Release
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface GitHubService {
    @GET("repos/{owner}/{repo}/stargazers")
    suspend fun getStargazers(
        @Path("owner") owner: String,
        @Path("repo") repo: String
    ): List<Stargazer>

    @GET("users/{username}")
    suspend fun getUserDetails(
        @Path("username") username: String
    ): StargazerDetails

    @GET("repos/{owner}/{repo}/releases/latest")
    fun getLatestRelease(
        @Path("owner") owner: String,
        @Path("repo") repo: String
    ): Call<Release>
}

