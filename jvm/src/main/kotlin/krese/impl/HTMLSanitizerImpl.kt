package krese.impl

import com.github.salomonbrys.kodein.Kodein
import krese.HTMLSanitizer
import org.owasp.html.HtmlPolicyBuilder



class HTMLSanitizerImpl(private val kodein: Kodein) : HTMLSanitizer {
    override fun sanitize(html: String): String {
        val policy = HtmlPolicyBuilder()
                .allowElements("div")
                .allowElements("span")
                .allowElements("b")
                .allowElements("u")
                .allowElements("ol")
                .allowElements("ul")
                .allowElements("li")
                .allowElements("i")
                .requireRelNofollowOnLinks()
                .toFactory()
        return policy.sanitize(html)
    }
}