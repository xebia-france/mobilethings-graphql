package fr.xebia.graphql

import GetUserQuery
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.TextView
import com.apollographql.apollo.ApolloCall
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.exception.ApolloException
import com.squareup.moshi.Moshi
import kotlinx.android.synthetic.main.activity_main.*
import me.lazmaid.kraph.Kraph
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

data class Fake(val id: String, val firstName: String, val lastName: String)

class MainActivity : AppCompatActivity() {

    companion object {
        const val URL = "http://fake.graphql.guru/graphql/"
        val CLIENT = OkHttpClient()
        private val MOSHI = Moshi.Builder().build()
        private val USER_ADAPTER = MOSHI.adapter(Fake::class.java)
        private val APOLLO = ApolloClient.builder().serverUrl(URL).okHttpClient(CLIENT).build()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Vanilla
        val q = getQueryVanilla()
        executeAsyncHttpPost(q, idVanillaView, firstNameVanillaView, lastNameVanillaView)

        // Kraph
        val q1 = getQueryKraph()
        executeAsyncHttpPost(q1, idKraphView, firstNameKraphView, lastNameKraphView)

        // Apollo
        APOLLO.query(GetUserQuery.builder().build())
                .enqueue(object : ApolloCall.Callback<GetUserQuery.Data>() {
                    override fun onFailure(ignored: ApolloException) {
                        // Oops
                    }

                    override fun onResponse(response: com.apollographql.apollo.api.Response<GetUserQuery.Data>) {
                        runOnUiThread({
                            idApolloView.text = response.data()?.Fake()?.id()
                            firstNameApolloView.text = response.data()?.Fake()?.firstName()
                            lastNameApolloView.text = response.data()?.Fake()?.lastName()
                        })
                    }
                })
    }

    private fun getQueryVanilla(): String = "{\"query\":\"{Fake{id,firstName,lastName}}\"}"

    private fun getQueryKraph(): String = Kraph {
        query {
            fieldObject("Fake") {
                field("id")
                field("firstName")
                field("lastName")
            }
        }
    }.toRequestString()

    private fun executeAsyncHttpPost(query: String, idView: TextView, firstNameView: TextView, lastNameView: TextView) {
        val request = Request.Builder()
                .url(URL)
                .post(RequestBody.create(MediaType.parse("application/json; charset=utf-8"), query))
                .build()
        CLIENT.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call?, e: IOException?) {
            }

            override fun onResponse(call: Call?, response: Response) {
                val str = response.body()?.string()
                if (str != null) {
                    val fake: Fake? = USER_ADAPTER.fromJson(
                            JSONObject(str).getJSONObject("data").getString("Fake"))
                    if (fake != null) {
                        runOnUiThread({
                            idView.text = fake.id
                            firstNameView.text = fake.firstName
                            lastNameView.text = fake.lastName
                        })
                    }
                }
            }
        })
    }
}
