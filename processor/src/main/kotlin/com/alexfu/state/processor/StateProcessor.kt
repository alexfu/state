package com.alexfu.state.processor

import com.alexfu.state.State
import com.google.devtools.ksp.containingFile
import com.google.devtools.ksp.getDeclaredFunctions
import com.google.devtools.ksp.getDeclaredProperties
import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.*
import com.google.devtools.ksp.validate
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toTypeName
import com.squareup.kotlinpoet.ksp.writeTo
import java.util.*

class StateProcessor(private val codegen: CodeGenerator, private val logger: KSPLogger) : SymbolProcessor {
    override fun process(resolver: Resolver): List<KSAnnotated> {
        val unresolvedSymbols = mutableListOf<KSAnnotated>()
        val annotationName = State::class.qualifiedName
        if (!annotationName.isNullOrBlank()) {
            val allSymbols = resolver.getSymbolsWithAnnotation(annotationName).toList()

            logger.info("Found ${allSymbols.size} symbols with $annotationName.")

            val validatedSymbols = allSymbols
                .filter { symbol ->
                    val isValid = validate(symbol)
                    if (!isValid) {
                        unresolvedSymbols.add(symbol)
                    }
                    isValid
                }
                .toList()

            validatedSymbols.forEach { symbol ->
                logger.info("Processing $symbol.")
                symbol.accept(StateVisitor(codegen = codegen, originatingFile = symbol.containingFile), Unit)
            }
        }
        return unresolvedSymbols
    }

    private fun validate(symbol: KSAnnotated): Boolean {
        if (!symbol.validate()) {
            return false
        }

        if (symbol !is KSClassDeclaration) {
            return false
        }

        // Ensure this class has a built-in copy function & not one that is
        // declared manually.
        val functions = symbol.getAllFunctions() - symbol.getDeclaredFunctions().toSet()
        val hasCopyFunction = functions.firstOrNull { it.simpleName.asString() == "copy" } != null
        if (!hasCopyFunction) {
            logger.warn("Skipping processing for ${symbol.simpleName.asString()}. @State annotation is only valid on data classes!")
        }
        return hasCopyFunction
    }
}

private class StateVisitor(private val codegen: CodeGenerator, private val originatingFile: KSFile?) : KSVisitorVoid() {
    override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: Unit) {
        val className = classDeclaration.toClassName()
        val declaredProps = classDeclaration.getDeclaredProperties().toList()
        val file = StateActionsCodeGenerator(className, declaredProps).generate()
        file.writeTo(
            codeGenerator = codegen,
            dependencies = Dependencies(
                aggregating = false,
                sources = originatingFile?.let { arrayOf(it) }.orEmpty()
            )
        )
    }
}

private class StateActionsCodeGenerator(private val className: ClassName, private val props: List<KSPropertyDeclaration>) {
    fun generate(): FileSpec {
        val fileName = "${className.simpleName}Actions"
        return FileSpec.builder(ClassName(className.packageName, fileName))
            .addType(buildClass(fileName))
            .build()
    }

    private fun buildClass(name: String): TypeSpec {
        return TypeSpec.objectBuilder(name)
            .apply {
                val returnType = ClassName("com.alexfu.state", "Action").parameterizedBy(className)
                props.forEach { prop ->
                    addFunction(buildActionFunction(prop = prop, returnType = returnType))
                }
            }
            .build()
    }

    private fun buildActionFunction(prop: KSPropertyDeclaration, returnType: TypeName): FunSpec {
        val propertyName = prop.simpleName.asString()
        return FunSpec.builder("set${capitalize(propertyName)}")
            .addParameter(propertyName, prop.type.toTypeName())
            .addStatement("return { it.copy($propertyName = $propertyName) }")
            .returns(returnType)
            .build()
    }

    private fun capitalize(str: String): String {
        return str.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
    }
}