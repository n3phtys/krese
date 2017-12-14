package krese


interface EndpointDefinition {
    fun getPathId() : String
    fun getPathRessources() : List<String>
    fun getPathTitle() : String
    fun getPathHeader() : String
    fun getTermsAndConditions() : List<String>
    fun getBodyHTML() : String //will be sanitized of any script and iframe
}