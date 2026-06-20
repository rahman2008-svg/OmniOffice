package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AboutScreen(
    modifier: Modifier = Modifier
) {
    val uriHandler = LocalUriHandler.current

    val primaryGradient = Brush.linearGradient(
        colors = listOf(Color(0xFF1E3A8A), Color(0xFF3B82F6), Color(0xFF06B6D4))
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC)), // Slate-50 background page
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Sleek Developer Identity Card (Hero Block)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Profile initials stylized with gradient ring
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .background(primaryGradient, CircleShape)
                            .padding(4.dp)
                            .background(Color.White, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(primaryGradient, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "AR",
                                color = Color.White,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Prince AR Abdur Rahman",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF0F172A),
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "Independent App Developer",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2563EB),
                        modifier = Modifier
                            .background(Color(0xFFEFF6FF), RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Passionate about building modern Android applications, productivity tools, AI-powered experiences, media players, educational apps, and next-generation digital products.",
                        fontSize = 13.sp,
                        color = Color(0xFF475569),
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        // 2. Company Details - NexVora Lab's Ofc
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(Color(0xFFECFDF5), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Business,
                                contentDescription = "Company icon",
                                tint = Color(0xFF10B981)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "NexVora Lab's Ofc",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )
                            Text(
                                text = "Innovative Digital Development Studio",
                                fontSize = 11.sp,
                                color = Color(0xFF64748B)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Focuses on creating innovative Android applications designed to improve productivity, entertainment, learning, and digital experiences.",
                        fontSize = 13.sp,
                        color = Color(0xFF475569),
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    HorizontalDivider(color = Color(0xFFF1F5F9))

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "MISSION",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF10B981),
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Build fast, beautiful, privacy-friendly, and user-focused applications accessible to everyone.",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF0F172A)
                    )
                }
            }
        }

        // 3. System Products Directory
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(Color(0xFFFFF7ED), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Layers,
                                contentDescription = "Products",
                                tint = Color(0xFFF97316)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Company Products",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    val productsList = listOf(
                        com.example.ui.ProductItem("NexPlay X", "Premium Media System", Icons.Filled.PlayCircle, Color(0xFFEF4444)),
                        com.example.ui.ProductItem("LifeSphere OS", "Dynamic Utility Ecosystem", Icons.Filled.DesktopAccessDisabled, Color(0xFF3B82F6)),
                        com.example.ui.ProductItem("Smart Day Planner X", "Optimized Schedule Builder", Icons.Filled.CalendarMonth, Color(0xFF10B981)),
                        com.example.ui.ProductItem("Study AI", "Offline Learning Portal", Icons.Filled.School, Color(0xFFF59E0B)),
                        com.example.ui.ProductItem("Lensora Studio", "High-Fidelity Camera Suite", Icons.Filled.Camera, Color(0xFF8B5CF6)),
                        com.example.ui.ProductItem("Offline AI", "Local Generative Sandbox", Icons.Filled.Memory, Color(0xFFEC4899)),
                        com.example.ui.ProductItem("NexVora Love Space", "Intimate Creative Canvas", Icons.Filled.Favorite, Color(0xFFE11D48)),
                        com.example.ui.ProductItem("CalcVerse", "Advanced Calculator Arena", Icons.Filled.Functions, Color(0xFF06B6D4)),
                        com.example.ui.ProductItem("NexVoice OS", "Smart Microphone Controller", Icons.Filled.Mic, Color(0xFF6366F1))
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        productsList.forEach { prod ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFFF8FAFC), RoundedCornerShape(12.dp))
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .background(prod.color.copy(alpha = 0.1f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = prod.icon,
                                        contentDescription = prod.name,
                                        tint = prod.color,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = prod.name,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF0F172A)
                                    )
                                    Text(
                                        text = prod.desc,
                                        fontSize = 10.sp,
                                        color = Color(0xFF64748B)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 4. Contact, Channels & Community Links
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(Color(0xFFEFF6FF), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.ContactSupport,
                                contentDescription = "Connect",
                                tint = Color(0xFF3B82F6)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Get in Touch",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // WhatsApp Channel 1
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { uriHandler.openUri("https://wa.me/8801707424006") }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.Call, contentDescription = "WhatsApp", tint = Color(0xFF25D366))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("WhatsApp (Primary Contact)", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text("01707424006", fontSize = 11.sp, color = Color(0xFF64748B))
                        }
                    }

                    HorizontalDivider(color = Color(0xFFF1F5F9))

                    // WhatsApp Channel 2
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { uriHandler.openUri("https://wa.me/8801796951709") }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.Call, contentDescription = "WhatsApp secondary", tint = Color(0xFF25D366))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("WhatsApp (Secondary Channel)", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text("01796951709", fontSize = 11.sp, color = Color(0xFF64748B))
                        }
                    }

                    HorizontalDivider(color = Color(0xFFF1F5F9))

                    // Facebook Social
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { uriHandler.openUri("https://www.facebook.com/share/1BNn32qoJo/") }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.Public, contentDescription = "Facebook logo", tint = Color(0xFF1877F2))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Connect via Facebook", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text("facebook.com/share/Prince", fontSize = 10.sp, color = Color(0xFF3B82F6))
                        }
                    }

                    HorizontalDivider(color = Color(0xFFF1F5F9))

                    // Instagram Social
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { uriHandler.openUri("https://www.instagram.com/ur___abdur____rahman__2008") }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.PhotoCamera, contentDescription = "Instagram logo", tint = Color(0xFFE1306C))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Follow on Instagram", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text("@ur___abdur____rahman__2008", fontSize = 10.sp, color = Color(0xFFE1306C))
                        }
                    }
                }
            }
        }

        // 5. Technical Stack Metadata
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(Color(0xFFF1F5F9), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Info,
                                contentDescription = "System Info",
                                tint = Color(0xFF475569)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Technical Information",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Application Version", fontSize = 12.sp, color = Color(0xFF64748B))
                        Text("1.0.0", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = Color(0xFFF1F5F9))
                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "DEPLOYMENT & CI/CD",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF475569),
                        letterSpacing = 1.sp
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))

                    val pipelineList = listOf("GitHub Actions", "Codemagic CI/CD", "Automated APK Build", "Release Workflow")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        pipelineList.forEach { pipe ->
                            Text(
                                text = pipe,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF475569),
                                modifier = Modifier
                                    .background(Color(0xFFF1F5F9), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }

        // 6. Signature / Trademark Footnotes
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Developed by Prince AR Abdur Rahman",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF475569),
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Published by NexVora Lab's Ofc",
                    fontSize = 10.sp,
                    color = Color(0xFF64748B),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "© 2026 NexVora Lab's Ofc. All Rights Reserved.",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF94A3B8),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

data class ProductItem(
    val name: String,
    val desc: String,
    val icon: ImageVector,
    val color: Color
)
