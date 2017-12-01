package fr.xebia.graphql

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.squareup.moshi.Moshi
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

data class Fake(val id: String, val firstName: String, val lastName: String)

class MainActivity : AppCompatActivity() {

    companion object {
        val JSON = MediaType.parse("application/json; charset=utf-8")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val client = OkHttpClient()
        val request = Request.Builder()
                .url("http://fake.graphql.guru/graphql/")
                .post(RequestBody.create(JSON, "{\"query\":\"{Fake{id,firstName,lastName}}\"}"))
                .build()
        val moshi = Moshi.Builder().build()
        val userAdapter = moshi.adapter(Fake::class.java)
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call?, e: IOException?) {
            }

            override fun onResponse(call: Call?, response: Response) {
                val str = response.body()?.string()
                if (str != null) {
                    val fake: Fake? = userAdapter.fromJson(JSONObject(str).getJSONObject("data").getString("Fake"))
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
