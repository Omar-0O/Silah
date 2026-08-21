package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.SoftGold
import com.example.viewmodel.RelativeViewModel

data class KinshipChallenge(
    val id: Int,
    val title: String,
    val description: String,
    val rewardPoints: Int,
    val isCompleted: Boolean,
    val iconRes: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChallengesTabScreen(
    viewModel: RelativeViewModel,
    modifier: Modifier = Modifier
) {
    val relatives by viewModel.relatives.collectAsState()
    val logs by viewModel.logs.collectAsState()

    val totalContacts = logs.size
    val totalRelatives = relatives.size

    val challenges = listOf(
        KinshipChallenge(
            1,
            "سفير الود والبر",
            "قم بالتواصل مع 3 من أقاربك خلال هذا الأسبوع",
            100,
            totalContacts >= 3,
            "🏆"
        ),
        KinshipChallenge(
            2,
            "وسام صلة الوالدين",
            "قم بالاطمئنان على الوالدين أو شيوخ العائلة هذا اليوم",
            150,
            relatives.any { it.relationshipDegree == "والدان" && (System.currentTimeMillis() - it.lastContactDate) <= 86400000L * 3 },
            "👑"
        ),
        KinshipChallenge(
            3,
            "تحدي الجمعة المباركة",
            "إرسال رسائل تهنئة بالجمعة لأكبر عدد من الأقارب",
            80,
            logs.count { it.type.contains("رسالة") } >= 2,
            "✨"
        ),
        KinshipChallenge(
            4,
            "حارس الأرحام",
            "إضافة جميع أقاربك وشجرتك العائلية في التطبيق (أكثر من 5 أقارب)",
            200,
            totalRelatives >= 5,
            "🌳"
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "تحديات وأوسِمَةُ صِلَةِ 🏆",
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 20.sp
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            // Header Stats Card
            item {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(10.dp, RoundedCornerShape(24.dp), ambientColor = SoftGold.copy(alpha = 0.3f))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color(0xFF2E513A), Color(0xFF1E5A35))
                            ),
                            shape = RoundedCornerShape(24.dp)
                        )
                ) {
                    Row(
                        modifier = Modifier
                            .padding(20.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Outlined.LocalFireDepartment, contentDescription = null, tint = SoftGold, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("سِلْسِلَةُ الوَصْلِ المُمَيَّزَةِ", fontSize = 12.sp, color = SoftGold, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("$totalContacts إجمالي الاتصالات", fontSize = 22.sp, fontWeight = FontWeight.Black, color = Color.White)
                            Text("تبارك الرحمن! كل تواصل يزرع محبة وأجراً 🤍", fontSize = 11.sp, color = Color(0xFFBCCEC3))
                        }

                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(Color(0x22FFFFFF))
                        ) {
                            Text("🌟", fontSize = 32.sp)
                        }
                    }
                }
            }

            item {
                Text(
                    "الأوسمة والأنشطة التحفيزية",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            // Challenge Cards
            items(challenges) { challenge ->
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (challenge.isCompleted) Color(0xFFF0FDF4) else MaterialTheme.colorScheme.surface
                    ),
                    border = BorderStroke(
                        1.dp,
                        if (challenge.isCompleted) Color(0xFF86EFAC) else SoftGold.copy(alpha = 0.2f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(4.dp, RoundedCornerShape(20.dp), ambientColor = Color.Black.copy(alpha = 0.03f))
                ) {
                    Row(
                        modifier = Modifier.padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(challenge.iconRes, fontSize = 32.sp)

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                challenge.title,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                challenge.description,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 16.sp
                            )
                        }

                        if (challenge.isCompleted) {
                            Badge(
                                containerColor = Color(0xFFDCFCE7),
                                contentColor = Color(0xFF15803D)
                            ) {
                                Text("مكتمل ✨", fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(4.dp))
                            }
                        } else {
                            Badge(
                                containerColor = Color(0xFFFEF3C7),
                                contentColor = Color(0xFFB45309)
                            ) {
                                Text("+${challenge.rewardPoints} نقطة", fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(4.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}
