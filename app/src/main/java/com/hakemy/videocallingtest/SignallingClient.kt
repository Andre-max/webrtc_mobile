package me.amryousef.webrtc_demo

import android.content.Context
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.util.Log
import com.google.firebase.database.*
import com.google.gson.Gson
import com.google.gson.JsonObject
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.features.json.GsonSerializer
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.websocket.WebSockets
import io.ktor.client.features.websocket.ws
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.readText
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import org.webrtc.IceCandidate
import org.webrtc.SessionDescription
import java.lang.Exception
import java.util.*


@ExperimentalCoroutinesApi
@KtorExperimentalAPI
class SignallingClient(
    private val listener: SignallingClientListener,val room:String
) : CoroutineScope {

    companion object {
        private const val HOST_ADDRESS = "196.221.149.227"
    }


    private val job = Job()

    private val gson = Gson()

    override val coroutineContext = Dispatchers.IO + job

    private val client = HttpClient(CIO) {
        install(WebSockets)
        install(JsonFeature) {
            serializer = GsonSerializer()
        }
    }

    private val sendChannel = ConflatedBroadcastChannel<String>()

    init {

        Log.e("room",room);
        val database: FirebaseDatabase = FirebaseDatabase.getInstance()
        val myRef: DatabaseReference = database.getReference(room)

// Read from the database
        // Read from the database
        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) { // This method is called once with the initial value and again

                try {


                // whenever data at this location is updated.
                val value =
                    dataSnapshot.getValue(String::class.java)!!
                val jsonObject = gson.fromJson(value, JsonObject::class.java)
                Log.e("answer",gson.fromJson(jsonObject, SessionDescription::class.java).toString())


                if (jsonObject.has("serverUrl")) {
                    listener.onIceCandidateReceived(gson.fromJson(jsonObject, IceCandidate::class.java))
                }
                else if (jsonObject.has("type") && jsonObject.get("type").asString == "OFFER") {
                    listener.onOfferReceived(gson.fromJson(jsonObject, SessionDescription::class.java))


                }
                else if (jsonObject.has("type") && jsonObject.get("type").asString == "ANSWER") {
                    listener.onAnswerReceived(gson.fromJson(jsonObject, SessionDescription::class.java))
                    Log.e("answer",gson.fromJson(jsonObject, SessionDescription::class.java).toString());
                }

                } catch (Exception : Exception)
                {

                }


            }

            override fun onCancelled(error: DatabaseError) { // Failed to read value
            }
        })








      //  connect()
    }

    private fun connect() = launch {
        client.ws(host = HOST_ADDRESS, path = "/connect") {
            listener.onConnectionEstablished()
            val sendData = sendChannel.openSubscription()

                while (true) {







                    sendData.poll()?.let {
                        Log.v(this@SignallingClient.javaClass.simpleName, "Sending: $it")
                        outgoing.send(Frame.Text(it))
                    }
                    incoming.poll()?.let { frame ->
                        if (frame is Frame.Text) {
                            val data = frame.readText()
                            Log.v(this@SignallingClient.javaClass.simpleName, "Received: $data")


                            val jsonObject = gson.fromJson(data, JsonObject::class.java)
                            Log.e("answer",gson.fromJson(jsonObject, SessionDescription::class.java).toString())

                            withContext(Dispatchers.Main) {
                                if (jsonObject.has("serverUrl")) {
                                    listener.onIceCandidateReceived(gson.fromJson(jsonObject, IceCandidate::class.java))
                                }
                                else if (jsonObject.has("type") && jsonObject.get("type").asString == "OFFER") {
                                    listener.onOfferReceived(gson.fromJson(jsonObject, SessionDescription::class.java))


                                }
                                else if (jsonObject.has("type") && jsonObject.get("type").asString == "ANSWER") {
                                    listener.onAnswerReceived(gson.fromJson(jsonObject, SessionDescription::class.java))
                                    Log.e("answer",gson.fromJson(jsonObject, SessionDescription::class.java).toString());
                                }
                            }
                        }
                    }
                }

        }
    }

    fun send(dataObject: Any?, room1: String) = runBlocking {

        // Write a message to the database



        Log.e("call",gson.toJson(dataObject).toString())
        val database: FirebaseDatabase = FirebaseDatabase.getInstance()
        val myRef: DatabaseReference = database.getReference(room1)

        myRef.setValue(gson.toJson(dataObject))


        sendChannel.send(gson.toJson(dataObject))
    }

    fun destroy() {
        client.close()
        job.complete()
    }
}