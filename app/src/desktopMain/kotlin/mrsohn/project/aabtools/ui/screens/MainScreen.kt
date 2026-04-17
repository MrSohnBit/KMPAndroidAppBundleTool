package mrsohn.project.aabtools.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Error
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Numbers
import androidx.compose.material.icons.rounded.Straighten
import androidx.compose.material.icons.rounded.Upload
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mrsohn.project.aabtools.service.ConversionStatus
import mrsohn.project.aabtools.viewmodel.ConversionViewModel
import java.awt.FileDialog
import java.awt.Frame
import java.io.File

@Composable
fun MainScreen(viewModel: ConversionViewModel) {
    val status by viewModel.status.collectAsState()
    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF2D1B4D), Color(0xFF121212))
                )
            )
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "AABTools",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Your AAB to APK Converter",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.7f),
                modifier = Modifier.padding(bottom = 32.dp)
            )

            // Main Content Area
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(bottom = 24.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF1E1E1E).copy(alpha = 0.8f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (viewModel.aabPath.isEmpty()) {
                        SelectionView(viewModel)
                    } else {
                        FileInfoView(viewModel)
                    }
                }
            }

            // Status View
            AnimatedVisibility(visible = status !is ConversionStatus.Idle) {
                StatusOverlay(status, viewModel)
            }
        }
    }
}

@Composable
fun SelectionView(viewModel: ConversionViewModel) {
    Button(
        onClick = {
            val file = pickFile("Select AAB File", listOf("aab"))
            if (file != null) {
                viewModel.updateAabPath(file.absolutePath)
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        shape = RoundedCornerShape(24.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6200EE))
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Rounded.FolderOpen,
                contentDescription = null,
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text("Select AAB File", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Text("or drag and drop file here", color = Color.White.copy(alpha = 0.6f))
        }
    }
}

@Composable
fun FileInfoView(viewModel: ConversionViewModel) {
    val aabFile = File(viewModel.aabPath)
    val metadata = viewModel.metadata
    
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Rounded.Description, contentDescription = null, tint = Color(0xFFD0BCFF))
            Spacer(modifier = Modifier.width(12.dp))
            Text(aabFile.name, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 20.sp)
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Row(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.weight(1f)) {
                InfoRow(Icons.Rounded.Straighten, "Size", "${"%.1f".format(aabFile.length() / 1024.0 / 1024.0)} MB")
                InfoRow(Icons.Rounded.Numbers, "Version Code", metadata?.versionCode?.toString() ?: "Loading...")
                InfoRow(Icons.Rounded.Info, "Package", metadata?.packageName ?: "Loading...")
            }
            
            Column(modifier = Modifier.weight(1f)) {
                OutlinedCard(
                    onClick = {
                        val dir = pickDirectory("Select Output Directory")
                        if (dir != null) {
                            viewModel.outputDirPath = dir.absolutePath
                        }
                    },
                    modifier = Modifier.fillMaxWidth().padding(start = 8.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.outlinedCardColors(containerColor = Color.Transparent, contentColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.Folder, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Output Folder", fontSize = 12.sp, color = Color.White.copy(alpha = 0.5f))
                        }
                        Text(
                            text = if (viewModel.outputDirPath.isEmpty()) "Default (Same as AAB)" else viewModel.outputDirPath,
                            fontSize = 14.sp,
                            maxLines = 2,
                            color = Color.White
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
        
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Button(
                onClick = { viewModel.convert() },
                modifier = Modifier.weight(1f).height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6200EE))
            ) {
                Icon(Icons.Rounded.Upload, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Convert to APK", fontWeight = FontWeight.Bold)
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            IconButton(
                onClick = { /* More options: Sign, etc. */ },
                modifier = Modifier
                    .size(56.dp)
                    .background(Color(0xFF6200EE), RoundedCornerShape(16.dp))
            ) {
                Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = Color.White)
            }
        }
        
        TextButton(
            onClick = { viewModel.updateAabPath("") },
            modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 8.dp)
        ) {
            Text("Clear Selection", color = Color.White.copy(alpha = 0.6f))
        }
    }
}

@Composable
fun InfoRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(modifier = Modifier.padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = Color.White.copy(alpha = 0.5f), modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(label, fontSize = 12.sp, color = Color.White.copy(alpha = 0.5f))
            Text(value, fontSize = 16.sp, color = Color.White)
        }
    }
}

@Composable
fun StatusOverlay(status: ConversionStatus, viewModel: ConversionViewModel) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF252525))
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (status) {
                is ConversionStatus.Processing -> {
                    CircularProgressIndicator(
                        modifier = Modifier.size(80.dp),
                        color = Color(0xFFD0BCFF),
                        strokeWidth = 8.dp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(status.message, color = Color.White)
                }
                is ConversionStatus.Success -> {
                    Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = Color(0xFF81C784), modifier = Modifier.size(64.dp))
                    Text("Conversion Successful!", color = Color(0xFF81C784), fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    Text(File(status.apkPath).name, color = Color.White.copy(alpha = 0.7f))
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { openFolder(File(status.apkPath).parentFile) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD0BCFF), contentColor = Color.Black)
                    ) {
                        Icon(Icons.Rounded.Folder, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Open APK Folder")
                    }
                }
                is ConversionStatus.Error -> {
                    Icon(Icons.Rounded.Error, contentDescription = null, tint = Color(0xFFE57373), modifier = Modifier.size(64.dp))
                    Text("Error", color = Color(0xFFE57373), fontWeight = FontWeight.Bold)
                    Text(status.message, color = Color.White, modifier = Modifier.padding(top = 8.dp))
                }
                else -> {}
            }
        }
    }
}

fun pickFile(title: String, extensions: List<String>): File? {
    val dialog = FileDialog(Frame(), title, FileDialog.LOAD)
    dialog.isVisible = true
    return if (dialog.file != null) File(dialog.directory, dialog.file) else null
}

fun pickDirectory(title: String): File? {
    System.setProperty("apple.awt.fileDialogForDirectories", "true")
    val dialog = FileDialog(Frame(), title, FileDialog.LOAD)
    dialog.isVisible = true
    System.setProperty("apple.awt.fileDialogForDirectories", "false")
    return if (dialog.file != null) File(dialog.directory, dialog.file) else null
}

fun openFolder(folder: File) {
    try {
        java.awt.Desktop.getDesktop().open(folder)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}


@Preview(showBackground = true)
@Composable
fun PreviewMainScreen() {
    MainScreen(ConversionViewModel())
}
