package com.example.ui.components

import android.annotation.SuppressLint
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView

/**
 * LatexRenderer renders mathematical formulas ($$...$$) and chemical equations
 * using a high-performance MathJax HTML WebView or native Compose typography.
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun LatexView(
    latexText: String,
    modifier: Modifier = Modifier,
    fontSizeSp: Int = 15,
    isDark: Boolean = isSystemInDarkTheme()
) {
    val textColor = MaterialTheme.colorScheme.onSurface
    val bgColor = MaterialTheme.colorScheme.surface

    val htmlContent = remember(latexText, isDark, fontSizeSp) {
        val hexTextColor = String.format("#%06X", 0xFFFFFF and textColor.toArgb())
        val hexBgColor = String.format("#%06X", 0xFFFFFF and bgColor.toArgb())
        
        // Convert plain newlines to <br/> while preserving LaTeX blocks
        val escapedText = latexText
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "<br/>")

        """
        <!DOCTYPE html>
        <html>
        <head>
            <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
            <script>
                MathJax = {
                    tex: {
                        inlineMath: [['$', '$'], ['\\(', '\\)']],
                        displayMath: [['$$', '$$'], ['\\[', '\\]']],
                        packages: {'[+]': ['mhchem', 'ams', 'color']}
                    },
                    loader: {load: ['[tex]/mhchem', '[tex]/ams', '[tex]/color']},
                    chtml: {
                        scale: 1.05
                    }
                };
            </script>
            <script id="MathJax-script" async src="https://cdn.jsdelivr.net/npm/mathjax@3/es5/tex-mml-chtml.js"></script>
            <style>
                body {
                    margin: 0;
                    padding: 4px;
                    background-color: $hexBgColor;
                    color: $hexTextColor;
                    font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
                    font-size: ${fontSizeSp}px;
                    line-height: 1.5;
                    word-wrap: break-word;
                }
                .mjx-chtml {
                    margin: 4px 0 !important;
                }
            </style>
        </head>
        <body>
            <div id="content">$latexText</div>
        </body>
        </html>
        """.trimIndent()
    }

    Box(modifier = modifier) {
        AndroidView(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            factory = { context ->
                WebView(context).apply {
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.loadWithOverviewMode = true
                    settings.useWideViewPort = false
                    setBackgroundColor(bgColor.toArgb())
                    webViewClient = object : WebViewClient() {
                        override fun onPageFinished(view: WebView?, url: String?) {
                            super.onPageFinished(view, url)
                            view?.setBackgroundColor(bgColor.toArgb())
                        }
                    }
                    loadDataWithBaseURL("https://cdn.jsdelivr.net/", htmlContent, "text/html", "UTF-8", null)
                }
            },
            update = { webView ->
                webView.setBackgroundColor(bgColor.toArgb())
                webView.loadDataWithBaseURL("https://cdn.jsdelivr.net/", htmlContent, "text/html", "UTF-8", null)
            }
        )
    }
}

/**
 * Lightweight native Compose LaTeX and STEM text parser.
 * Renders formulas with clean Unicode STEM symbols, subscripts, superscripts, and fractions.
 */
@Composable
fun FormattedLatexText(
    text: String,
    modifier: Modifier = Modifier,
    fontSize: androidx.compose.ui.unit.TextUnit = 14.sp,
    fontWeight: FontWeight = FontWeight.Normal,
    color: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface,
    lineHeight: androidx.compose.ui.unit.TextUnit = 20.sp
) {
    val annotated = remember(text) {
        parseLatexToAnnotatedString(text)
    }

    Text(
        text = annotated,
        modifier = modifier,
        fontSize = fontSize,
        fontWeight = fontWeight,
        color = color,
        lineHeight = lineHeight
    )
}

fun parseLatexToAnnotatedString(raw: String): AnnotatedString {
    var cleaned = raw
        .replace("$$\\int", "∫ ")
        .replace("\\int", "∫")
        .replace("\\frac", "")
        .replace("\\sqrt", "√")
        .replace("\\lim", "lim")
        .replace("\\sum", "∑")
        .replace("\\infty", "∞")
        .replace("\\rightarrow", " → ")
        .replace("\\xrightarrow", " ──→ ")
        .replace("\\rightleftharpoons", " ⇌ ")
        .replace("\\Delta", "Δ")
        .replace("\\alpha", "α")
        .replace("\\beta", "β")
        .replace("\\gamma", "γ")
        .replace("\\theta", "θ")
        .replace("\\pi", "π")
        .replace("\\lambda", "λ")
        .replace("\\mu", "μ")
        .replace("\\sigma", "σ")
        .replace("\\pm", "±")
        .replace("\\times", "×")
        .replace("\\div", "÷")
        .replace("\\le", "≤")
        .replace("\\ge", "≥")
        .replace("\\ne", "≠")
        .replace("\\approx", "≈")
        .replace("\\cdot", "·")
        .replace("\\quad", "   ")
        .replace("\\text", "")
        .replace("\\ce", "")
        .replace("\\left(", "(")
        .replace("\\right)", ")")
        .replace("\\left[", "[")
        .replace("\\right]", "]")
        .replace("\\left\\{", "{")
        .replace("\\right\\}", "}")
        .replace("$$", "")
        .replace("$", "")
        .replace("{", "")
        .replace("}", "")
        .replace("\\", "")

    return buildAnnotatedString {
        var i = 0
        while (i < cleaned.length) {
            when {
                cleaned[i] == '^' && i + 1 < cleaned.length -> {
                    // Superscript
                    val nextChar = cleaned[i + 1]
                    val superScriptChar = toSuperscript(nextChar)
                    if (superScriptChar != null) {
                        append(superScriptChar)
                        i += 2
                    } else {
                        pushStyle(SpanStyle(baselineShift = BaselineShift.Superscript, fontSize = 10.sp))
                        append(nextChar)
                        pop()
                        i += 2
                    }
                }
                cleaned[i] == '_' && i + 1 < cleaned.length -> {
                    // Subscript
                    val nextChar = cleaned[i + 1]
                    val subScriptChar = toSubscript(nextChar)
                    if (subScriptChar != null) {
                        append(subScriptChar)
                        i += 2
                    } else {
                        pushStyle(SpanStyle(baselineShift = BaselineShift.Subscript, fontSize = 10.sp))
                        append(nextChar)
                        pop()
                        i += 2
                    }
                }
                else -> {
                    append(cleaned[i])
                    i++
                }
            }
        }
    }
}

private fun toSuperscript(c: Char): Char? {
    return when (c) {
        '0' -> '⁰'
        '1' -> '¹'
        '2' -> '²'
        '3' -> '³'
        '4' -> '⁴'
        '5' -> '⁵'
        '6' -> '⁶'
        '7' -> '⁷'
        '8' -> '⁸'
        '9' -> '⁹'
        '+' -> '⁺'
        '-' -> '⁻'
        '=' -> '⁼'
        '(' -> '⁽'
        ')' -> '⁾'
        'n' -> 'ⁿ'
        'x' -> 'ˣ'
        else -> null
    }
}

private fun toSubscript(c: Char): Char? {
    return when (c) {
        '0' -> '₀'
        '1' -> '₁'
        '2' -> '₂'
        '3' -> '₃'
        '4' -> '₄'
        '5' -> '₅'
        '6' -> '₆'
        '7' -> '₇'
        '8' -> '₈'
        '9' -> '₉'
        '+' -> '₊'
        '-' -> '₋'
        '=' -> '₌'
        '(' -> '₍'
        ')' -> '₎'
        'a' -> 'ₐ'
        'e' -> 'ₑ'
        'i' -> 'ᵢ'
        'j' -> 'ⱼ'
        'k' -> 'ₖ'
        'm' -> 'ₘ'
        'n' -> 'ₙ'
        'p' -> 'ₚ'
        'r' -> 'ᵣ'
        's' -> 'ₛ'
        't' -> 'ₜ'
        'x' -> 'ₓ'
        else -> null
    }
}
