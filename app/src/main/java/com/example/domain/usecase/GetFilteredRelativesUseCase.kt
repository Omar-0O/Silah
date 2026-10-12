package com.example.domain.usecase

import com.example.data.Relative
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetFilteredRelativesUseCase @Inject constructor() {

    operator fun invoke(
        relatives: Flow<List<Relative>>,
        query: String,
        category: String
    ): Flow<List<Relative>> {
        return relatives.map { list ->
            filterAndSort(list, query, category)
        }
    }

    operator fun invoke(
        relatives: Flow<List<Relative>>,
        query: Flow<String>,
        category: Flow<String>
    ): Flow<List<Relative>> {
        return combine(relatives, query, category) { list, q, cat ->
            filterAndSort(list, q, cat)
        }
    }

    fun filterAndSort(
        relativesList: List<Relative>,
        query: String,
        category: String
    ): List<Relative> {
        return relativesList.filter { relative ->
            val cleanQuery = query.trim()
            val matchesSearch = if (cleanQuery.isBlank()) true
            else relative.name.contains(cleanQuery, ignoreCase = true) || relative.phone.contains(cleanQuery)

            val matchesCategory = when (category) {
                "الكل" -> true
                "والدان" -> relative.relationshipDegree in listOf("والدان", "أب", "أم", "والد", "والدة", "جد", "جدة", "Father", "Mother", "Parents")
                "أشقاء" -> relative.relationshipDegree in listOf("أشقاء", "أخ", "أخت", "شقيق", "شقيقة", "Brother", "Sister", "Siblings")
                "أعمام/أخوال" -> relative.relationshipDegree in listOf("أعمام/أخوال", "عم", "عمة", "خال", "خالة", "Uncle", "Aunt", "Uncles/Aunts")
                "أقارب آخرون" -> relative.relationshipDegree == "أقارب آخرون" || (
                    relative.relationshipDegree !in listOf("والدان", "أب", "أم", "والد", "والدة", "جد", "جدة", "Father", "Mother", "Parents") &&
                    relative.relationshipDegree !in listOf("أشقاء", "أخ", "أخت", "شقيق", "شقيقة", "Brother", "Sister", "Siblings") &&
                    relative.relationshipDegree !in listOf("أعمام/أخوال", "عم", "عمة", "خال", "خالة", "Uncle", "Aunt", "Uncles/Aunts")
                )
                else -> relative.relationshipDegree.contains(category) || category.contains(relative.relationshipDegree)
            }
            matchesSearch && matchesCategory
        }.sortedWith(
            compareBy<Relative> { relative ->
                if (relative.lastContactDate == 0L) 0L
                else relative.lastContactDate + (relative.contactIntervalDays * 86_400_000L)
            }.thenBy { relative ->
                when {
                    relative.relationshipDegree in listOf("والدان", "أب", "أم", "والد", "والدة", "جد", "جدة") -> 1
                    relative.relationshipDegree in listOf("أشقاء", "أخ", "أخت", "شقيق", "شقيقة") -> 2
                    relative.relationshipDegree in listOf("أعمام/أخوال", "عم", "عمة", "خال", "خالة") -> 3
                    else -> 4
                }
            }.thenBy { it.name }
        )
    }
}
