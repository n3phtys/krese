package krese

data class ResourceSet(
        val emailsOfModerators: List<String>,
        val endpointDefinition: EndpointDefinition
)

data class Config(val port: Int,
                  val webDirectoryPath: String,
                  val jdbcString: String,
                  val jdbcDriver: String,
                  val dbUsername: String,
                  val dbPassword: String,
                  val useCSRFProtection: Boolean,
                  val resourceSets: List<ResourceSet>
                  )