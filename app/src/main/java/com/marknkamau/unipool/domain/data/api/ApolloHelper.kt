package com.marknkamau.unipool.domain.data.api

import com.apollographql.apollo.ApolloClient
import okhttp3.OkHttpClient

class ApolloHelper {
    private val API_ENDPOINT = "http://unipool.herokuapp.com/graphql"

    val apolloClient: ApolloClient

    init {
        val okHttpClient = OkHttpClient.Builder().build()

        apolloClient = ApolloClient.builder()
                .serverUrl(API_ENDPOINT)
                .okHttpClient(okHttpClient)
                .build()
    }
}
