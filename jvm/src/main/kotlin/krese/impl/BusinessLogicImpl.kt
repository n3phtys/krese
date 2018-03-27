package krese.impl

import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.instance
import krese.*
import krese.data.*
import krese.utility.getId
import org.joda.time.DateTime

class BusinessLogicImpl(private val kodein: Kodein) : BusinessLogic {

    private val authVerifier: AuthVerifier = kodein.instance()
    private val appConfig: ApplicationConfiguration = kodein.instance()
    private val mailService: MailService = kodein.instance()
    private val mailTemplater: MailTemplater = kodein.instance()
    private val fileSystemWrapper: FileSystemWrapper = kodein.instance()
    private val databaseEncapsulation: DatabaseEncapsulation = kodein.instance()


    override fun retrieveReservations(urk: UniqueReservableKey, from: DateTime, to: DateTime, callerEmail: Email?): GetResponse? {
        val res = fileSystemWrapper.getReservableToKey(urk)
        if (res != null) {
            return GetResponse(
                    databaseEncapsulation.retrieveBookingsForKey(urk, from, to).map { it.toOutput(res.operatorEmails.contains(callerEmail?.address)) }
            )
        } else {
            return null
        }
    }

    override fun retrieveKeys(callerEmail: Email?): GetTotalResponse {
        return GetTotalResponse(fileSystemWrapper.getKeysFromDirectory().keys.map { fileSystemWrapper.getReservableToKey(it) }.filter { it != null }.map { it!! })
    }


    fun DbBookingOutputData.managedBy(email: Email): Boolean {
        return fileSystemWrapper.getReservableToKey(this.key)?.operatorEmails?.any { it.equals(other = email.address, ignoreCase = false) } == true
    }

    fun DbBookingOutputData.createdBy(email: Email): Boolean {
        return this.email.address.equals(email.address, false)
    }


    fun getModerator(action: PostAction): List<Email>? {
        val key: UniqueReservableKey? = when (action) {
            is CreateAction -> action.key
            else -> databaseEncapsulation.get(action.getId())?.key
        }
        if (key != null) {
            val ls = fileSystemWrapper.getReservableToKey(key)?.operatorEmails?.map { Email(it) }
            if (ls != null) {
                return ls
            } else {
                return null
            }
        } else {
            return null
        }

    }

    fun legalInGeneral(action: PostAction, verifiedSender: Email?): Email? {
        val sender: Email = when (action) {
            is CreateAction -> action.email
            else -> {
                if (verifiedSender == null) {
                    return null
                } else {
                    verifiedSender
                }
            }
        }
        when (action) {
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
            else -> {
                throw IllegalArgumentException()
            }
        }
    }


    //TODO: currently overlying reservations are still possible, needs to be fixed

    private fun isPossible(action: CreateAction): Boolean {
        val res = fileSystemWrapper.getReservableToKey(action.key)
        //end date at least 1s after start date
        return action.endTime > action.startTime + 1000L &&
                //key exists
                res != null &&
                //blocks exist for key
                action.blocks.all { res.elements.allows(it) } &&
                //blocks are free for reservation
                this.databaseEncapsulation.isFree(action.key, action.blocks, action.startTime, action.endTime, res.elements) &&
                //name and email is not blank
                action.name.isNotBlank() && action.email.address.isNotBlank() && isValidEmail(action.email.address)
    }

    fun requiresEmailVerification(action: PostAction, verification: Email?, verificationValid: Boolean): Boolean {
        //("check if immediate")
        return !verificationValid
    }

    fun sendEmailVerificationRequest(action: PostAction, key: UniqueReservableKey, reservable: Reservable?, reservation: Reservation?, receiver: Email) {
        val mail: ProcessedMailTemplate = mailTemplater.construct(action.toVerifyTemplate(), key, action, true, reservable, reservation, receiver)
        mailService.sendEmail(listOf(receiver), mail.body, mail.subject)
    }

    fun isLegalWithGivenVerification(action: PostAction, verification: Email): Boolean {
        return when (action) {
            is CreateAction -> action.email.address.equals(verification.address)
            is DeclineAction -> databaseEncapsulation.get(action.id)?.key?.let { fileSystemWrapper.getReservableToKey(it)?.operatorEmails?.contains(verification.address) } == true
            is WithdrawAction -> databaseEncapsulation.get(action.id)?.email?.address?.equals(verification.address) == true
            is AcceptAction -> {
                val id = action.id
                val db = databaseEncapsulation.get(id)
                val key = db?.key
                val res = key?.let { fileSystemWrapper.getReservableToKey(it)?.operatorEmails?.contains(verification.address) }
                res == true
            }
            else -> {
                throw IllegalArgumentException()
            }
        }
    }

    fun executeAction(action: PostAction): DbBookingOutputData? {
        return when (action) {
            is CreateAction -> {
                databaseEncapsulation.createUpdateBooking(null, DbBookingInputData(
                        action.key, action.email, action.name, action.telephone, action.commentUser, "", DateTime(action.startTime), DateTime(action.endTime), DateTime.now(), false, action.blocks
                ))
            }
            is DeclineAction -> {
                databaseEncapsulation.deleteBooking(action.id)
            }
            is WithdrawAction -> databaseEncapsulation.deleteBooking(action.id)
            is AcceptAction -> databaseEncapsulation.acceptBooking(action.id)
            else -> {
                throw IllegalArgumentException()
            }
        }
        //("actually write data to database")
    }

    fun notifyCreator(action: PostAction, creator: Email, key: UniqueReservableKey, reservable: Reservable?, reservation: Reservation?) {
        mailService.sendEmail(creator, when (action) {

            is CreateAction -> mailTemplater.construct(TemplateTypes.CreatedToCreator, key, action, false, reservable, reservation, creator)
            is DeclineAction -> mailTemplater.construct(TemplateTypes.DeclinedToCreator, key, action, false, reservable, reservation, creator)
            is WithdrawAction -> mailTemplater.construct(TemplateTypes.WithdrawnToCreator, key, action, false, reservable, reservation, creator)
            is AcceptAction -> mailTemplater.construct(TemplateTypes.AcceptedToCreator, key, action, false, reservable, reservation, creator)
            else -> {
                throw IllegalArgumentException()
            }
        })

    }

    fun notifyModerator(action: PostAction, moderator: Email, key: UniqueReservableKey, reservable: Reservable?, reservation: Reservation?) {
        mailService.sendEmail(moderator, when (action) {
            is CreateAction -> mailTemplater.construct(TemplateTypes.CreatedToModerator, key, action, false, reservable, reservation, moderator)
            is DeclineAction -> mailTemplater.construct(TemplateTypes.DeclinedToModerator, key, action, false, reservable, reservation, moderator)
            is WithdrawAction -> mailTemplater.construct(TemplateTypes.WithdrawnToModerator, key, action, false, reservable, reservation, moderator)
            is AcceptAction -> mailTemplater.construct(TemplateTypes.AcceptedToCreator, key, action, false, reservable, reservation, moderator)
            else -> {
                throw IllegalArgumentException()
            }
        })

    }

    //: verify if is legal in general
    //: check if requring verification
    //: require verification if necessary and return
    //: check if legal for given verification
    //: send notification emails to all participants

    private fun getKey(action: PostAction): UniqueReservableKey? = when (action) {
        is CreateAction -> action.key
        is DeclineAction -> databaseEncapsulation.get(action.id)?.key
        is WithdrawAction -> databaseEncapsulation.get(action.id)?.key
        is AcceptAction -> databaseEncapsulation.get(action.id)?.key
        else -> {
            throw IllegalArgumentException()
        }
    }

    private fun getReservable(action: PostAction): Reservable? = getKey(action)?.let { fileSystemWrapper.getReservableToKey(it) }

    private fun getReservation(action: PostAction): Reservation? = when (action) {
        is CreateAction -> null
        is DeclineAction -> databaseEncapsulation.get(action.id)?.toOutput(true)
        is WithdrawAction -> databaseEncapsulation.get(action.id)?.toOutput(true)
        is AcceptAction -> databaseEncapsulation.get(action.id)?.toOutput(true)
        else -> {
            throw IllegalArgumentException()
        }
    }

    override fun process(action: PostAction, verification: Email?, verificationValid: Boolean): PostResponse {
        val actioneer: Email? = legalInGeneral(action, verification)
        if (actioneer != null && (verification == null || actioneer.equals(verification))) {
            val key = getKey(action)
            if (key != null) {
                val reservable = getReservable(action)
                var reservation = getReservation(action)
                if (requiresEmailVerification(action, verification, verificationValid)) {
                    sendEmailVerificationRequest(action, key, reservable, reservation, actioneer)
                    return PostResponse(true, false, "verification request send to user email address")
                } else {
                    if (isLegalWithGivenVerification(action, actioneer)) {
                        val mods = getModerator(action)
                        assert(mods != null)

                        val creator = when (action) {
                            is CreateAction -> action.email
                            else -> databaseEncapsulation.get(action.getId())?.email
                        }
                        assert(creator != null)

                        when (action) {
                            is CreateAction -> {
                                val x = executeAction(action)
                                if (x != null) {
                                    reservation = x.toOutput(true)
                                }
                            }
                            else -> {
                            }
                        }
                        mods!!.forEach { notifyModerator(action, it, key, reservable, reservation) }
                        notifyCreator(action, creator!!, key, reservable, reservation)
                        when (action) {
                            is CreateAction -> {
                            }
                            else -> {
                                executeAction(action)
                            }
                        }

                        return PostResponse(true, true, "post request processed successfully")
                    } else {
                        return PostResponse(false, false, "user is not authorized")
                    }
                }
            } else {
                return PostResponse(false, false, "UniqueReservableKey not recognized")
            }
        } else {
            return PostResponse(false, false, "invalid post request")
        }
    }
}