package com.example.ui.dialogs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.PrimaryGreen
import com.example.ui.theme.SoftGold

sealed class MilestoneType(
    val milestoneId: String,
    val count: Int,
    val isSupportPrompt: Boolean
) {
    object Milestone5 : MilestoneType("milestone_5", 5, isSupportPrompt = false)
    object Milestone10 : MilestoneType("milestone_10", 10, isSupportPrompt = true)
    object Milestone25 : MilestoneType("milestone_25", 25, isSupportPrompt = true)
    object Milestone50 : MilestoneType("milestone_50", 50, isSupportPrompt = true)
    object Milestone100 : MilestoneType("milestone_100", 100, isSupportPrompt = true)
}

@Composable
fun MilestoneDialog(
    milestone: MilestoneType,
    lang: String,
    onSupportClick: () -> Unit,
    onNotNowClick: () -> Unit,
    onDismiss: () -> Unit
) {
    val layoutDirection = if (lang == "en") LayoutDirection.Ltr else LayoutDirection.Rtl

    val title = when (milestone) {
        MilestoneType.Milestone5 -> if (lang == "en") "🎉 You're Making a Difference!" else "🎉 بدأت تعمل فرق!"
        MilestoneType.Milestone10 -> if (lang == "en") "🤍 10 Relatives Contacted!" else "🤍 وصلت رحم 10 أشخاص!"
        MilestoneType.Milestone25 -> if (lang == "en") "🌱 Your Impact is Growing!" else "🌱 أثرك بيكبر!"
        MilestoneType.Milestone50 -> if (lang == "en") "🤍 50 Relatives Contacted!" else "🤍 50 شخص وصلت لهم!"
        MilestoneType.Milestone100 -> if (lang == "en") "🥹 100 Relatives Contacted!" else "🥹 100 شخص وصلت لهم!"
    }

    val description = when (milestone) {
        MilestoneType.Milestone5 -> if (lang == "en") {
            "Keep preserving your ties with family 🤍"
        } else {
            "كمل في الحفاظ على صلتك بأهلك 🤍"
        }
        MilestoneType.Milestone10 -> if (lang == "en") {
            "It's wonderful that Sila helped you remember those you love.\n\nIf you'd like to help me keep developing Sila, you can support me."
        } else {
            "جميل إن صِلَة ساعدتك تفتكر الناس اللي بتحبهم.\n\nلو حابب تساعدني أكمل تطوير صِلَة، تقدر تدعمني."
        }
        MilestoneType.Milestone25 -> if (lang == "en") {
            "You've connected with 25 relatives.\n\nThank you for being part of Sila."
        } else {
            "أنت تواصلت مع 25 شخص من أهلك.\n\nشكرًا إنك جزء من صِلَة."
        }
        MilestoneType.Milestone50 -> if (lang == "en") {
            "You're not just using Sila...\nYou're preserving vital relationships in your life."
        } else {
            "أنت مش بس بتستخدم صِلَة...\nأنت بتحافظ على علاقات مهمة في حياتك."
        }
        MilestoneType.Milestone100 -> if (lang == "en") {
            "Thank you for being part of Sila."
        } else {
            "شكرًا إنك جزء من صِلَة."
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = title,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryGreen,
                        textAlign = TextAlign.Center
                    )

                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                        ),
                        border = BorderStroke(1.dp, PrimaryGreen.copy(alpha = 0.2f))
                    ) {
                        Text(
                            text = description,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 22.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(16.dp)
                        )
                    }

                    if (milestone.isSupportPrompt) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = onSupportClick,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = SoftGold,
                                    contentColor = Color(0xFF141816)
                                )
                            ) {
                                Text(
                                    text = if (lang == "en") "Support Sila 🤍" else "ادعم صِلَة 🤍",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            TextButton(
                                onClick = onNotNowClick,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = if (lang == "en") "Not Now" else "ليس الآن",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    } else {
                        Button(
                            onClick = onDismiss,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                        ) {
                            Text(
                                text = if (lang == "en") "Continue" else "متابعة",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
