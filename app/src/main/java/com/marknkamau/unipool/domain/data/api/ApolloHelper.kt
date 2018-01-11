package com.marknkamau.unipool.domain.data.api

import com.apollographql.apollo.ApolloClient
import okhttp3.OkHttpClient

object ApolloHelper {
    private val API_ENDPOINT = "http://unipool.herokuapp.com/graphql"
    val apolloClient: ApolloClient by lazy {

        val okHttpClient = OkHttpClient.Builder().build()

        ApolloClient.builder()
                .serverUrl(API_ENDPOINT)
                .okHttpClient(okHttpClient)
                .build()
    }
}
