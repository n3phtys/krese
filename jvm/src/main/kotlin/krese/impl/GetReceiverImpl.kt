package krese.impl

import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.instance
import krese.AuthVerifier
import krese.BusinessLogic
import krese.GetReceiver
import krese.data.Email
import krese.data.GetResponse
import krese.data.GetTotalResponse
import krese.data.UniqueReservableKey
import org.joda.time.DateTime

class GetReceiverImpl(private val kodein: Kodein) : GetReceiver {
    private val authVerifier: AuthVerifier = kodein.instance()
    private val businessLogic: BusinessLogic = kodein.instance()


    override fun retrieve(key: UniqueReservableKey, from : DateTime, to : DateTime, callerEmail: Email?): GetResponse? {
        return businessLogic.retrieveReservations(key, from, to, callerEmail)
    }
    override fun retrieveAll(callerEmail: Email?): GetTotalResponse {
        return businessLogic.retrieveKeys(callerEmail)
    }
}