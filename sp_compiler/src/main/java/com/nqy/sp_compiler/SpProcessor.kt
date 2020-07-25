package com.nqy.sp_compiler

import com.nqy.nqy_anno.Column
import com.nqy.nqy_anno.Entity
import com.squareup.kotlinpoet.*
import java.io.File
import java.lang.StringBuilder
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement

@SupportedSourceVersion(value = SourceVersion.RELEASE_8)
@SupportedAnnotationTypes(value = ["com.nqy.nqy_anno.Entity", "com.nqy.nqy_anno.Column"])
class SpProcessor : AbstractProcessor() {
    private var messager: Messager? = null

    /*override fun init(processingEnv: ProcessingEnvironment?) {
        super.init(processingEnv)
        messager = processingEnv?.messager
    }*/

    /*private fun print(text: String) {
        if (!LOG) return
        messager?.printMessage(Diagnostic.Kind.NOTE, text)
        println("SpProcessor-------$text")
    }*/

    override fun process(
        annotations: MutableSet<out TypeElement>,
        roundEnv: RoundEnvironment
    ): Boolean {
        val elementUtils = processingEnv.elementUtils
        /*print("process")
        annotations.forEach {
            print(it::class.java.name)
        }*/

        roundEnv.getElementsAnnotatedWith(Entity::class.java)
            .forEach { element ->
                if (element.kind.isClass) {
                    val entity = element.getAnnotation(Entity::class.java)
                    val allMembers = element.enclosedElements

                    val className = element.simpleName.toString()
                    val packageName = elementUtils.getPackageOf(element).toString()
                    generateClass(className, packageName, allMembers, entity)
                }
            }

        return true
    }

    private fun appendStr(s:String): String {
        return "\"\"\"$s\"\"\""
    }

    private fun generateClass(
        className: String,
        packageName: String,
        allMembers: List<Element>,
        entity: Entity
    ) {
        val fileName = if (entity.name.isNotEmpty()) {
            entity.name
        } else {
            "${className}${SUFFIX}"
        }
        val typeSpec = TypeSpec.objectBuilder(fileName)
        typeSpec.addProperty(
            PropertySpec.builder(
                SP,
                Class.forName("android.content.SharedPreferences"),
                KModifier.PRIVATE
            ).initializer(entity.Sp).build()
        )

        val clearCode = StringBuilder()
        allMembers.forEach { member ->
            if (member.kind.isField && !member.modifiers.contains(Modifier.STATIC)) {
                val spColumnInfo = member.getAnnotation(Column::class.java)
                val defInitValue = spColumnInfo?.defValue ?: ""
                val clear = spColumnInfo?.clear ?: true

                val name = member.asType().asTypeName()
                val valueName = "_${member.simpleName}"
                val propertyName = member.simpleName.toString()
//                print("propertyName----$propertyName")
                val typeName = if (name.toString().contains("String")) ClassName("kotlin","String") else name

                typeSpec.addProperty(
                    PropertySpec.builder(valueName,typeName.copy(true))
                        .initializer("null")
                        .addModifiers(KModifier.PRIVATE)
                        .mutable(true)
                        .build()
                )

                val paramType = typeName.toString()
                val defValue = when{
                    paramType.contains("String")->if (defInitValue.isNotEmpty()) defInitValue else ""
                    paramType.contains("Boolean")->"${if (defInitValue.isNotEmpty()) defInitValue.toBoolean() else false}"
                    paramType.contains("Int")->"${if (defInitValue.isNotEmpty()) defInitValue.toInt() else 0}"
                    paramType.contains("Long")->"${if (defInitValue.isNotEmpty()) defInitValue.toLong() else 0}"
                    paramType.contains("String")->{
                        val res =
                            (if (spColumnInfo.defValue.isNotEmpty()) spColumnInfo.defValue.toFloat() else 0F).toString()
                        if (!res.contains("F")) {
                            "${res}F"
                        } else {
                            res
                        }
                    }
                    else -> "not support"
                }

                val getName = when {
                    paramType.contains("String") -> "getString(\"$propertyName\", \"\"\"$defValue\"\"\")"
                    paramType.contains("Boolean") -> "getBoolean(\"$propertyName\", $defValue)"
                    paramType.contains("Int") -> "getInt(\"$propertyName\", $defValue)"
                    paramType.contains("Long") -> "getLong(\"$propertyName\", $defValue)"
                    paramType.contains("Float") -> "getFloat(\"$propertyName\", $defValue)"

                    else -> "unsupport"
                }

                val valueStringCode = "value"
                val setName = when {
                    paramType.contains("String") -> "putString(\"$propertyName\", $valueStringCode)"
                    paramType.contains("Boolean") -> "putBoolean(\"$propertyName\", value)"
                    paramType.contains("Int") -> "putInt(\"$propertyName\", value)"
                    paramType.contains("Long") -> "putLong(\"$propertyName\", value)"
                    paramType.contains("Float") -> "putFloat(\"$propertyName\", value)"
                    else -> "unsupport"
                }

                typeSpec.addProperty(
                    PropertySpec.builder(propertyName, typeName)
                        .mutable(true)
                        .getter(
                            FunSpec.getterBuilder()
                                .addCode(
                                    if (paramType.contains("String")) {
                                        """
                                    |if ($valueName == null) {
                                    |   val $SP_DEFAULT_VALUE = $SP.$getName ?:""
                                    |   if($SP_DEFAULT_VALUE == ${appendStr(defValue)}){
                                    |     $valueName = $SP_DEFAULT_VALUE     
                                    |   }else{
                                    |     $valueName = $SP_DEFAULT_VALUE
                                    |   } 
                                    |}
                                    |return $valueName!!
                                    |""".trimMargin()
                                    } else {
                                        """
                                    |if ($valueName == null) {
                                    |   $valueName = $SP.$getName
                                    |}
                                    |return $valueName!!
                                    |""".trimMargin()
                                    }
                                )
                                .build()
                        )
                        .setter(
                            FunSpec.setterBuilder().addParameter("value", typeName).addCode(
                                """
                            |if ($valueName == value) return
                            |$valueName = value
                            |$SP.edit().$setName.apply()
                            |""".trimMargin()
                            ).build()
                        )
                        .build()
                )

                if (clear) {
                    if (typeName.toString().contains("String")) {
                        clearCode.append("$propertyName = \"\"\"$defValue\"\"\" \n")
                    } else {
                        clearCode.append("$propertyName = $defValue \n")
                    }
                }


            }
        }

        typeSpec.addFunction(
            FunSpec.builder("clear")
                .addCode(
                    """
              |$clearCode
            """.trimMargin()
                )
                .build()
        )
        val file = FileSpec.builder(packageName, fileName).addType(typeSpec.build()).build()

        val kaptKotlinGeneratedDir = processingEnv.options[KAPT_KOTLIN_GENERATED_OPTION_NAME]
        file.writeTo(File(kaptKotlinGeneratedDir, "nqy"))

    }


    companion object {
        const val KAPT_KOTLIN_GENERATED_OPTION_NAME = "kapt.kotlin.generated"
        private const val SUFFIX = "SP"
        private const val LOG = false
        private const val SP = "sp"
        private const val SP_DEFAULT_VALUE = "spDefaultValue"

    }


}
