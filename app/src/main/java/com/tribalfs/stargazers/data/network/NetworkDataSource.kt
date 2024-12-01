package com.tribalfs.stargazers.data.network

import com.tribalfs.stargazers.data.model.Stargazer
import com.tribalfs.stargazers.data.network.api.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext

sealed class ApiResult<out T> {
    data class Success<out T>(val data: T) : ApiResult<T>()
    data class Failure(val e: Exception) : ApiResult<Nothing>()
}

object NetworkDataSource {

    suspend fun fetchStargazers(): ApiResult<List<Stargazer>>  {
        return try {
            val stargazers = getStargazers()
            ApiResult.Success(stargazers)
        } catch (e: Exception) {
            ApiResult.Failure(e)
        }
    }

    private suspend fun getStargazers() = withContext(Dispatchers.IO){
        val repoList = listOf(
            "sesl-androidx",
            "sesl-material-components-android",
            "oneui-design",
            "Stargazers"
        )

        with(RetrofitClient.instance) {
            val stargazerList = repoList.map { repoName ->
                async {
                    getStargazers("tribalfs", repoName).map { stargazer ->
                        stargazer to repoName
                    }
                }
            }.awaitAll().flatten()

            val stargazerMap = stargazerList.groupBy({ it.first.login }) { it }

            val mergedStargazers = stargazerMap.map { (_, stargazerTuples) ->
                val stargazer = stargazerTuples.first().first
                val starredRepos = stargazerTuples.map { it.second }.toSet()
                stargazer.copy(starredRepos = starredRepos)
            }

            return@with mergedStargazers.map {
                async {
                    val userDetails = getUserDetails(it.login)
                    it.copy(
                        name = userDetails.name,
                        location = userDetails.location,
                        company = userDetails.company,
                        email = userDetails.email,
                        twitter_username = userDetails.twitter_username,
                        blog = userDetails.blog,
                        bio = userDetails.bio,
                    )
                }
            }.awaitAll()
                .sortedWith(compareBy(
                    { it.getDisplayName().first().isDigit() },
                    { it.getDisplayName().uppercase() }
                ))
        }
    }
}