package com.tribalfs.stargazers.data.network.api

import com.tribalfs.stargazers.data.model.Stargazer
import com.tribalfs.stargazers.data.model.StargazerDetails
import com.tribalfs.stargazers.data.model.Tags
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

    @GET("repos/{owner}/{repo}/tags")
    fun getTags(
        @Path("owner") owner: String,
        @Path("repo") repo: String
    ): Call<List<Tags>>
}

