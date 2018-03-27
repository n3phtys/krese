package krese.data


//first line of file is
//#<Subject>

//other lines get parsed as markdown


//following constants can be added and will be replaced on the fly:
enum class TemplateContants {
    POSITIVE_ACTION_LINK,
    NEGATIVE_ACTION_LINK,
    NAME_OF_CREATOR,
    EMAIL_OF_CREATOR,
    TELEPHONE_OF_CREATOR,
    MODERATOR_MARKDOWN_LINKS,
    TITLE_OF_RESERVABLE,
    LOGIN_LINK,
    LIST_OF_RESERVED_ELEMENTS,
    RESERVATION_COMMENT,
    RESERVATION_FROM_STRING,
    RESERVATION_TO_STRING,
    LINK_DURATION,
    FULL_HOST_ROOT,
    CREATION_DATE,
    RESERVATION_DATE,
    //TODO: add other required constants in those mails
}

data class ProcessedMailTemplate(val subject: String, val body: String) {

}

data class MailTemplate(val subject: String, val body: String) {

}

enum class TemplateTypes {
    AcceptedToCreator,
    AcceptedToModerator,
    CreatedToCreator,
    CreatedToModerator,
    WithdrawnToCreator,
    WithdrawnToModerator,
    DeclinedToCreator,
    DeclinedToModerator,
    DeclineRequestVerificationToModerator,
    AcceptRequestVerificationToModerator,
    WithdrawRequestVerificationToCreator,
    CreateRequestVerificationToCreator,
    LoginVerification;


    fun fileName(): String = "${this.name}.md"

    fun getResourceTemplate(reader: MailFileReader): MailTemplate {
        val folder = "compiledmailtemplates"
        val resourcePath = "/$folder/${this.fileName()}"
        val filecontent = reader.readResourceOrFail(resourcePath)
        return filecontent.buildMailTemplate()
    }

    fun getGlobalTemplate(reader: MailFileReader, config: MailFileConfigGlobal): MailTemplate? {
        val p = config.globalMailDir()
        if (p != null) {
            val s = reader.getTemplatesFromDir(p).get(this)
            if (s != null) {
                return reader.parseTemplate(s)
            }
        }
        return null
    }


    fun getKeySpecificTemplate(key: UniqueReservableKey, reader: MailFileReader, config: MailFileConfigSpecific): MailTemplate? {
        val p = config.specificMailDir(key)
        if (p != null) {
            val s = reader.getTemplatesFromDir(p).get(this)
            if (s != null) {
                return reader.parseTemplate(s)
            }
        }
        return null
    }


    fun getMostSpecificTemplate(key: UniqueReservableKey?, reader: MailFileReader, configGlobal: MailFileConfigGlobal, configSpecial: MailFileConfigSpecific): MailTemplate {
        val a = key?.let { this.getKeySpecificTemplate(it, reader, configSpecial) }
        if (a != null) {
            return a
        } else {
            val b = this.getGlobalTemplate(reader, configGlobal)
            if (b != null) {
                return b
            } else {
                return this.getResourceTemplate(reader)
            }
        }
    }


}

fun PostAction.toVerifyTemplate(): TemplateTypes {
    return when (this) {
        is CreateAction -> TemplateTypes.CreateRequestVerificationToCreator
        is DeclineAction -> TemplateTypes.DeclineRequestVerificationToModerator
        is AcceptAction -> TemplateTypes.AcceptRequestVerificationToModerator
        is WithdrawAction -> TemplateTypes.WithdrawRequestVerificationToCreator
        else -> throw IllegalArgumentException()
    }
}


fun String.buildMailTemplate(): MailTemplate {
    val filecontent = this.lines()
    val firstLine = filecontent.first()
    val body = filecontent.drop(1)
    return MailTemplate(firstLine.dropWhile { it == '#' }, body.joinToString("\n"))
}

interface MailFileReader {
    fun getTemplatesFromDir(dir: String): Map<TemplateTypes, String>
    fun parseTemplate(path: String): MailTemplate?
    fun readResourceOrFail(filePath: String): String
}

interface MailFileConfigGlobal {
    fun globalMailDir(): String?
}

interface MailFileConfigSpecific {
    fun specificMailDir(key: UniqueReservableKey): String?
}

//there is a resource based default set of mails, a global directory with global mail templates, and it's also possible to declare templates per reservable