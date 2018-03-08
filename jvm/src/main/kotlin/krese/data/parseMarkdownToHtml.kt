package krese.data

fun parseMarkdownToHtml(markdown: String): String  {
    val flavour = org.intellij.markdown.flavours.commonmark.CommonMarkFlavourDescriptor()
    val parsedTree = org.intellij.markdown.parser.MarkdownParser(flavour).buildMarkdownTreeFromString(markdown)
    val html: String = org.intellij.markdown.html.HtmlGenerator(markdown, parsedTree, flavour).generateHtml()
    return html
}