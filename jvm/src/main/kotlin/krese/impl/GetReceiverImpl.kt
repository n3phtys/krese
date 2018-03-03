package krese.impl

import com.github.salomonbrys.kodein.Kodein
import krese.GetReceiver
import krese.data.GetResponse
import krese.data.UniqueReservableKey

class GetReceiverImpl(private val kodein: Kodein) : GetReceiver {
    override fun accept(key: UniqueReservableKey): GetResponse? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}