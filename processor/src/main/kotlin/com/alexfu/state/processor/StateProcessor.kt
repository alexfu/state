package com.alexfu.state.processor

import com.alexfu.state.State
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSVisitorVoid
import com.google.devtools.ksp.validate

class StateProcessor : SymbolProcessor {
    override fun process(resolver: Resolver): List<KSAnnotated> {
        val unresolvedSymbols = mutableListOf<KSAnnotated>()
        val annotationName = State::class.qualifiedName
        if (!annotationName.isNullOrBlank()) {
            val allSymbols = resolver.getSymbolsWithAnnotation(annotationName).toList()

            val validatedSymbols = allSymbols
                .filter { symbol ->
                    val isValid = symbol.validate()
                    if (!isValid) {
                        unresolvedSymbols.add(symbol)
                    }
                    isValid
                }
                .toList()

            validatedSymbols.forEach { symbol ->
                symbol.accept(StateVisitor(), Unit)
            }
        }
        return unresolvedSymbols
    }
}

private class StateVisitor : KSVisitorVoid() {
    override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: Unit) {
        
    }
}