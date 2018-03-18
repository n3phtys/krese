package krese.impl

import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.instance
import krese.*
import krese.data.*
import krese.utility.getId
import org.joda.time.DateTime
import java.util.*

class BusinessLogicImpl(private val kodein: Kodein): BusinessLogic {

    private val authVerifier: AuthVerifier = kodein.instance()
    private val appConfig: ApplicationConfiguration = kodein.instance()
    private val mailService: MailService = kodein.instance()
    private val fileSystemWrapper: FileSystemWrapper = kodein.instance()
    private val databaseEncapsulation: DatabaseEncapsulation = kodein.instance()

    init {
        //mailService.sendEmail(listOf(Email(appConfig.mailTestTarget)), "krese works body", "Krese was just started")
    }


    override fun retrieveReservations(urk: UniqueReservableKey, from : DateTime, to : DateTime, callerEmail: Email?): GetResponse? {
        val res = fileSystemWrapper.getReservableToKey(urk)
        if (res != null) {
            return GetResponse(
                    res,
                    databaseEncapsulation.retrieveBookingsForKey(urk, from, to).map { it.toOutput(res.operatorEmails.contains(callerEmail?.address)) }
            )
        } else {
            return null
        }
    }

    override fun retrieveKeys(callerEmail: Email?): GetTotalResponse {
        return GetTotalResponse(fileSystemWrapper.getKeysFromDirectory().keys.toList())
    }


    fun DbBookingOutputData.managedBy(email: Email): Boolean {
        return fileSystemWrapper.getReservableToKey(this.key)?.operatorEmails?.any { it.equals(other = email.address, ignoreCase = false) } == true
    }

    fun DbBookingOutputData.createdBy(email: Email): Boolean {
        return this.email.address.equals(email.address, false)
    }



    fun getModerator(action: PostAction) : List<Email> {
        val key = databaseEncapsulation.get(action.getId())?.key
        if (key != null) {
            val ls = fileSystemWrapper.getReservableToKey(key)?.operatorEmails?.map { Email(it) }
            if (ls != null) {
                return ls
            } else {
                return listOf()
            }
        } else {
            return listOf()
        }
    }

    fun legalInGeneral(action : PostAction, verifiedSender: Email?): Email? {
        val sender : Email = when(action) {
            is CreateAction -> action.email
            else -> {
                if (verifiedSender == null) {
                    return null
                } else {
                    verifiedSender
                }
            }
        }
        when(action) {
            is CreateAction -> {
                if (isPossible(action)) {
                    return sender
                } else {
                    return null
                }
            }
            is DeclineAction -> {
                val entry = databaseEncapsulation.get(action.id)
                if (entry != null && entry.managedBy(sender)) {
                    return sender
                } else {
                    return null
                }
            }
            is WithdrawAction -> {
                val entry = databaseEncapsulation.get(action.id)
                if (entry != null && entry.createdBy(sender)) {
                    return sender
                } else {
                    return null
                }
            }
            is AcceptAction -> {
                val entry = databaseEncapsulation.get(action.id)
                if (entry != null && entry.managedBy(sender) && !entry.accepted) {
                    return sender
                } else {
                    return null
                }
            }
        }
    }

    private fun isPossible(action: CreateAction): Boolean {
        TODO("check if creation is allowed")
    }

    fun requiresEmailVerification(action: PostAction, verification: Email?, verificationValid: Boolean) : Boolean {
        //("check if immediate")
        return !verificationValid
    }

    fun sendEmailVerificationRequest(action: PostAction, sender: Email) {
        TODO("set action link to sender")
    }

    fun isLegalWithGivenVerification(action: PostAction, verification: Email) : Boolean {
        TODO("check if authorized")
    }

    fun executeAction(action: PostAction) {
        TODO("actually write data to database")
    }

    fun notifyCreator(action: PostAction, creators : List<Email>) {
        TODO("implement notification for creator")
    }

    fun notifyModerator(action: PostAction, moderator: List<Email>) {
        TODO("implement notification for moderator")
    }

    //todo: verify if is legal in general
    //todo: check if requring verification
    //todo: require verification if necessary and return
    //todo: check if legal for given verification
    //todo: send notification emails to all participants


    override fun process(action: PostAction, verification: Email?, verificationValid: Boolean) : PostResponse {
        val actioneer: Email? = legalInGeneral(action, verification)
        if (actioneer != null && (verification == null || actioneer.equals(verification))) {
            if (requiresEmailVerification(action, verification, verificationValid)) {
                sendEmailVerificationRequest(action, actioneer)
                return PostResponse(true, false, "verification request send to user email address")
            } else {
                if (isLegalWithGivenVerification(action, actioneer))  {
                    executeAction(action)
                    notifyModerator(action, getModerator(action))
                    notifyCreator(action, listOf(actioneer))
                    return PostResponse(true, true, "post request processed successfully")
                } else {
                    return PostResponse(false, false, "user is not authorized")
                }
            }
        } else {
            return PostResponse(false, false, "invalid post request")
        }
    }
}