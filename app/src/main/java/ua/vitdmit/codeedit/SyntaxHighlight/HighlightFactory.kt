package ua.vitdmit.codeedit.SyntaxHighlight

import android.content.Context
enum class Languages {
    CPlusPlus, Java, CSharp, Python, JavaScript
}

fun detectLanguage(fileName: String):Languages {
    if (fileName.endsWith(".c") || fileName.endsWith(".cpp") || fileName.endsWith(".h") || fileName.endsWith(".hpp"))
        return Languages.CPlusPlus
    else if (fileName.endsWith(".java"))
        return Languages.Java
    else if (fileName.endsWith(".cs"))
        return Languages.CSharp
    else if (fileName.endsWith(".py"))
        return Languages.Python
    else if (fileName.endsWith(".js"))
        return Languages.JavaScript
    return Languages.CPlusPlus
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
