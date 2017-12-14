package krese

data class ResourceSet(
        val emailsOfModerators: List<String>,
        val endpointDefinition: EndpointDefImpl
)

data class EndpointDefImpl(val name: String) :EndpointDefinition {
    override fun getPathId(): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getPathRessources(): List<String> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getPathTitle(): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getPathHeader(): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getTermsAndConditions(): List<String> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getBodyHTML(): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}

data class Config(val port: Int = 8080,
                  val webDirectoryPath: String = "web",
                  val jdbcString: String = "jdbc:h2:mem:test",
                  val jdbcDriver: String = "org.h2.Driver",
                  val dbUsername: String = "test",
                  val dbPassword: String = "1234",
                  val useCSRFProtection: Boolean = false,
                  val resourceSets: List<ResourceSet> = listOf(
                          ResourceSet(
                                  emailsOfModerators = listOf("test@localhost"),
                                  endpointDefinition = EndpointDefImpl("bedrooms")
                          )
                  )
                  )