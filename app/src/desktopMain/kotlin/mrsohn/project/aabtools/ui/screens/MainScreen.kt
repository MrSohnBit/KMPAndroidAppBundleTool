package mrsohn.project.aabtools.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.draganddrop.dragAndDropTarget
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.automirrored.rounded.InsertDriveFile
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draganddrop.DragAndDropEvent
import androidx.compose.ui.draganddrop.DragAndDropTarget
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mrsohn.project.aabtools.LocalWindow
import mrsohn.project.aabtools.service.ConversionStatus
import mrsohn.project.aabtools.viewmodel.ConversionViewModel
import java.awt.FileDialog
import java.awt.Frame
import java.awt.datatransfer.DataFlavor
import java.awt.dnd.DnDConstants
import java.awt.dnd.DropTargetDragEvent
import java.awt.dnd.DropTargetDropEvent
import java.io.File

enum class DragStatus { None, Valid, Invalid }

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun MainScreen(viewModel: ConversionViewModel) {
    val status by viewModel.status.collectAsState()
    var dragStatus by remember { mutableStateOf(DragStatus.None) }
    val scrollState = rememberScrollState()

    // Determine window height based on status
    val windowHeight = if (status is ConversionStatus.Success) 1020.dp else 760.dp
    val window = LocalWindow.current
    LaunchedEffect(windowHeight) {
        window?.let { w ->
            w.setSize(w.width, windowHeight.value.toInt())
        }
    }

    val dndTarget = remember {
        object : DragAndDropTarget {
            private fun updateDragStatus(event: DragAndDropEvent) {
                val files = extractFilesFromEvent(event)
                if (files.isEmpty()) {
                    // If we can't extract files yet, don't show invalid state
                    return
                }
                val hasAab = files.any { it.name.lowercase().endsWith(".aab") }
                dragStatus = if (hasAab) DragStatus.Valid else DragStatus.Invalid
            }

            override fun onEntered(event: DragAndDropEvent) {
                updateDragStatus(event)
            }

            override fun onChanged(event: DragAndDropEvent) {
                updateDragStatus(event)
            }

            override fun onExited(event: DragAndDropEvent) {
                dragStatus = DragStatus.None
            }

            override fun onDrop(event: DragAndDropEvent): Boolean {
                dragStatus = DragStatus.None
                val files = extractFilesFromEvent(event)
                val aabFile = files.firstOrNull { it.name.lowercase().endsWith(".aab") }

                return if (aabFile != null) {
                    viewModel.reset() // Reset status and other info when new AAB is dropped
                    viewModel.updateAabPath(aabFile.absolutePath)
                    true
                } else {
                    false
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .dragAndDropTarget(
                shouldStartDragAndDrop = { true },
                target = dndTarget
            )
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF1A1225), Color(0xFF0F0F0F))
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 40.dp, vertical = 32.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "AABTools",
                    style = MaterialTheme.typography.headlineLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp
                )
                Text(
                    text = "Fast & Simple AAB to APK Converter",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.5f)
                )
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                border = BorderStroke(
                    1.dp, 
                    when (dragStatus) {
                        DragStatus.Valid -> Color.Transparent
                        DragStatus.Invalid -> Color(0xFFFFB4B4)
                        DragStatus.None -> Color.White.copy(alpha = 0.05f)
                    }
                ),
                colors = CardDefaults.cardColors(
                    containerColor = when (dragStatus) {
                        DragStatus.Valid -> Color.Transparent
                        DragStatus.Invalid -> Color(0xFF421D1D)
                        DragStatus.None -> Color(0xFF1C1C1E).copy(alpha = 0.6f)
                    }
                )
            ) {
                Box(
                    modifier = Modifier.padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (viewModel.aabPath.isEmpty()) {
                        SelectionView(viewModel, dragStatus)
                    } else {
                        FileInfoView(viewModel, dndTarget)
                    }
                }
            }

            if (viewModel.aabPath.isNotEmpty() && status is ConversionStatus.Idle) {
                Button(
                    onClick = { viewModel.convert() },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFD0BCFF),
                        contentColor = Color(0xFF381E72)
                    )
                ) {
                    Icon(Icons.Rounded.Straighten, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Convert to APK", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Success View
            AnimatedVisibility(
                visible = status is ConversionStatus.Success,
                enter = fadeIn() + scaleIn(initialScale = 0.9f)
            ) {
                val successStatus = status as? ConversionStatus.Success
                if (successStatus != null) {
                    SuccessResultCard(viewModel, successStatus.apkPath)
                }
            }
        }

        // Processing / Error Overlay
        AnimatedVisibility(
            visible = status is ConversionStatus.Processing || status is ConversionStatus.Error,
            enter = fadeIn() + scaleIn(initialScale = 0.9f),
            exit = fadeOut() + scaleOut(targetScale = 0.9f),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.7f)).clickable(enabled = false) {},
                contentAlignment = Alignment.Center
            ) {
                StatusOverlay(status, viewModel)
            }
        }
    }
}

@Composable
fun SuccessResultCard(viewModel: ConversionViewModel, apkPath: String) {
    val apkFile = File(apkPath)
    val apkSize = formatFileSize(apkFile.length())

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF242426)),
        border = BorderStroke(1.dp, Color(0xFFB4FF9F).copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.CheckCircle, null, tint = Color(0xFFB4FF9F), modifier = Modifier.size(32.dp))
                Spacer(Modifier.width(16.dp))
                Column {
                    Text("Conversion Successful", style = MaterialTheme.typography.titleMedium, color = Color.White)
                    Text("Universal APK is ready", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.5f))
                }
                Spacer(Modifier.weight(1f))
                Button(
                    onClick = { viewModel.openFolder(apkPath) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.05f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Rounded.Folder, null, modifier = Modifier.size(18.dp), tint = Color(0xFFD0BCFF))
                    Spacer(Modifier.width(8.dp))
                    Text("Open Folder", fontSize = 13.sp, color = Color.White)
                }
            }

            Spacer(Modifier.height(20.dp))
            Surface(
                color = Color.Black.copy(alpha = 0.2f),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.AutoMirrored.Rounded.InsertDriveFile, null, tint = Color.White.copy(alpha = 0.3f))
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(apkFile.name, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(apkPath, color = Color.White.copy(alpha = 0.4f), fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(apkSize, color = Color(0xFFD0BCFF), fontSize = 11.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
            HorizontalDivider(color = Color.White.copy(alpha = 0.05f))
            Spacer(Modifier.height(24.dp))

            DeviceSelectionCard(viewModel, apkPath)

            Spacer(Modifier.height(24.dp))
            TextButton(
                onClick = { viewModel.reset() },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                colors = ButtonDefaults.textButtonColors(contentColor = Color.White.copy(alpha = 0.4f))
            ) {
                Icon(Icons.Rounded.Refresh, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Start New Conversion")
            }
        }
    }
}

@Composable
fun DeviceSelectionCard(viewModel: ConversionViewModel, apkPath: String) {
    val devices = viewModel.connectedDevices
    val installationStatus = viewModel.installationStatus
    val selectedSerials = viewModel.selectedDeviceSerials

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Install to Devices", style = MaterialTheme.typography.titleSmall, color = Color.White)
                Text("${selectedSerials.count { it.value }} devices selected", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.4f))
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = { viewModel.deselectAllDevices() }) {
                    Text("Deselect All", fontSize = 12.sp, color = Color.White.copy(alpha = 0.5f))
                }

                TextButton(onClick = { viewModel.selectAllDevices() }) {
                    Text("Select All", fontSize = 12.sp, color = Color(0xFFD0BCFF))
                }

                IconButton(onClick = { viewModel.refreshDevices() }, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Rounded.Refresh, null, tint = Color(0xFFD0BCFF), modifier = Modifier.size(16.dp))
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        if (devices.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxWidth().height(100.dp).background(Color.White.copy(alpha = 0.02f), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Rounded.PhonelinkOff, null, tint = Color.White.copy(alpha = 0.1f), modifier = Modifier.size(32.dp))
                    Spacer(Modifier.height(8.dp))
                    Text("No devices connected", color = Color.White.copy(alpha = 0.3f), fontSize = 13.sp)
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                devices.forEach { device ->
                    val isSelected = selectedSerials[device.serial] ?: false
                    Card(
                        onClick = { viewModel.toggleDeviceSelection(device.serial) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) Color(0xFFD0BCFF).copy(alpha = 0.1f) else Color.White.copy(alpha = 0.03f)
                        ),
                        border = BorderStroke(1.dp, if (isSelected) Color(0xFFD0BCFF).copy(alpha = 0.4f) else Color.Transparent)
                    ) {
                        ListItem(
                            headlineContent = { Text(device.model, color = Color.White, fontSize = 14.sp) },
                            supportingContent = { Text(device.serial, color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp) },
                            leadingContent = { 
                                Icon(
                                    Icons.Rounded.Smartphone, 
                                    null, 
                                    tint = if (isSelected) Color(0xFFD0BCFF) else Color.White.copy(alpha = 0.3f)
                                ) 
                            },
                            trailingContent = {
                                Checkbox(
                                    checked = isSelected,
                                    onCheckedChange = null, // Handled by Card onClick
                                    colors = CheckboxDefaults.colors(checkedColor = Color(0xFFD0BCFF))
                                )
                            },
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = { viewModel.installToSelectedDevices(apkPath) },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(12.dp),
                enabled = selectedSerials.any { it.value },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD0BCFF), contentColor = Color(0xFF381E72))
            ) {
                Icon(Icons.Rounded.Download, null)
                Spacer(Modifier.width(8.dp))
                Text("Install to Selected Devices", fontWeight = FontWeight.Bold)
            }
        }
        
        installationStatus?.let {
            Spacer(modifier = Modifier.height(16.dp))
            Surface(
                color = if (it.contains("Error")) Color(0xFFFFB4B4).copy(alpha = 0.1f) else Color(0xFFD0BCFF).copy(alpha = 0.1f),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        if (it.contains("Error")) Icons.Rounded.ErrorOutline else Icons.Rounded.Info, 
                        null, 
                        tint = if (it.contains("Error")) Color(0xFFFFB4B4) else Color(0xFFD0BCFF),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(it, color = if (it.contains("Error")) Color(0xFFFFB4B4) else Color(0xFFD0BCFF), fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun SelectionView(viewModel: ConversionViewModel, dragStatus: DragStatus) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp)
            .clickable {
                val file = pickFile("Select AAB File", listOf("aab"))
                if (file != null) viewModel.updateAabPath(file.absolutePath)
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            if (dragStatus == DragStatus.Invalid) Icons.Rounded.ErrorOutline else Icons.Rounded.FolderOpen,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = when (dragStatus) {
                DragStatus.Valid -> Color(0xFFD0BCFF)
                DragStatus.Invalid -> Color(0xFFFFB4B4)
                DragStatus.None -> Color.White.copy(alpha = 0.3f)
            }
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            if (dragStatus == DragStatus.Invalid) "Invalid File (AAB Only)" else "Select or Drag AAB File",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = if (dragStatus == DragStatus.Invalid) Color(0xFFFFB4B4) else Color.White
        )
        Text(
            if (dragStatus == DragStatus.Invalid) "Please drop a .aab file instead" else "BundleTool will generate a universal APK",
            color = if (dragStatus == DragStatus.Invalid) Color(0xFFFFB4B4).copy(alpha = 0.6f) else Color.White.copy(alpha = 0.4f),
            fontSize = 14.sp
        )
    }
}

@Composable
fun FileInfoView(viewModel: ConversionViewModel, dndTarget: DragAndDropTarget) {
    val metadata = viewModel.metadata
    val file = File(viewModel.aabPath)
    val fileSize = if (file.exists()) formatFileSize(file.length()) else "Unknown size"

    Column(verticalArrangement = Arrangement.spacedBy(20.dp),
        modifier = Modifier.dragAndDropTarget(
            shouldStartDragAndDrop = { true },
            target = dndTarget
        )) {
        Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Selected AAB", style = MaterialTheme.typography.labelMedium, color = Color(0xFFD0BCFF))
                    Row(verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clickable() {
                                viewModel.openFolder(viewModel.aabPath)
                            }) {
                        Text(
                            file.name,
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f, fill = false),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(Modifier.width(8.dp))
                        Icon(Icons.Rounded.Folder, null, tint = Color(0xFFD0BCFF), modifier = Modifier.size(14.dp))
                    }
                    Text(fileSize, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.5f))
                }
            IconButton(
                onClick = { viewModel.reset() },
                colors = IconButtonDefaults.iconButtonColors(containerColor = Color.White.copy(alpha = 0.05f))
            ) {
                Icon(Icons.Rounded.Close, null, tint = Color.White)
            }
        }

        HorizontalDivider(color = Color.White.copy(alpha = 0.05f))

        if (metadata != null) {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                InfoItem(Icons.Rounded.Android, "Package", metadata.packageName, Modifier.weight(1.5f))
                InfoItem(Icons.Rounded.Numbers, "Version", "${metadata.versionName} (${metadata.versionCode})", Modifier.weight(1f))
            }
        } else {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = Color(0xFFD0BCFF))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Fetching metadata...", color = Color.White.copy(alpha = 0.4f), fontSize = 13.sp)
            }
        }

        SigningOptionsCard(viewModel)
    }
}

@Composable
fun InfoItem(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 4.dp)) {
            Icon(icon, null, tint = Color.White.copy(alpha = 0.3f), modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(4.dp))
            Text(label, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.3f))
        }
        Text(value, style = MaterialTheme.typography.bodyMedium, color = Color.White, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SigningOptionsCard(viewModel: ConversionViewModel) {
    var expanded by remember { mutableStateOf(false) }
    var showSaveDialog by remember { mutableStateOf(false) }
    var newProfileName by remember { mutableStateOf("") }

    OutlinedCard(
        modifier = Modifier.fillMaxWidth().animateContentSize(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.outlinedCardColors(containerColor = Color.White.copy(alpha = 0.02f)),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.VpnKey, null, tint = Color(0xFFD0BCFF), modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    val jksFileName = viewModel.keystorePath.substringAfterLast(File.separator).takeIf { it.isNotEmpty() }
                    Text(
                        text = if (jksFileName != null) "Signing ($jksFileName)" else "Signing",
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                        color = Color.White
                    )
                }
                Icon(if (expanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore, null, tint = Color.White.copy(alpha = 0.4f))
            }

            if (expanded) {
                if (viewModel.savedProfiles.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Saved Profiles", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.4f))
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        viewModel.savedProfiles.forEach { profile ->
                            Surface(
                                onClick = { viewModel.applyProfile(profile) },
                                shape = RoundedCornerShape(8.dp),
                                color = Color.White.copy(alpha = 0.05f),
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(profile.name, fontSize = 11.sp, color = Color.White)
                                    IconButton(
                                        onClick = { viewModel.deleteProfile(profile) },
                                        modifier = Modifier.size(16.dp)
                                    ) {
                                        Icon(Icons.Rounded.Close, null, tint = Color.White.copy(alpha = 0.4f), modifier = Modifier.size(10.dp))
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = viewModel.keystorePath,
                    onValueChange = { viewModel.keystorePath = it },
                    label = { Text("Keystore Path", fontSize = 12.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        IconButton(onClick = {
                            val file = pickFile("Select Keystore", listOf("jks", "keystore"))
                            if (file != null) viewModel.keystorePath = file.absolutePath
                        }) { Icon(Icons.Rounded.Folder, null, modifier = Modifier.size(20.dp)) }
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = viewModel.keystorePassword,
                    onValueChange = { viewModel.keystorePassword = it },
                    label = { Text("Keystore Password", fontSize = 12.sp) },
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = viewModel.keyAlias,
                        onValueChange = { viewModel.keyAlias = it },
                        label = { Text("Key Alias", fontSize = 12.sp) },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = viewModel.keyPassword,
                        onValueChange = { viewModel.keyPassword = it },
                        label = { Text("Key Password", fontSize = 12.sp) },
                        modifier = Modifier.weight(1f),
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { showSaveDialog = true },
                    modifier = Modifier.fillMaxWidth().height(40.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.05f)),
                    enabled = viewModel.keystorePath.isNotEmpty() && viewModel.keystorePassword.isNotEmpty()
                ) {
                    Icon(Icons.Rounded.CheckCircle, null, modifier = Modifier.size(16.dp), tint = Color(0xFFD0BCFF))
                    Spacer(Modifier.width(8.dp))
                    Text("Save as Profile", fontSize = 12.sp, color = Color.White)
                }
            }
        }
    }

    if (showSaveDialog) {
        androidx.compose.ui.window.DialogWindow(
            onCloseRequest = { showSaveDialog = false },
            title = "Save Keystore Profile",
            state = androidx.compose.ui.window.rememberDialogState(width = 400.dp, height = 300.dp)
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color(0xFF1C1C1E)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text("Profile Name", style = MaterialTheme.typography.titleMedium, color = Color.White)
                    Spacer(Modifier.height(16.dp))
                    OutlinedTextField(
                        value = newProfileName,
                        onValueChange = { newProfileName = it },
                        placeholder = { Text("e.g. My App Key", color = Color.White.copy(alpha = 0.3f)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.weight(1f))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = { showSaveDialog = false }) {
                            Text("Cancel", color = Color.White.copy(alpha = 0.5f))
                        }
                        Button(
                            onClick = {
                                if (newProfileName.isNotEmpty()) {
                                    viewModel.saveCurrentAsProfile(newProfileName)
                                    showSaveDialog = false
                                    newProfileName = ""
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD0BCFF))
                        ) {
                            Text("Save", color = Color(0xFF381E72))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatusOverlay(status: ConversionStatus, viewModel: ConversionViewModel) {
    Card(
        modifier = Modifier.fillMaxWidth(0.85f).wrapContentHeight(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF242426)),
        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
    ) {
        Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (status) {
                is ConversionStatus.Processing -> {
                    CircularProgressIndicator(modifier = Modifier.size(48.dp), color = Color(0xFFD0BCFF))
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(status.message, color = Color.White, textAlign = TextAlign.Center)
                }
                is ConversionStatus.Error -> {
                    Icon(Icons.Rounded.Error, null, tint = Color(0xFFFFB4B4), modifier = Modifier.size(64.dp))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Conversion Failed", style = MaterialTheme.typography.titleLarge, color = Color.White, fontWeight = FontWeight.Bold)
                    Text(status.message, color = Color.White.copy(alpha = 0.7f), textAlign = TextAlign.Center, modifier = Modifier.padding(top = 8.dp))
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    Button(
                        onClick = { viewModel.reset() },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.1f))
                    ) {
                        Text("Try Again")
                    }
                }
                else -> {}
            }
        }
    }
}

fun pickFile(title: String, allowedExtensions: List<String>): File? {
    val frame = Frame()
    val dialog = FileDialog(frame, title, FileDialog.LOAD)
    dialog.setFilenameFilter { _, name -> allowedExtensions.any { name.lowercase().endsWith(it) } }
    dialog.isVisible = true
    val file = dialog.file
    val dir = dialog.directory
    dialog.dispose()
    frame.dispose()
    return if (file != null && dir != null) File(dir, file) else null
}

@OptIn(ExperimentalComposeUiApi::class)
private fun extractFilesFromEvent(event: DragAndDropEvent): List<File> {
    return when (val nativeEvent = event.nativeEvent) {
        is DropTargetDragEvent -> getDroppedFiles(nativeEvent.transferable)
        is DropTargetDropEvent -> {
            nativeEvent.acceptDrop(DnDConstants.ACTION_COPY)
            val files = getDroppedFiles(nativeEvent.transferable)
            nativeEvent.dropComplete(files.isNotEmpty())
            files
        }
        else -> emptyList()
    }
}

private fun getDroppedFiles(transferable: java.awt.datatransfer.Transferable): List<File> {
    return try {
        if (transferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
            @Suppress("UNCHECKED_CAST")
            transferable.getTransferData(DataFlavor.javaFileListFlavor) as List<File>
        } else emptyList()
    } catch (_: Exception) { emptyList() }
}

private fun formatFileSize(bytes: Long): String {
    val kb = bytes / 1024.0
    val mb = kb / 1024.0
    return if (mb >= 1.0) "%.2f MB".format(mb) else "%.2f KB".format(kb)
}
