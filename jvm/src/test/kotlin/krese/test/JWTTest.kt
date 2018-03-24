package krese.test

import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.bind
import com.github.salomonbrys.kodein.singleton
import krese.*
import krese.data.Email
import krese.data.JWTPayload
import krese.data.LinkActions
import krese.data.buildUserProfile
import krese.impl.AuthVerifierImpl
import org.joda.time.DateTime
import org.junit.Test
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertSame

class JWTTest {

class ApplicationMockConfig(): ApplicationConfiguration {
    override fun globalMailDir(): String? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override val mailTestTarget: String
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    override val applicationProtocol: String
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    override val webDirectory: String
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    override val mailUsername: String
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    override val mailPassword: String
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    override val mailFrom: String
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    override val mailHost: String
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    override val mailPort: Int
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    override val mailStarttls: Boolean
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    override val mailAuth: Boolean
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    override val reservablesDirectory: String
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    override val applicationHost: String
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    override val applicationPort: Int
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    override val hashSecret: String
        get() = "mysecret"
}

    val kodein = Kodein {
        bind<ApplicationConfiguration>() with singleton {  ApplicationMockConfig() }
    }


    @Test
    fun authverifier_encodesAndDecodesJWTCorrectly() {
        val auth = AuthVerifierImpl(kodein)
        val userProfile = buildUserProfile(Email("my@email.com"), DateTime.now().minusSeconds(5), DateTime.now().plusSeconds(10))
        val input1 = JWTPayload(LinkActions.CreateNewEntry, listOf("param a", "Param b", "[54,24,643]"), userProfile)
        val input2 = JWTPayload(null, listOf("Do not need"), userProfile)
        val input3 = JWTPayload(null, listOf("Do not need"), userProfile.copy(validTo =  userProfile.validTo - 11000))
        val encoded1 = auth.encodeJWT(input1)
        val encoded2 = auth.encodeJWT(input2)
        val encoded3 = auth.encodeJWT(input3)
        val output1 = auth.decodeJWT(encoded1!!)
        val output2 = auth.decodeJWT(encoded2!!)
        val output3 = auth.decodeJWT(encoded3!!)
        assertEquals(input1, output1)
        assertEquals(input2, output2)
        assertEquals(null, output3)
    }
}