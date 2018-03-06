package krese.impl

import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.instance
import krese.AuthVerifier
import krese.BusinessLogic
import krese.GetReceiver
import krese.data.GetResponse
import krese.data.UniqueReservableKey

class GetReceiverImpl(private val kodein: Kodein) : GetReceiver {
    private val authVerifier: AuthVerifier = kodein.instance()
    private val businessLogic: BusinessLogic = kodein.instance()


    override fun retrieve(key: UniqueReservableKey): GetResponse? {
        return businessLogic.retrieveReservations(key)
    }
}