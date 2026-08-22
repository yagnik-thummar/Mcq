package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class LatexSymbol(val label: String, val latex: String)

enum class SymbolCategory(val title: String) {
    MATH("Math & Algebra"),
    CALCULUS("Calculus"),
    CHEMISTRY("Chemistry"),
    GREEK("Greek Symbols"),
    RELATIONS("Operators & Sets")
}

@Composable
fun MathKeyboardToolbar(
    onInsertSymbol: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedCategory by remember { mutableStateOf(SymbolCategory.MATH) }

    val mathSymbols = remember {
        listOf(
            LatexSymbol("x/y", "\\frac{a}{b}"),
            LatexSymbol("√x", "\\sqrt{x}"),
            LatexSymbol("ⁿ√x", "\\sqrt[n]{x}"),
            LatexSymbol("x²", "^{2}"),
            LatexSymbol("xⁿ", "^{n}"),
            LatexSymbol("xᵢ", "_{i}"),
            LatexSymbol("±", "\\pm "),
            LatexSymbol("×", "\\times "),
            LatexSymbol("÷", "\\div "),
            LatexSymbol("·", "\\cdot "),
            LatexSymbol("π", "\\pi "),
            LatexSymbol("θ", "\\theta "),
            LatexSymbol("$$..$$", "$$\\quad$$")
        )
    }

    val calculusSymbols = remember {
        listOf(
            LatexSymbol("∫", "\\int "),
            LatexSymbol("∫ₐᵇ", "\\int_{a}^{b} f(x)\\,dx"),
            LatexSymbol("∑", "\\sum_{i=1}^{n}"),
            LatexSymbol("lim", "\\lim_{x \\to 0}"),
            LatexSymbol("dy/dx", "\\frac{dy}{dx}"),
            LatexSymbol("∂/∂x", "\\frac{\\partial}{\\partial x}"),
            LatexSymbol("∞", "\\infty "),
            LatexSymbol("Δ", "\\Delta ")
        )
    }

    val chemSymbols = remember {
        listOf(
            LatexSymbol("→", "\\rightarrow "),
            LatexSymbol("⇌", "\\rightleftharpoons "),
            LatexSymbol("↑", "\\uparrow "),
            LatexSymbol("↓", "\\downarrow "),
            LatexSymbol("H₂O", "\\text{H}_2\\text{O}"),
            LatexSymbol("CO₂", "\\text{CO}_2"),
            LatexSymbol("H₂SO₄", "\\text{H}_2\\text{SO}_4"),
            LatexSymbol("Δ (Heat)", "\\xrightarrow{\\Delta}"),
            LatexSymbol("Fe²⁺", "\\text{Fe}^{2+}"),
            LatexSymbol("SO₄²⁻", "\\text{SO}_4^{2-}"),
            LatexSymbol("Benzene", "\\text{C}_6\\text{H}_6")
        )
    }

    val greekSymbols = remember {
        listOf(
            LatexSymbol("α", "\\alpha "),
            LatexSymbol("β", "\\beta "),
            LatexSymbol("γ", "\\gamma "),
            LatexSymbol("δ", "\\delta "),
            LatexSymbol("ε", "\\varepsilon "),
            LatexSymbol("θ", "\\theta "),
            LatexSymbol("λ", "\\lambda "),
            LatexSymbol("μ", "\\mu "),
            LatexSymbol("π", "\\pi "),
            LatexSymbol("σ", "\\sigma "),
            LatexSymbol("ω", "\\omega "),
            LatexSymbol("Ω", "\\Omega ")
        )
    }

    val relationSymbols = remember {
        listOf(
            LatexSymbol("≤", "\\le "),
            LatexSymbol("≥", "\\ge "),
            LatexSymbol("≠", "\\ne "),
            LatexSymbol("≈", "\\approx "),
            LatexSymbol("∈", "\\in "),
            LatexSymbol("⊂", "\\subset "),
            LatexSymbol("∪", "\\cup "),
            LatexSymbol("∩", "\\cap "),
            LatexSymbol("⇒", "\\implies "),
            LatexSymbol("⇔", "\\iff ")
        )
    }

    val activeSymbols = when (selectedCategory) {
        SymbolCategory.MATH -> mathSymbols
        SymbolCategory.CALCULUS -> calculusSymbols
        SymbolCategory.CHEMISTRY -> chemSymbols
        SymbolCategory.GREEK -> greekSymbols
        SymbolCategory.RELATIONS -> relationSymbols
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(vertical = 6.dp)
    ) {
        // Category Tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            SymbolCategory.entries.forEach { cat ->
                FilterChip(
                    selected = selectedCategory == cat,
                    onClick = { selectedCategory = cat },
                    label = { Text(cat.title, fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier.height(32.dp)
                )
            }
        }

        // Symbol Keys Ribbon
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            activeSymbols.forEach { sym ->
                Surface(
                    onClick = { onInsertSymbol(sym.latex) },
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 2.dp,
                    shadowElevation = 1.dp
                ) {
                    Text(
                        text = sym.label,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
                    )
                }
            }
        }
    }
}
