package krese.impl

import com.github.salomonbrys.kodein.Kodein
import krese.MailService
import krese.MailTemplater

class MailServiceImpl(private val kodein: Kodein) : MailService, MailTemplater {
    override fun buildLoginMail() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun sendEmail(receiver: String, body: String, text: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}
