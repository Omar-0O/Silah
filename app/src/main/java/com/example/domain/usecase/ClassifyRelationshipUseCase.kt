package com.example.domain.usecase

import javax.inject.Inject

class ClassifyRelationshipUseCase @Inject constructor() {

    operator fun invoke(name: String): String {
        val lower = name.lowercase()
        return when {
            lower.contains("ماما") || lower.contains("امي") || lower.contains("أمي") || lower.contains("والدتي") ||
            lower.contains("بابا") || lower.contains("ابي") || lower.contains("أبي") || lower.contains("والدي") -> "والدان"

            lower.contains("اخي") || lower.contains("أخي") || lower.contains("اختي") || lower.contains("أختي") ||
            lower.contains("شقيقي") || lower.contains("شقيقتي") -> "أشقاء"

            lower.contains("عمي") || lower.contains("عمتي") || lower.contains("خالي") || lower.contains("خالتي") ||
            lower.contains("عم") || lower.contains("عمة") || lower.contains("خال") || lower.contains("خالة") -> "أعمام/أخوال"

            else -> "أقارب آخرون"
        }
    }
}
