package ua.vitdmit.codeedit.SyntaxHighlight

import android.content.Context
enum class Languages {
    CPlusPlus, Java, CSharp, Python, JavaScript
}
class HighlightFactory(val context: Context) {
    var classes = HashMap<Languages, Highlighter>()
    fun get(language: Languages): Highlighter {
        if (!classes.containsKey(language)) {
            when (language) {
                Languages.CPlusPlus -> {
                    classes[language] = CPlusPlusHighlighter(context)
                }
                Languages.Java -> {
                    classes[language] = JavaHighlighter(context)
                }
                Languages.CSharp -> {
                    classes[language] = CSharpHighlighter(context)
                }
                Languages.Python -> {
                    classes[language] = PythonHighlighter(context)
                }
                Languages.JavaScript -> {
                    classes[language] = JavaScriptHighlighter(context)
                }
            }
        }
        return classes[language]!!
    }
}
